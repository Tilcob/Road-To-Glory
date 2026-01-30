package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public class DialogChoice {
    private String text;
    private Array<ScriptEvent> lines;
    private Array<DialogEffect> effects;
    private String next;

    public DialogChoice(String text, Array<ScriptEvent> lines) {
        this.text = text;
        this.lines = lines;
    }

    public DialogChoice(String text, Array<ScriptEvent> lines, Array<DialogEffect> effects, String next) {
        this.text = text;
        this.lines = lines;
        this.effects = effects;
        this.next = next;
    }

    public DialogChoice() {
    }

    public String text() {
        return text;
    }

    public Array<ScriptEvent> lines() {
        return lines;
    }

    public Array<DialogEffect> effects() {
        return effects;
    }

    public String next() {
        return next;
    }
}
