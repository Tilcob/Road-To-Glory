package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

public class EquipmentSlot extends Stack {
    private final Skin skin;

    public EquipmentSlot(Skin skin) {
        this.skin = skin;
        setupUi();
    }

    private void setupUi() {
        setTouchable(Touchable.enabled);

        Image image = new Image(skin.getDrawable("Other_panel_border_brown_detail"));
        add(image);
    }
}
