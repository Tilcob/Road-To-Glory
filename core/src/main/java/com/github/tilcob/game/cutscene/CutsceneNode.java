package com.github.tilcob.game.cutscene;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public record CutsceneNode(String id, Array<ScriptEvent> scriptEvents) {
}
