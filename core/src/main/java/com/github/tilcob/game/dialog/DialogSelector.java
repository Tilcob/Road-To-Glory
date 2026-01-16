package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class DialogSelector {

    public static DialogSelection select(DialogData dialogData, QuestLog questLog,
                                         DialogFlags dialogFlags, String npcName) {
        String firstContactFlag = firstContactFlagKey(npcName);
        if (firstContactFlag != null && dialogFlags != null && !dialogFlags.get(firstContactFlag)) {
            return new DialogSelection(dialogData.idle(), dialogData.choices());
        }

        DialogFlagDialog flagDialog = selectFlagDialog(dialogData.flagDialogs(), dialogFlags);
        if (flagDialog != null && flagDialog.lines() != null && flagDialog.lines().size > 0) {
            return new DialogSelection(flagDialog.lines(), new Array<>());
        }
        return select(dialogData.idle(), dialogData.choices(), dialogData.questDialog(), questLog);
    }

    public static DialogSelection select(Array<String> idle, Array<DialogChoice> rootChoices,
                                         QuestDialog questDialog, QuestLog questLog) {
        if (questDialog != null) {
            Quest quest = questLog.getQuestById(questDialog.questId());
            if (quest == null) {
                return new DialogSelection(idle, rootChoices);
            }

            QuestState state = questLog.getQuestStateById(questDialog.questId());

            return switch (state) {
                case NOT_STARTED -> new DialogSelection(questDialog.notStarted(), questDialog.notStartedChoices());
                case IN_PROGRESS -> selectInProgress(questDialog, questLog);
                case COMPLETED -> new DialogSelection(questDialog.completed(), questDialog.completedChoices());
            };
        }
        return new DialogSelection(idle, rootChoices);
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

    private static DialogFlagDialog selectFlagDialog(Array<DialogFlagDialog> flagDialogs, DialogFlags dialogFlags) {
        if (flagDialogs == null || dialogFlags == null) return null;
        for (DialogFlagDialog flagDialog : flagDialogs) {
            if (flagDialog == null) {
                continue;
            }
            String flag = flagDialog.flag();
            if (flag != null && dialogFlags.get(flag)) {
                return flagDialog;
            }
        }
        return null;
    }
}
