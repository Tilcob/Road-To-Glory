package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogChoice {
    private String text;
    private Array<String> lines;
    private Array<DialogEffect> effects;
    private String next;

    public DialogChoice(String text, Array<String> lines) {
        this.text = text;
        this.lines = lines;
    }

    public DialogChoice(String text, Array<String> lines, Array<DialogEffect> effects, String next) {
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

    public Array<String> lines() {
        return lines;
    }

    public Array<DialogEffect> effects() {
        return effects;
    }

    public String next() {
        return next;
    }
}
