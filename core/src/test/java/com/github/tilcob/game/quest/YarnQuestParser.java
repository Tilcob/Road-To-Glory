package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class YarnQuestParserTest extends HeadlessGdxTest {

    @Test
    void parsesRewardHeadersWithDots() throws IOException {
        String content = """
            questId: reward_headers
            displayName: Reward Headers
            journalText: Verify dot headers.
            startNode: q_reward_headers_start
            reward_money: 25
            reward_item: sword
            reward_items: shield, potion
            title: q_reward_headers_start
            ---
            <<quest_start reward_headers>>
            ===
            """;

        Path tempFile = Files.createTempFile("quest-reward-headers", ".yarn");
        Files.writeString(tempFile, content);

        FileHandle handle = Gdx.files.absolute(tempFile.toAbsolutePath().toString());
        QuestDefinition definition = new YarnQuestParser().parse(handle);

        assertNotNull(definition);
        assertEquals("reward_headers", definition.questId());
        assertEquals(25, definition.reward().money());
        assertTrue(definition.reward().items().contains("sword"));
        assertTrue(definition.reward().items().contains("shield"));
        assertTrue(definition.reward().items().contains("potion"));
    }
}
