package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.SplitStackEvent;

public class InventorySlot extends Stack {
    private final int slotIndex;
    private final Skin skin;
    private final Label countLabel;
    private final GameEventBus eventBus;
    private Table countTable;

    public InventorySlot(int slotIndex, Skin skin, GameEventBus eventBus) {
        this.slotIndex = slotIndex;
        this.skin = skin;
        this.eventBus = eventBus;

        setupUi();
        setupInput();
        this.countLabel = findActor("countLabel");
    }

    private void setupInput() {
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    eventBus.fire(new SplitStackEvent(slotIndex));
                    return true;
                }
                return false;
            }
        });

    }

    private void setupUi() {
        setTouchable(Touchable.enabled);

        Image image = new Image(skin.getDrawable("Other_panel_border_brown_detail"));
        add(image);

        Label label = new Label("", skin, "text_08");
        label.setName("countLabel");
        label.setColor(skin.getColor("BLACK"));
        label.setVisible(false);

        countTable = new Table();
        countTable.setFillParent(true);
        countTable.add(label).expand().bottom().right().padRight(2).padBottom(2);

        add(countTable);
    }

    public void setCount(int count) {
        if (count > 1) {
            countLabel.setText(Integer.toString(count));
            countLabel.setVisible(true);
        } else {
            countLabel.setVisible(false);
        }
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public Table getCountTable() {
        return countTable;
    }
}
