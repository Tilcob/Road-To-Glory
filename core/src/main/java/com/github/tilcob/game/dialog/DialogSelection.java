package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public record DialogSelection(Array<ScriptEvent> lines, Array<DialogChoice> choices) {
}
