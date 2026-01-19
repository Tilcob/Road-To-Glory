package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class QuestYarnRegistry {
    private static final String TAG = QuestYarnRegistry.class.getSimpleName();
    private static final String QUESTS_DIR = "quests";

    private final String indexPath;
    private final Map<String, QuestDefinition> definitions = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

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

        try {
            JsonNode root = mapper.readTree(indexFile.readString());
            if (!root.isArray()) {
                Gdx.app.error(TAG, "Quest index must be a JSON array: " + indexFile.path());
                return Collections.unmodifiableMap(definitions);
            }
            for (JsonNode entry : root) {
                if (!entry.isTextual()) {
                    Gdx.app.error(TAG, "Invalid quest index entry: " + entry.toString());
                    continue;
                }
                loadQuestDefinition(resolveQuestFileFromEntry(entry.asText()));
            }
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest index: " + indexFile.path(), e);
        }
        return Collections.unmodifiableMap(definitions);
    }

    public QuestDefinition getQuestDefinition(String questId) {
        return definitions.get(questId);
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    private void loadQuestDefinition(FileHandle questFile) {
        if (!questFile.exists()) {
            Gdx.app.error(TAG, "Quest file not found: " + questFile.path());
            return;
        }
        try {
            JsonNode root = mapper.readTree(questFile.readString());
            if (!root.isObject()) {
                Gdx.app.error(TAG, "Quest definition must be a JSON object: " + questFile.path());
                return;
            }
            String questId = getText(root, "questId");
            if (questId == null || questId.isBlank()) {
                Gdx.app.error(TAG, "Quest entry missing questId: " + questFile.path());
                return;
            }
            if (definitions.containsKey(questId)) {
                Gdx.app.error(TAG, "Duplicate questId detected: " + questId);
                return;
            }
            String title = getText(root, "title");
            if (title == null || title.isBlank()) {
                Gdx.app.error(TAG, "Missing title for quest: " + questId);
                return;
            }
            String description = getText(root, "description");
            if (description == null || description.isBlank()) {
                Gdx.app.error(TAG, "Missing description for quest: " + questId);
                return;
            }
            String startNode = getText(root, "startNode");

            List<QuestDefinition.StepDefinition> steps = parseSteps(root.get("steps"));
            QuestDefinition.RewardDefinition reward = parseRewards(root.get("rewards"));

            QuestDefinition quest = new QuestDefinition(
                questId,
                title,
                description,
                startNode,
                List.copyOf(steps),
                reward
            );
            definitions.put(quest.questId(), quest);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest definition: " + questFile.path(), e);
        }
    }

    private List<QuestDefinition.StepDefinition> parseSteps(JsonNode stepsNode) {
        if (stepsNode == null || !stepsNode.isArray()) return List.of();

        List<QuestDefinition.StepDefinition> steps = new ArrayList<>();
        for (JsonNode stepNode : stepsNode) {
            if (!stepNode.isObject()) {
                Gdx.app.error(TAG, "Invalid quest step entry: " + stepNode.toString());
                continue;
            }
            String type = getText(stepNode, "type");
            if (type == null || type.isBlank()) {
                Gdx.app.error(TAG, "Quest step missing type: " + stepNode.toString());
                continue;
            }
            String normalizedType = type.toLowerCase(Locale.ROOT);
            switch (normalizedType) {
                case "talk" -> {
                    String npc = getText(stepNode, "npc");
                    if (npc == null || npc.isBlank()) {
                        Gdx.app.error(TAG, "Talk step missing npc: " + stepNode.toString());
                        continue;
                    }
                    steps.add(new QuestDefinition.StepDefinition("talk", npc, null, 0, null));
                }
                case "collect" -> {
                    String itemId = getText(stepNode, "itemId");
                    int amount = getInt(stepNode, "amount");
                    if (itemId == null || itemId.isBlank()) {
                        Gdx.app.error(TAG, "Collect step missing itemId: " + stepNode.toString());
                        continue;
                    }
                    steps.add(new QuestDefinition.StepDefinition("collect", null, itemId, amount, null));
                }
                case "kill" -> {
                    String enemy = getText(stepNode, "enemy");
                    int amount = getInt(stepNode, "amount");
                    if (enemy == null || enemy.isBlank()) {
                        Gdx.app.error(TAG, "Kill step missing enemy: " + stepNode.toString());
                        continue;
                    }
                    steps.add(new QuestDefinition.StepDefinition("kill", null, null, amount, enemy));
                }
                default -> Gdx.app.error(TAG, "Unknown step type: " + type);
            }
        }

        return steps;
    }

    private QuestDefinition.RewardDefinition parseRewards(JsonNode rewardsNode) {
        if (rewardsNode == null || !rewardsNode.isObject()) {
            return new QuestDefinition.RewardDefinition(0, List.of());
        }
        int money = getInt(rewardsNode, "money");
        List<String> items = new ArrayList<>();
        JsonNode itemsNode = rewardsNode.get("items");

        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                if (!itemNode.isTextual()) {
                    Gdx.app.error(TAG, "Invalid reward item entry: " + itemNode.toString());
                    continue;
                }
                items.add(itemNode.asText());
            }
        }

        return new QuestDefinition.RewardDefinition(money, items);
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && field.isTextual()) return field.asText();
        return null;
    }

    private int getInt(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && field.canConvertToInt()) return field.asInt();
        return 0;
    }

    private FileHandle resolveQuestFileFromEntry(String entry) {
        String fileName = ensureJsonExtension(entry);
        if (fileName.contains("/")) {
            return Gdx.files.internal(fileName);
        }
        return Gdx.files.internal(QUESTS_DIR + "/" + fileName);
    }

    private String ensureJsonExtension(String entry) {
        if (entry.toLowerCase().endsWith(".json")) {
            return entry;
        }
        return entry + ".json";
    }
}
