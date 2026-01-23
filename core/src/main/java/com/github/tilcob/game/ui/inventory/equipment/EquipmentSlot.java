package com.github.tilcob.game.ui.inventory.equipment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.github.tilcob.game.event.GameEventBus;

public class EquipmentSlot extends Stack {
    private final Skin skin;

    public EquipmentSlot(Skin skin, GameEventBus eventBus) {
        this.skin = skin;
        setupUi();
        setupInput();
    }

    private void setupUi() {
        setTouchable(Touchable.enabled);

        Image image = new Image(skin.getDrawable("Other_panel_border_brown_detail"));
        add(image);
    }

    private void setupInput() {
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    Gdx.app.log("EquipmentSlot", "CTRL + LEFT CLICK");
                    return true;
                }
                return false;
            }
        });
    }
}
