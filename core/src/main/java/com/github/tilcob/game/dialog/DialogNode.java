package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogNode {
    private String id;
    private Array<String> lines;
    private Array<DialogChoice> choices;

    public DialogNode(String id, Array<String> lines, Array<DialogChoice> choices) {
        this.id = id;
        this.lines = lines;
        this.choices = choices;
    }

    public DialogNode() {
    }

    public String id() {
        return id;
    }

    public Array<String> lines() {
        return lines;
    }

    public Array<DialogChoice> choices() {
        return choices;
    }
}
