package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.quest.QuestState;

public class DialogSelector {

    public static Array<String> select(Array<String> idle, QuestDialog questDialog, QuestLog questLog) {
        if (questDialog != null) {
            QuestState state = questLog.getQuestStateById(questDialog.questId());

            return switch (state) {
                case NOT_STARTED -> questDialog.notStarted();
                case IN_PROGRESS -> questDialog.inProgress();
                case COMPLETED -> questDialog.completed();
            };
        }
        return idle;
    }
}
