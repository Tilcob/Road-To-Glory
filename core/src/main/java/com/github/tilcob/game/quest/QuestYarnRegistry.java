package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;

public class QuestYarnRegistry {
    private static final String TAG = QuestYarnRegistry.class.getSimpleName();
    private static final String NODE_SEPARATOR = "===";
    private static final String HEADER_SEPARATOR = "---";
    private static final String INDEX_NODE_TITLE = "quests_index";

    private final String indexPath;
    private final Map<String, QuestDefinition> definitions = new HashMap<>();

    public QuestYarnRegistry(String indexPath) {
        this.indexPath = indexPath;
    }

    public Map<String, QuestDefinition> loadAll() {
        definitions.clear();
        FileHandle indexFile = Gdx.files.internal(indexPath);
        if (!indexFile.exists()) {
            Gdx.app.error(TAG, "Quest index not found: " + indexPath);
            return Collections.unmodifiableMap(definitions);
        }
        ParsedNode indexNode = findNode(indexFile.readString("UTF-8"), INDEX_NODE_TITLE);
        if (indexNode == null) {
            Gdx.app.error(TAG, "Missing quests_index node in: " + indexPath);
            return Collections.unmodifiableMap(definitions);
        }
        parseIndexNode(indexNode.bodyLines);
        return Collections.unmodifiableMap(definitions);
    }

    public QuestDefinition getQuestDefinition(String questId) {
        return definitions.get(questId);
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    private void parseIndexNode(List<String> bodyLines) {
        QuestDefinitionBuilder current = null;

        for (String rawLine : bodyLines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                if (trimmed.isEmpty()) {
                    current = finalizeQuest(current);
                }
                continue;
            }

            if (trimmed.startsWith("questId:")) {
                current = finalizeQuest(current);
                current = new QuestDefinitionBuilder();
                current.questId = trimmed.substring("questId:".length()).trim();
                continue;
            }

            if (current == null) {
                Gdx.app.error(TAG, "Encountered quest metadata before questId: " + trimmed);
                continue;
            }

            if (trimmed.startsWith("displayName:")) {
                current.title = trimmed.substring("displayName:".length()).trim();
                continue;
            }
            if (trimmed.startsWith("journalText:")) {
                current.description = trimmed.substring("journalText:".length()).trim();
                continue;
            }
            if (trimmed.startsWith("startNode:")) {
                current.startNode = trimmed.substring("startNode:".length()).trim();
                continue;
            }
            if (trimmed.startsWith("step:")) {
                parseStep(trimmed.substring("step:".length()).trim(), current);
                continue;
            }
            if (trimmed.startsWith("reward.money:")) {
                current.rewardMoney = parseInt(trimmed.substring("reward.money:".length()).trim());
                continue;
            }
            if (trimmed.startsWith("reward.item:")) {
                String itemId = trimmed.substring("reward.item:".length()).trim();
                if (!itemId.isEmpty()) {
                    current.rewardItems.add(itemId);
                }
                continue;
            }

            Gdx.app.error(TAG, "Unrecognized quest index line: " + trimmed);
        }

        finalizeQuest(current);
    }

    private QuestDefinitionBuilder finalizeQuest(QuestDefinitionBuilder builder) {
        if (builder == null) {
            return null;
        }
        if (builder.questId == null || builder.questId.isBlank()) {
            Gdx.app.error(TAG, "Quest entry missing questId.");
            return null;
        }
        if (definitions.containsKey(builder.questId)) {
            Gdx.app.error(TAG, "Duplicate questId detected: " + builder.questId);
            return null;
        }
        if (builder.title == null || builder.title.isBlank()) {
            Gdx.app.error(TAG, "Missing displayName for quest: " + builder.questId);
        }
        if (builder.description == null || builder.description.isBlank()) {
            Gdx.app.error(TAG, "Missing journalText for quest: " + builder.questId);
        }
        if (builder.startNode == null || builder.startNode.isBlank()) {
            Gdx.app.error(TAG, "Missing startNode for quest: " + builder.questId);
        }

        QuestDefinition.RewardDefinition reward = new QuestDefinition.RewardDefinition(
            builder.rewardMoney,
            List.copyOf(builder.rewardItems)
        );
        QuestDefinition quest = new QuestDefinition(
            builder.questId,
            builder.title,
            builder.description,
            builder.startNode,
            List.copyOf(builder.steps),
            reward
        );
        definitions.put(quest.questId(), quest);
        return null;
    }

    private void parseStep(String payload, QuestDefinitionBuilder builder) {
        if (payload.isEmpty()) {
            Gdx.app.error(TAG, "Empty step definition.");
            return;
        }
        String[] parts = payload.split("\\s+");
        String type = parts[0].toLowerCase(Locale.ROOT);

        switch (type) {
            case "talk" -> {
                if (parts.length < 2) {
                    Gdx.app.error(TAG, "Talk step missing npc: " + payload);
                    return;
                }
                builder.steps.add(new QuestDefinition.StepDefinition("talk", parts[1], null, 0, null));
            }
            case "collect" -> {
                if (parts.length < 3) {
                    Gdx.app.error(TAG, "Collect step missing item/amount: " + payload);
                    return;
                }
                int amount = parseInt(parts[2]);
                builder.steps.add(new QuestDefinition.StepDefinition("collect", null, parts[1], amount, null));
            }
            case "kill" -> {
                if (parts.length < 3) {
                    Gdx.app.error(TAG, "Kill step missing enemy/amount: " + payload);
                    return;
                }
                int amount = parseInt(parts[2]);
                builder.steps.add(new QuestDefinition.StepDefinition("kill", null, null, amount, parts[1]));
            }
            default -> Gdx.app.error(TAG, "Unknown step type: " + payload);
        }
    }

    private int parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            Gdx.app.error(TAG, "Failed to parse integer: " + raw, ex);
            return 0;
        }
    }

    private ParsedNode findNode(String content, String nodeTitle) {
        String[] lines = content.split("\\R", -1);
        List<String> headerLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        boolean inBody = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (NODE_SEPARATOR.equals(trimmed)) {
                ParsedNode node = buildNode(headerLines, bodyLines);
                if (node != null && nodeTitle.equalsIgnoreCase(node.title)) {
                    return node;
                }
                headerLines.clear();
                bodyLines.clear();
                inBody = false;
                continue;
            }
            if (!inBody && HEADER_SEPARATOR.equals(trimmed)) {
                inBody = true;
                continue;
            }
            if (inBody) {
                bodyLines.add(line);
            } else if (!trimmed.isEmpty()) {
                headerLines.add(line);
            }
        }

        ParsedNode node = buildNode(headerLines, bodyLines);
        if (node != null && nodeTitle.equalsIgnoreCase(node.title)) {
            return node;
        }
        return null;
    }

    private ParsedNode buildNode(List<String> headerLines, List<String> bodyLines) {
        String title = null;
        for (String headerLine : headerLines) {
            String trimmed = headerLine.trim();
            String lower = trimmed.toLowerCase(Locale.ROOT);
            if (lower.startsWith("title:")) {
                title = trimmed.substring("title:".length()).trim();
                break;
            }
        }
        if (title == null || title.isBlank()) {
            return null;
        }
        return new ParsedNode(title, List.copyOf(bodyLines));
    }

    private static final class ParsedNode {
        private final String title;
        private final List<String> bodyLines;

        private ParsedNode(String title, List<String> bodyLines) {
            this.title = title;
            this.bodyLines = bodyLines;
        }
    }

    private static final class QuestDefinitionBuilder {
        private String questId;
        private String title;
        private String description;
        private String startNode;
        private int rewardMoney = 0;
        private final List<String> rewardItems = new ArrayList<>();
        private final List<QuestDefinition.StepDefinition> steps = new ArrayList<>();
    }
}
