package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

public class InventorySlot extends Stack {
    private final int row;
    private final int col;
    private final Label countLabel;

    public InventorySlot(int row, int col, Skin skin) {
        this.row = row;
        this.col = col;
        this.countLabel = new Label("", skin, "text_10");
        countLabel.setColor(skin.getColor("BLACK"));
        setTouchable(Touchable.enabled);
    }

    public void setCount(int count) {
        if (count > 1) {
            countLabel.setText(Integer.toString(count));
            countLabel.setVisible(true);
        } else {
            countLabel.setVisible(false);
        }
    }

    public int getIndex(int columns) {
        return row * columns + col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
