package com.github.tilcob.game.quest;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogNode;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

import java.util.Map;

public class QuestManager {
    private static final String TAG = QuestManager.class.getSimpleName();

    private final QuestYarnRuntime questYarnRuntime;
    private final Map<String, DialogData> allDialogs;

    public QuestManager(QuestYarnRuntime questYarnRuntime, Map<String, DialogData> allDialogs) {
        this.questYarnRuntime = questYarnRuntime;
        this.allDialogs = allDialogs;
    }

    public void signal(String eventType, String target, int amount) {
        signal(null, eventType, target, amount);
    }

    public void signal(Entity player, String eventType, String target, int amount) {
        if (eventType == null || eventType.isBlank()) {
            return;
        }
        questYarnRuntime.setVariable("$eventType", eventType);
        questYarnRuntime.setVariable("$eventTarget", target);
        questYarnRuntime.setVariable("$eventAmount", amount);

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
            executeNode(player, "on_" + eventType);
        }
    }

    private boolean executeNode(Entity player, String nodeId) {
        DialogNode node = findNode(nodeId);
        if (node == null) return false;
        Array<String> lines = node.lines();
        if (lines == null) return false;
        boolean shouldExecute = true;
        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (isIfLine(trimmed)) {
                shouldExecute = evaluateCondition(trimmed);
                continue;
            }
            if (isEndIfLine(trimmed)) {
                shouldExecute = true;
                continue;
            }
            if (!shouldExecute) continue;
            questYarnRuntime.executeCommandLine(player, line);
        }
        return true;
    }

    private boolean isIfLine(String line) {
        return line.startsWith("<<if ") && line.endsWith(">>");
    }

    private boolean isEndIfLine(String line) {
        return line.equals("<<endif>>");
    }

    private boolean evaluateCondition(String line) {
        String inner = line.substring(2, line.length() - 2).trim();
        String condition = inner.substring("if ".length()).trim();
        String[] parts = condition.split("==", 2);
        if (parts.length != 2) return false;
        String left = parts[0].trim();
        String right = parts[1].trim();
        if (right.startsWith("\"") && right.endsWith("\"") && right.length() >= 2) {
            right = right.substring(1, right.length() - 1);
        }
        Object value = questYarnRuntime.getVariable(left);
        return value != null && right.equals(String.valueOf(value));
    }

    private DialogNode findNode(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) return null;
        for (DialogData dialogData : allDialogs.values()) {
            if (dialogData == null) continue;
            ObjectMap<String, DialogNode> nodes = dialogData.nodesById();
            if (nodes == null || nodes.isEmpty()) continue;
            DialogNode node = nodes.get(nodeId);
            if (node != null) return node;
        }
        if (Gdx.app != null) Gdx.app.debug(TAG, "Quest Yarn node not found: " + nodeId);
        return null;
    }
}
