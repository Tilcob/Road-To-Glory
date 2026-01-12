package com.github.tilcob.game.input;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

/**
 * Central input routing for the game.
 * <p>
 * Only active screens (e.g. {@code MenuScreen}, {@code GameScreen}) should call
 * {@link #setInputProcessors(InputProcessor...)} from their lifecycle methods like {@code show()}.
 * Controller states should only update their internal state and must not add or remove input
 * processors.
 */
public class InputManager {
    private final InputMultiplexer inputMultiplexer;

    public InputManager(InputMultiplexer inputMultiplexer) {
        this.inputMultiplexer = inputMultiplexer;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if (processors == null) {
            return;
        }

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }
}
