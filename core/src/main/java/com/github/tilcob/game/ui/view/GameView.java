package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.ItemModel;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.Map;

public class GameView extends View<GameViewModel> {
    private final HorizontalGroup lifeGroup;
    private Table inventoryRoot;

    public GameView(Skin skin, Stage stage, GameViewModel viewModel) {
        super(skin, stage, viewModel);
        this.lifeGroup = findActor("lifeGroup");
        updateLife(viewModel.getLifePoints());
    }

    @Override
    protected void setupUI() {
        align(Align.bottomLeft);
        setFillParent(true);

        inventoryRoot = new Table();
        inventoryRoot.setName("inventoryRoot");
        inventoryRoot.setFillParent(true);
        inventoryRoot.setVisible(false);

        Table table1 = new Table();
        table1.setName("inventoryPanel");

        Label label = new Label("Inventory", skin);
        label.setColor(skin.getColor("black"));
        table1.add(label);

        table1.row();

        Table table2 = new Table();
        table2.setName("items");

        table2.add();

        table2.add();
        ScrollPane scrollPane = new ScrollPane(table2, skin);
        table1.add(scrollPane);
        inventoryRoot.add(table1);
        stage.addActor(inventoryRoot);

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.setName("lifeGroup");
        horizontalGroup.padLeft(5.0f);
        horizontalGroup.padBottom(5.0f);
        horizontalGroup.space(5.0f);
        add(horizontalGroup);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.LIFE_POINTS_PC, Integer.class, this::updateLife);
        viewModel.onPropertyChange(Constants.PLAYER_DAMAGE_PC, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(Constants.ADD_ITEMS, Array.class, this::updatePlayerItems);
        viewModel.onPropertyChange(Constants.OPEN_INVENTORY, Boolean.class, this::setInventoryVisibility);
    }

    private void setInventoryVisibility(boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }

    private void updatePlayerItems(Array<ItemModel> array) {
        Gdx.app.log("updatePlayerItems", "items: " + array);
    }

    private void updateLife(int lifePoints) {
        lifeGroup.clear();

        int maxLife = viewModel.getMaxLife();
        while (maxLife > 0) {
            int imgIdx = MathUtils.clamp(lifePoints, 0, 4);
            Image image = new Image(skin, "life_0" + imgIdx);
            lifeGroup.addActor(image);

            maxLife -= 4;
            lifePoints -= 4;
        }
    }

    private void showDamage(Map.Entry<Vector2, Integer> damageAndPosition) {
        final Vector2 position = damageAndPosition.getKey();
        int damage = damageAndPosition.getValue();

        TextraLabel textraLabel = new TypingLabel("[%50]{JUMP=2.0;0.5;0.9}{RAINBOW}" + damage, skin, "small");
        stage.addActor(textraLabel);

        textraLabel.addAction(
            Actions.parallel(
                Actions.sequence(Actions.delay(1.25f), Actions.removeActor()),
                Actions.forever(Actions.run(() -> {
                    Vector2 stageCoords = toStageCoords(position);
                    textraLabel.setPosition(stageCoords.x, stageCoords.y);
                }))
            )
        );
    }

    private Vector2 toStageCoords(Vector2 gamePosition) {
        Vector2 resultPos = viewModel.toScreenCoords(gamePosition);
        stage.getViewport().unproject(resultPos);
        resultPos.y = stage.getViewport().getWorldHeight() -  resultPos.y;
        return resultPos;
    }
}
