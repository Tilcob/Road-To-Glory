package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public class DialogData {
    private Array<ScriptEvent> idle;
    private Array<ScriptEvent> rootLines;
    private Array<DialogChoice> choices;
    private Array<DialogFlagDialog> flagDialogs;
    private QuestDialog questDialog;
    private Array<DialogNode> nodes;
    private ObjectMap<String, DialogNode> nodesById;

    public DialogData(Array<ScriptEvent> idle, Array<ScriptEvent> rootLines, Array<DialogChoice> choices,
                      Array<DialogFlagDialog> flagDialogs, QuestDialog questDialog, Array<DialogNode> nodes,
                      ObjectMap<String, DialogNode> nodesById) {
        this.idle = idle;
        this.choices = choices;
        this.rootLines = rootLines;
        this.flagDialogs = flagDialogs;
        this.questDialog = questDialog;
        this.nodes = nodes;
        this.nodesById = nodesById;
    }

    public DialogData() {
    }

    public static DialogData empty() {
        return new DialogData();
    }

    public Array<ScriptEvent> idle() {
        return idle;
    }

    public Array<ScriptEvent> rootLines() {
        return rootLines;
    }

    public Array<DialogChoice> choices() {
        return choices;
    }

    public Array<DialogFlagDialog> flagDialogs() {
        return flagDialogs;
    }

    public QuestDialog questDialog() {
        return questDialog;
    }

    public Array<DialogNode> getNodes() {
        return nodes;
    }

    public ObjectMap<String, DialogNode> nodesById() {
        return nodesById;
    }
}
