package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.dialog.DialogChoice;
import com.github.tilcob.game.dialog.DialogNode;

public class DialogSession implements Component {
    public static final ComponentMapper<DialogSession> MAPPER = ComponentMapper.getFor(DialogSession.class);

    private final Entity npc;
    private String currentNodeId;
    private int lineIndex;
    private int choiceIndex;
    private boolean awaitingChoice;
    private boolean choiceConsumed;

    public DialogSession(Entity npc) {
        this.npc = npc;
        this.currentNodeId = null;
        this.lineIndex = 0;
        this.choiceIndex = 0;
        this.awaitingChoice = false;
        this.choiceConsumed = false;
    }

    public Entity getNpc() {
        return npc;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public int getChoiceIndex() {
        return choiceIndex;
    }

    public void setChoiceIndex(int choiceIndex) {
        this.choiceIndex = choiceIndex;
    }

    public boolean isAwaitingChoice() {
        return awaitingChoice;
    }

    public void setAwaitingChoice(boolean awaitingChoice) {
        this.awaitingChoice = awaitingChoice;
    }

    public boolean isChoiceConsumed() {
        return choiceConsumed;
    }

    public void setChoiceConsumed(boolean choiceConsumed) {
        this.choiceConsumed = choiceConsumed;
    }
}
