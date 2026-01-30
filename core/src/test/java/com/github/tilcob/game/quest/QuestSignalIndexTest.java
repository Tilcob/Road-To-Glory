package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.test.HeadlessGdxTest;
import com.github.tilcob.game.yarn.QuestScriptStore;
import com.github.tilcob.game.yarn.YarnParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuestSignalIndexTest extends HeadlessGdxTest {

    @Test
    void questSignalNodes_areIndexed_andResolvable() {
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/index.json");
        registry.loadAll();
        QuestScriptStore store = new QuestScriptStore(registry);

        store.hasNode("__trigger_load__");

        Map<String, FileHandle> files = registry.getQuestFiles();
        assertFalse(files.isEmpty(), "Quest registry has no quest files. Is assets/quests/index.json present?");

        int checked = 0;

        for (Map.Entry<String, FileHandle> e : files.entrySet()) {
            String expectedQuestIdFromRegistry = e.getKey();
            FileHandle fh = e.getValue();
            assertNotNull(fh, "Quest filehandle is null for questId=" + expectedQuestIdFromRegistry);
            assertTrue(fh.exists(), "Quest file does not exist: " + fh.path());

            String content = fh.readString("UTF-8");
            List<YarnParser.YarnNodeRaw> rawNodes = YarnParser.parse(content);

            for (YarnParser.YarnNodeRaw raw : rawNodes) {
                String nodeId = raw.id();
                if (nodeId == null) continue;

                if (!nodeId.startsWith("q_")) continue;
                int onIndex = nodeId.indexOf("_on_");
                if (onIndex <= 2) continue;

                String questId = nodeId.substring(2, onIndex);
                String eventType = nodeId.substring(onIndex + 4);

                if (questId.isBlank() || eventType.isBlank()) continue;
                String resolved = store.getQuestSignalNodeId(questId, eventType.toLowerCase());

                assertEquals(
                    nodeId,
                    resolved,
                    () -> "QuestSignalIndex missing/incorrect for nodeId='" + nodeId + "' (file=" + fh.path() + ") " +
                        "resolved='" + resolved + "' questId='" + questId + "' eventType='" + eventType + "'"
                );

                assertEquals(
                    expectedQuestIdFromRegistry,
                    questId,
                    "Quest signal node '" + nodeId + "' is in wrong quest file: " + fh.path()
                );

                checked++;
            }
        }

        assertTrue(checked > 0,
            "No quest signal nodes matched pattern q_<questId>_on_<eventType>. " +
                "Either none exist, or the naming convention differs.");
    }
}
