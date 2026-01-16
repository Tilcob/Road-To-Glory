package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public record DialogSelection(Array<String> lines, Array<DialogChoice> choices) {
}
