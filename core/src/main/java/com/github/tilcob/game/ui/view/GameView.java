package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.inventory.InventoryDragAndDrop;
import com.github.tilcob.game.ui.inventory.InventoryItemSource;
import com.github.tilcob.game.ui.inventory.InventorySlot;
import com.github.tilcob.game.ui.inventory.InventorySlotTarget;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.ItemModel;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.Map;

public class GameView extends View<GameViewModel> {
    private final HorizontalGroup lifeGroup;
    private Table inventoryRoot;
    private InventorySlot[][] slots;

    public GameView(Skin skin, Stage stage, GameViewModel viewModel) {
        super(skin, stage, viewModel);
        this.lifeGroup = findActor("lifeGroup");
        updateLife(viewModel.getLifePoints());
    }

    @Override
    protected void setupUI() {
        slots = new InventorySlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        dragAndDrop = new InventoryDragAndDrop();

        align(Align.bottomLeft);
        setFillParent(true);

        inventoryRoot = new Table();
        inventoryRoot.setFillParent(true);
        inventoryRoot.setVisible(false);

        Table table1 = new Table();
        table1.setBackground(skin.getDrawable("Other_panel_brown"));
        Label label = new Label("Inventory", skin, "text_12");
        label.setColor(skin.getColor("BLACK"));
        table1.add(label).row();
        Table contentTable = new Table();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                int index = i * Constants.INVENTORY_COLUMNS + j;
                InventorySlot slot = new InventorySlot(index, skin, viewModel.getEventBus());
                this.slots[i][j] = slot;
                contentTable.add(slot);
            }
            contentTable.row();
        }
        table1.add(contentTable).pad(5.0f);
        inventoryRoot.add(table1);
        stage.addActor(inventoryRoot);

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.setName("lifeGroup");
        horizontalGroup.padLeft(5.0f);
        horizontalGroup.padBottom(5.0f);
        horizontalGroup.space(5.0f);
        add(horizontalGroup);

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                InventorySlot slot = slots[i][j];
                dragAndDrop.addTarget(new InventorySlotTarget(slot, viewModel.getEventBus()));
            }
        }
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.LIFE_POINTS_PC, Integer.class, this::updateLife);
        viewModel.onPropertyChange(Constants.PLAYER_DAMAGE_PC, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, Array.class, this::updatePlayerItems);
        viewModel.onPropertyChange(Constants.OPEN_INVENTORY, Boolean.class, this::setInventoryVisibility);
    }

    private void setInventoryVisibility(boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }

    private void updatePlayerItems(Array<ItemModel> array) {
        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                InventorySlot slot = slots[i][j];
                Actor actor = slot.findActor("item");
                if (actor != null) actor.remove();
                slot.setCount(0);
            }
        }

        for (ItemModel item : array) {
            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;

            InventorySlot slot = slots[row][col];
            Image itemImage = new Image(skin.getDrawable(item.getDrawableName()));
            itemImage.setName("item");
            itemImage.setScaling(Scaling.fit);
            itemImage.setFillParent(true);

            slot.add(itemImage);
            slot.setCount(item.getCount());
            slot.getCountTable().toFront();

            dragAndDrop.addSource(new InventoryItemSource(itemImage, item.getSlotIdx()));
        }
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

        TextraLabel textraLabel = new TypingLabel("[%50]{JUMP=2.0;0.5;0.9}{RAINBOW}" + damage, skin);
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
