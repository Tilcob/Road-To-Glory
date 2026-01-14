package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class DialogSelector {

    public static Array<String> select(DialogData dialogData, QuestLog questLog, DialogFlags dialogFlags) {
        Array<String> flagDialog = selectFlagDialog(dialogData.flagDialogs(), dialogFlags);
        if (flagDialog != null && flagDialog.size > 0) {
            return flagDialog;
        }
        return select(dialogData.idle(), dialogData.questDialog(), questLog);
    }

    public static Array<String> select(Array<String> idle, QuestDialog questDialog, QuestLog questLog) {
        if (questDialog != null) {
            QuestState state = questLog.getQuestStateById(questDialog.questId());

            return switch (state) {
                case NOT_STARTED -> questDialog.notStarted();
                case IN_PROGRESS -> selectInProgress(questDialog, questLog);
                case COMPLETED -> questDialog.completed();
            };
        }
        return idle;
    }

    private static Array<String> selectInProgress(QuestDialog questDialog, QuestLog questLog) {
        Quest quest = questLog.getQuestById(questDialog.questId());
        if (quest == null) {
            return questDialog.inProgress();
        }
        Array<String> stepDialog = selectStepDialog(questDialog, quest.getCurrentStep());
        return stepDialog != null && stepDialog.size > 0 ? stepDialog : questDialog.inProgress();
    }

    private static Array<String> selectStepDialog(QuestDialog questDialog, int stepIndex) {
        if (questDialog.stepDialogs() == null) {
            return null;
        }
        String key = String.valueOf(stepIndex);
        return questDialog.stepDialogs().get(key);
    }

    private static Array<String> selectFlagDialog(Array<DialogFlagDialog> flagDialogs, DialogFlags dialogFlags) {
        if (flagDialogs == null || dialogFlags == null) {
            return null;
        }
        for (DialogFlagDialog flagDialog : flagDialogs) {
            if (flagDialog == null) {
                continue;
            }
            String flag = flagDialog.flag();
            if (flag != null && dialogFlags.get(flag)) {
                return flagDialog.lines();
            }
        }
        return null;
    }
}
