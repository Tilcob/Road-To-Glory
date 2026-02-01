package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.tilcob.game.component.SkillTreeState;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.skill.data.SkillNodeDefinition;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;
import com.github.tilcob.game.ui.model.SkillTreeViewModel;

public class SkillTreeView extends View<SkillTreeViewModel> {
    private Window window;
    private Label pointsLabel;
    private Table nodesTable;

    public SkillTreeView(Skin skin, Stage stage, SkillTreeViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        window = new Window("Skills", skin);
        window.getTitleLabel().setAlignment(1);
        window.setMovable(false);
        window.setVisible(false);

        pointsLabel = new Label("Points: 0", skin);
        nodesTable = new Table();

        window.add(pointsLabel).pad(10).row();
        window.add(nodesTable).expand().fill().row();

        // Close button
        TextButton closeBtn = new TextButton("Close (K)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                viewModel.setOpen(false);
            }
        });
        window.add(closeBtn).pad(10);

        window.setSize(600, 400); // Fixed size for now
        window.setPosition(
                (Constants.WIDTH * Constants.WINDOW_FACTOR - window.getWidth()) / 2,
                (Constants.HEIGHT * Constants.WINDOW_FACTOR - window.getHeight()) / 2);

        addActor(window);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.OPEN_SKILLS, Boolean.class, this::setSkillTreeVisibility);
    }

    private void setSkillTreeVisibility(boolean open) {
        window.setVisible(open);
        if (open) {
            refresh();
        }
    }

    private void refresh() {
        SkillTreeDefinition def = viewModel.getTreeDefinition();
        SkillTreeState state = viewModel.getTreeState();
        if (def == null || state == null)
            return;

        pointsLabel.setText("Level: " + state.getCurrentLevel() + " | Points: " + state.getSkillPoints());

        nodesTable.clear();
        for (SkillNodeDefinition node : def.getNodes()) {
            boolean unlocked = state.isUnlocked(node.getId());
            boolean affordable = state.getSkillPoints() >= node.getCost();
            boolean levelReq = state.getCurrentLevel() >= node.getRequiredLevel();
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
}
