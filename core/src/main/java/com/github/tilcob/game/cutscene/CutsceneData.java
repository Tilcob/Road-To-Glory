package com.github.tilcob.game.cutscene;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public record CutsceneData(String cutsceneId, Array<ScriptEvent> scriptEvents, ObjectMap<String, CutsceneNode> nodesById) {
    public static CutsceneData empty(String cutsceneId) {
        return new CutsceneData(cutsceneId, new Array<>(), new ObjectMap<>());
    }
}
