package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class DialogSelector {

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
}
