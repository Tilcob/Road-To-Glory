package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.dialog.DialogChoice;

public class DialogSession implements Component {
    public static final ComponentMapper<DialogSession> MAPPER = ComponentMapper.getFor(DialogSession.class);

    private final Entity npc;
    private Array<String> lines;
    private Array<DialogChoice> choices;
    private int index;
    private int choiceIndex;
    private boolean awaitingChoice;
    private final boolean repeatableChoices;
    private boolean choiceConsumed;

    public DialogSession(Entity npc, Array<String> lines, Array<DialogChoice> choices, boolean repeatableChoices) {
        this.npc = npc;
        this.lines = lines == null ? new Array<>() : lines;
        this.choices = choices == null ? new Array<>() : new Array<>(choices);
        this.index = 0;
        this.choiceIndex = 0;
        this.awaitingChoice = false;
        this.repeatableChoices = repeatableChoices;
        this.choiceConsumed = false;
    }

    public Entity getNpc() {
        return npc;
    }

    public String currentLine() {
        if (lines.isEmpty()) {
            return "";
        }
        return lines.get(index);
    }

    public boolean hasLines() {
        return !lines.isEmpty();
    }

    public boolean advance() {
        index++;
        return index < lines.size;
    }

    public boolean hasChoices() {
        return choices != null && choices.size > 0;
    }

    public boolean isAwaitingChoice() {
        return awaitingChoice;
    }

    public void beginChoice() {
        if (!hasChoices() || choiceConsumed) {
            return;
        }
        awaitingChoice = true;
        choiceIndex = 0;
    }

    public void moveChoice(int delta) {
        if (!awaitingChoice || choices.isEmpty()) {
            return;
        }
        choiceIndex = MathUtils.clamp(choiceIndex + delta, 0, choices.size - 1);
    }

    public DialogChoice selectChoice() {
        if (!awaitingChoice || choices.isEmpty()) {
            return null;
        }
        DialogChoice choice = choices.get(choiceIndex);
        awaitingChoice = false;
        choiceConsumed = true;
        if (!repeatableChoices) choices.clear();
        return choice;
    }

    public boolean hasRemainingChoices() {
        return !choiceConsumed && hasChoices();
    }

    public Array<DialogChoice> getChoices() {
        return choices;
    }

    public int getChoiceIndex() {
        return choiceIndex;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return lines.size;
    }

    public void setLines(Array<String> lines) {
        this.lines = lines == null ? new Array<>() : lines;
        this.index = 0;
    }
}
