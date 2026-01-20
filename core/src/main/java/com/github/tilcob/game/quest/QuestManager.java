package com.github.tilcob.game.quest;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

public class QuestManager {
    private static final String TAG = QuestManager.class.getSimpleName();

    private final QuestYarnRuntime questYarnRuntime;

    public QuestManager(QuestYarnRuntime questYarnRuntime) {
        this.questYarnRuntime = questYarnRuntime;
    }

    public void signal(String eventType, String target, int amount) {
        signal(null, eventType, target, amount);
    }

    public void signal(Entity player, String eventType, String target, int amount) {
        if (eventType == null || eventType.isBlank()) {
            return;
        }
        questYarnRuntime.setVariable(player, "$eventType", eventType);
        questYarnRuntime.setVariable(player, "$eventTarget", target);
        questYarnRuntime.setVariable(player, "$eventAmount", amount);

        boolean handled = false;
        if (player != null) {
            QuestLog questLog = QuestLog.MAPPER.get(player);
            if (questLog != null) {
                Array<Quest> quests = questLog.getQuests();
                for (int i = 0; i < quests.size; i++) {
                    Quest quest = quests.get(i);
                    if (quest == null || quest.isCompleted()) {
                        continue;
                    }
                    String nodeId = "q_" + quest.getQuestId() + "_on_" + eventType;
                    if (executeNode(player, nodeId)) {
                        handled = true;
                    }
                }
            }
        }
        if (!handled) {
            String fallbackNode = "on_" + eventType;
            if (questYarnRuntime.hasNode(fallbackNode)) {
                executeNode(player, fallbackNode);
            }
        }
    }

    private boolean executeNode(Entity player, String nodeId) {
        return questYarnRuntime.executeNode(player, nodeId);
    }
}
