package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public class DialogNode {
    private String id;
    private Array<ScriptEvent> events;
    private Array<DialogChoice> choices;

    public DialogNode(String id, Array<ScriptEvent> events, Array<DialogChoice> choices) {
        this.id = id;
        this.events = events;
        this.choices = choices;
    }

    public DialogNode() {
    }

    public String id() {
        return id;
    }

    public Array<ScriptEvent> events() {
        return events;
    }

    public Array<DialogChoice> choices() {
        return choices;
    }
}
