package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.save.states.SkillTreeState;
import com.github.tilcob.game.skill.data.SkillNodeDefinition;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;
import com.github.tilcob.game.ui.model.SkillTreeViewModel;

public class SkillTreeView extends View<SkillTreeViewModel> {
    private static final float PANEL_PADDING = 12f;
    private static final float TEXT_COLUMN_MAX_WIDTH = 320f;

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
        setTouchable(Touchable.enabled);

        rootTable = new Table();
        rootTable.background(skin.getDrawable("Other_panel_brown"));
        rootTable.pad(PANEL_PADDING);
        rootTable.setTouchable(Touchable.enabled);
        setRoot(rootTable);
        setVisibleBound(false);
        rootTable.top();

        pointsLabel = new Label("Points: 0", skin, "text_10");
        pointsLabel.setColor(skin.getColor("BLACK"));
        tabsTable = new Table();
        nodesTable = new Table();

        TextButton closeBtn = new TextButton("Close (K)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                viewModel.setOpen(false);
            }
        });
        Label titleLabel = new Label("Skills", skin, "text_10");
        titleLabel.setAlignment(1);
        titleLabel.setColor(skin.getColor("BLACK"));

        Table titleRow = new Table();
        titleRow.add(titleLabel).expandX().left().pad(6);
        titleRow.add(pointsLabel).expandX().center().pad(6);
        titleRow.add(closeBtn).right().pad(6);

        ScrollPane scrollPane = new ScrollPane(nodesTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setTouchable(Touchable.enabled);

        rootTable.add(titleRow).expandX().fillX().row();
        rootTable.add(tabsTable).expandX().fillX().pad(6, 0, 6, 0).row();
        rootTable.add(scrollPane).expand().fill().pad(6).row();

        add(rootTable).expand().fill().pad(20);
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
        setVisibleBound(open);
        setTouchable(open ? Touchable.enabled : Touchable.disabled);
        if (rootTable != null) {
            rootTable.setTouchable(open ? Touchable.enabled : Touchable.disabled);
        }
        if (open) {
            toFront();
            refresh();
        }
    }

    private void refresh() {
        buildTabs();
        SkillTreeDefinition def = viewModel.getTreeDefinition();
        SkillTreeState state = viewModel.getTreeState();
        if (def == null || state == null) return;

        int displayLevel = state.getCurrentLevel() + 1;
        int sharedPoints = viewModel.getSharedSkillPoints();
        pointsLabel.setText("Level: " + displayLevel + " | Points: " + sharedPoints);

        nodesTable.clear();
        nodesTable.defaults().expandX().fillX().pad(6);
        for (SkillNodeDefinition node : def.getNodes()) {
            boolean unlocked = state.isUnlocked(node.getId());
            boolean affordable = sharedPoints >= node.getCost();
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

            boolean canUnlock = !unlocked && affordable && levelReq && parentsUnlocked;
            String status = unlocked ? "Unlocked"
                : (canUnlock ? "Ready" : "Locked");
            String requirements = "Cost: " + node.getCost()
                + " | Required Level: " + node.getRequiredLevel()
                + " | Status: " + status;

            Table nodeRow = new Table();
            nodeRow.background(skin.getDrawable("Other_panel_brown"));
            nodeRow.pad(8);

            Label nameLabel = new Label(node.getName(), skin, "text_10");
            nameLabel.setColor(skin.getColor("BLACK"));
            Label descLabel = new Label(node.getDescription(), skin, "text_10");
            descLabel.setColor(skin.getColor("BLACK"));
            Label reqLabel = new Label(requirements, skin, "text_10");
            reqLabel.setColor(skin.getColor("BLACK"));
            nameLabel.setWrap(true);
            descLabel.setWrap(true);
            reqLabel.setWrap(true);

            Table textColumn = new Table();
            textColumn.defaults().left().growX().maxWidth(TEXT_COLUMN_MAX_WIDTH);
            textColumn.add(nameLabel).row();
            textColumn.add(descLabel).padTop(4).row();
            textColumn.add(reqLabel).padTop(4).row();

            String buttonText = unlocked ? "Unlocked" : (canUnlock ? "Unlock" : "Locked");
            TextButton actionButton = new TextButton(buttonText, skin);
            actionButton.setDisabled(!canUnlock);
            actionButton.setTouchable(canUnlock ? Touchable.enabled : Touchable.disabled);

            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!canUnlock) return;
                    viewModel.unlockNode(node.getId());
                    refresh();
                }
            });

            nodeRow.add(textColumn).expandX().fillX().left().padRight(10).maxWidth(TEXT_COLUMN_MAX_WIDTH);
            nodeRow.add(actionButton).right();
            nodesTable.add(nodeRow).row();
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
            tabsTable.add(tabButton).padRight(6).height(32);
        }
        tabsTable.row();
    }
}
