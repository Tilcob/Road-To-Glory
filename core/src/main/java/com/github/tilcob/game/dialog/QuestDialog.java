package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class QuestDialog {
    private String questId;
    private Array<String> notStarted;
    private Array<String> inProgress;
    private Array<String> completed;
    private ObjectMap<String, Array<String>> stepDialogs;
    private Array<DialogChoice> notStartedChoices;
    private Array<DialogChoice> inProgressChoices;
    private Array<DialogChoice> completedChoices;
    private ObjectMap<String, Array<DialogChoice>> stepChoices;

    public QuestDialog(String questId, Array<String> notStarted, Array<String> inProgress, Array<String> completed) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    public QuestDialog(String questId, Array<String> notStarted, Array<String> inProgress, Array<String> completed,
                       ObjectMap<String, Array<String>> stepDialogs) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
        this.stepDialogs = stepDialogs;
    }

    public QuestDialog(String questId, Array<String> notStarted, Array<String> inProgress, Array<String> completed,
                       ObjectMap<String, Array<String>> stepDialogs, Array<DialogChoice> notStartedChoices,
                       Array<DialogChoice> inProgressChoices, Array<DialogChoice> completedChoices,
                       ObjectMap<String, Array<DialogChoice>> stepChoices) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
        this.stepDialogs = stepDialogs;
        this.notStartedChoices = notStartedChoices;
        this.inProgressChoices = inProgressChoices;
        this.completedChoices = completedChoices;
        this.stepChoices = stepChoices;
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

    public Array<DialogChoice> notStartedChoices() {
        return notStartedChoices;
    }

    public Array<DialogChoice> inProgressChoices() {
        return inProgressChoices;
    }

    public Array<DialogChoice> completedChoices() {
        return completedChoices;
    }

    public ObjectMap<String, Array<DialogChoice>> stepChoices() {
        return stepChoices;
    }
}
