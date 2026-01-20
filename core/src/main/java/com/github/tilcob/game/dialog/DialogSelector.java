package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class DialogSelector {

    public static DialogSelection select(DialogData dialogData, QuestLog questLog, DialogFlags dialogFlags) {
        QuestDialog questDialog = dialogData.questDialog();
        Array<String> rootLines = dialogData.rootLines();
        if (rootLines == null || rootLines.size == 0) rootLines = dialogData.idle();

        if (questDialog != null && questLog != null) {
            return select(rootLines, dialogData.idle(), dialogData.choices(), questDialog, questLog, dialogFlags);
        }

        return new DialogSelection(rootLines, dialogData.choices());
    }

    public static DialogSelection select(Array<String> rootLines, Array<String> idle,
                                         Array<DialogChoice> rootChoices,
                                         QuestDialog questDialog, QuestLog questLog, DialogFlags dialogFlags) {
        if (questDialog != null) {
            Quest quest = questLog.getQuestById(questDialog.questId());
            if (quest == null) return new DialogSelection(rootLines, rootChoices);

            QuestState state = questLog.getQuestStateById(questDialog.questId());

            return switch (state) {
                case NOT_STARTED -> new DialogSelection(questDialog.notStarted(), questDialog.notStartedChoices());
                case IN_PROGRESS -> selectInProgress(questDialog, questLog);
                case COMPLETED -> selectCompleted(rootLines, idle, rootChoices, questDialog, dialogFlags);
            };
        }
        return new DialogSelection(rootLines, rootChoices);
    }

    private static DialogSelection selectInProgress(QuestDialog questDialog, QuestLog questLog) {
        Quest quest = questLog.getQuestById(questDialog.questId());
        if (quest == null) return new DialogSelection(questDialog.inProgress(), questDialog.inProgressChoices());

        DialogSelection stepDialog = selectStepDialog(questDialog, quest.getCurrentStep());
        if (stepDialog != null && stepDialog.lines() != null && stepDialog.lines().size > 0) return stepDialog;
        return new DialogSelection(questDialog.inProgress(), questDialog.inProgressChoices());
    }

    public static String firstContactFlagKey(String npcName) {
        if (npcName == null || npcName.isBlank()) {
            return null;
        }
        String normalizedName = npcName.trim().toLowerCase().replaceAll("\\s+", "_");
        return "npc_" + normalizedName + "_root_shown";
    }

    public static String completedDialogFlagKey(String questId) {
        if (questId == null || questId.isBlank()) {
            return null;
        }
        String normalizedId = questId.trim().toLowerCase().replaceAll("\\s+", "_");
        return "quest_" + normalizedId + "_completed_seen";
    }

    private static DialogSelection selectStepDialog(QuestDialog questDialog, int stepIndex) {
        if (questDialog.stepDialogs() == null) {
            return null;
        }
        String key = String.valueOf(stepIndex);
        Array<String> lines = questDialog.stepDialogs().get(key);
        if (lines == null) return null;

        Array<DialogChoice> choices = questDialog.stepChoices() == null ? null : questDialog.stepChoices().get(key);
        return new DialogSelection(lines, choices);
    }

    private static DialogSelection selectCompleted(Array<String> rootLines, Array<String> idle,
                                                  Array<DialogChoice> rootChoices,
                                                  QuestDialog questDialog, DialogFlags dialogFlags) {
        String flagKey = completedDialogFlagKey(questDialog.questId());
        boolean completedSeen = flagKey != null && dialogFlags != null && dialogFlags.get(flagKey);
        if (completedSeen) {
            Array<String> idleLines = (idle == null || idle.size == 0) ? rootLines : idle;
            return new DialogSelection(idleLines, new Array<>());
        }
        return new DialogSelection(questDialog.completed(), questDialog.completedChoices());
    }
}
