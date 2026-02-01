package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.github.tilcob.game.save.states.SkillTreeState;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.skill.data.SkillNodeDefinition;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;
import com.github.tilcob.game.ui.model.SkillTreeViewModel;

public class SkillTreeView extends View<SkillTreeViewModel> {
    private Table rootTable;
    private Table tabsTable;
    private Label pointsLabel;
    private Table nodesTable;

    public SkillTreeView(Skin skin, Stage stage, SkillTreeViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        rootTable = new Table();
        rootTable.background(skin.getDrawable("Other_panel_brown"));
        rootTable.setVisible(false);
        rootTable.top();

        pointsLabel = new Label("Points: 0", skin);
        tabsTable = new Table();
        nodesTable = new Table();

        TextButton closeBtn = new TextButton("Close (K)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                viewModel.setOpen(false);
            }
        });
        Label titleLabel = new Label("Skills", skin);
        titleLabel.setAlignment(1);

        Table titleRow = new Table();
        titleRow.add(titleLabel).expandX().left().pad(10);
        titleRow.add(closeBtn).pad(10);

        ScrollPane scrollPane = new ScrollPane(nodesTable, skin);
        scrollPane.setFadeScrollBars(false);

        rootTable.add(titleRow).expandX().fillX().row();
        rootTable.add(tabsTable).expandX().fillX().pad(5, 10, 5, 10).row();
        rootTable.add(pointsLabel).pad(10).left().row();
        rootTable.add(scrollPane).expand().fill().pad(10).row();

        resizeRootTable();
        centerRootTable();

        addActor(rootTable);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.OPEN_SKILLS, Boolean.class, this::setSkillTreeVisibility);
        viewModel.onPropertyChange(Constants.SKILL_TREE_UPDATED, Boolean.class, updated -> {
            if (viewModel.isOpen()) {
                refresh();
            }
        });
    }

    private void setSkillTreeVisibility(boolean open) {
        rootTable.setVisible(open);
        if (open) {
            resizeRootTable();
            centerRootTable();
            refresh();
        }
    }

    private void resizeRootTable() {
        float padding = 40f;
        float maxWidth = Math.max(200f, stage.getWidth() - padding);
        float maxHeight = Math.max(200f, stage.getHeight() - padding);
        float width = Math.min(600f, maxWidth);
        float height = Math.min(400f, maxHeight);
        rootTable.setSize(width, height);
    }

    private void centerRootTable() {
        float x = (stage.getWidth() - rootTable.getWidth()) / 2f;
        float y = (stage.getHeight() - rootTable.getHeight()) / 2f;
        rootTable.setPosition(x, y);
    }

    private void refresh() {
        buildTabs();
        SkillTreeDefinition def = viewModel.getTreeDefinition();
        SkillTreeState state = viewModel.getTreeState();
        if (def == null || state == null)
            return;

        int displayLevel = state.getCurrentLevel() + 1;
        pointsLabel.setText("Level: " + displayLevel + " | Points: " + state.getSkillPoints());

        nodesTable.clear();
        for (SkillNodeDefinition node : def.getNodes()) {
            boolean unlocked = state.isUnlocked(node.getId());
            boolean affordable = state.getSkillPoints() >= node.getCost();
            boolean levelReq = displayLevel >= node.getRequiredLevel();
            boolean parentsUnlocked = true;
            if (node.getParentIds() != null) {
                for (String pid : node.getParentIds()) {
                    if (!state.isUnlocked(pid)) {
                        parentsUnlocked = false;
                        break;
                    }
                }
            }

            String status = unlocked ? "[UNLOCKED]"
                    : (affordable && levelReq && parentsUnlocked ? "[UNLOCK]" : "[LOCKED]");
            String text = node.getName() + "\n" + node.getDescription() + "\nCost: " + node.getCost() + " " + status;

            TextButton nodeBtn = new TextButton(text, skin);
            nodeBtn.setDisabled(!(!unlocked && affordable && levelReq && parentsUnlocked));

            if (!unlocked && affordable && levelReq && parentsUnlocked) {
                nodeBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        viewModel.unlockNode(node.getId());
                        refresh();
                    }
                });
            }

            nodesTable.add(nodeBtn).pad(5).fillX().row();
        }
    }

    private void buildTabs() {
        tabsTable.clear();
        String activeTreeId = viewModel.getActiveTreeId();
        for (String treeId : viewModel.getTreeIds()) {
            SkillTreeDefinition treeDefinition = viewModel.getTreeDefinition(treeId);
            String label = treeDefinition != null && treeDefinition.getName() != null
                ? treeDefinition.getName()
                : treeId;
            TextButton tabButton = new TextButton(label, skin);
            boolean isActive = treeId.equals(activeTreeId);
            tabButton.setDisabled(isActive);
            if (!isActive) {
                tabButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        viewModel.setActiveTreeId(treeId);
                    }
                });
            }
            tabsTable.add(tabButton).padRight(6);
        }
        tabsTable.row();
    }
}
