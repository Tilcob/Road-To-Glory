package com.github.tilcob.game.dialog;

import com.github.tilcob.game.yarn.script.ScriptEvent;

public record DialogLine(ScriptEvent text, int index, int total) {
}
