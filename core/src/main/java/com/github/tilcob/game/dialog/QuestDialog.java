package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class QuestDialog {
    private String questId;
    private Array<String> notStarted;
    private Array<String> inProgress;
    private Array<String> completed;

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
}
