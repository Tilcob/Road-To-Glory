package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public class QuestDialog {
    private String questId;
    private Array<ScriptEvent> notStarted;
    private Array<ScriptEvent> inProgress;
    private Array<ScriptEvent> completed;
    private ObjectMap<String, Array<ScriptEvent>> stepDialogs;
    private Array<DialogChoice> notStartedChoices;
    private Array<DialogChoice> inProgressChoices;
    private Array<DialogChoice> completedChoices;
    private ObjectMap<String, Array<DialogChoice>> stepChoices;

    public QuestDialog(String questId, Array<ScriptEvent> notStarted, Array<ScriptEvent> inProgress, Array<ScriptEvent> completed) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    public QuestDialog(String questId, Array<ScriptEvent> notStarted, Array<ScriptEvent> inProgress, Array<ScriptEvent> completed,
                       ObjectMap<String, Array<ScriptEvent>> stepDialogs) {
        this.questId = questId;
        this.notStarted = notStarted;
        this.inProgress = inProgress;
        this.completed = completed;
        this.stepDialogs = stepDialogs;
    }

    public QuestDialog(String questId, Array<ScriptEvent> notStarted, Array<ScriptEvent> inProgress, Array<ScriptEvent> completed,
                       ObjectMap<String, Array<ScriptEvent>> stepDialogs, Array<DialogChoice> notStartedChoices,
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

    public Array<ScriptEvent> notStarted() {
        return notStarted;
    }

    public Array<ScriptEvent> inProgress() {
        return inProgress;
    }

    public Array<ScriptEvent> completed() {
        return completed;
    }

    public ObjectMap<String, Array<ScriptEvent>> stepDialogs() {
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
