package com.github.tilcob.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputService {
    private InputService() {
    }

    public static boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }
}
