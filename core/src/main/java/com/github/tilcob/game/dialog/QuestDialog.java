package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class QuestDialog {
    private String questId;
    private Array<String> notStarted;
    private Array<String> inProgress;
    private Array<String> completed;
    private ObjectMap<String, Array<String>> stepDialogs;

    public QuestDialog(String questId, Array<String> notStarted, Array<String> inProgress, Array<String> completed) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    public QuestDialog() {}

    public String questId() {
        return questId;
    }

    public Array<String> notStarted() {
        return notStarted;
    }

    public Array<String> inProgress() {
        return inProgress;
    }

    public Array<String> completed() {
        return completed;
    }

    public ObjectMap<String, Array<String>> stepDialogs() {
        return stepDialogs;
    }
}
