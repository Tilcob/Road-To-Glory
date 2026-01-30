package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.event.QuestStepEvent;
import com.github.tilcob.game.yarn.script.ScriptEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class YarnDialogLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsTaggedNodesIntoDialogData() throws IOException {
        String yarn = """
            title: Start
            tags: root
            ---
            Welcome, traveler!
            -> Show me your wares.
                <<jump shop_wares>>
                I'm still setting up.
            ===

            title: Idle
            tags: idle
            ---
            Welcome in my shop!
            ===

            title: shop_wares
            ---
            Come back soon!
            ===
            """;
        FileHandle fileHandle = writeTempYarn("shopkeeper", yarn);
        YarnDialogLoader loader = new YarnDialogLoader();

        DialogData dialogData = loader.load(fileHandle);

        assertNotNull(dialogData);
        assertEquals(1, dialogData.idle().size);
        assertEquals("Welcome in my shop!", firstText(dialogData.idle()));
        assertEquals(1, dialogData.rootLines().size);
        assertEquals("Welcome, traveler!", firstText(dialogData.rootLines()));
        assertEquals(1, dialogData.choices().size);
        DialogChoice choice = dialogData.choices().first();
        assertEquals("Show me your wares.", choice.text());
        assertEquals("shop_wares", choice.next());
        assertEquals(1, choice.lines().size);
        assertEquals("I'm still setting up.", firstText(choice.lines()));
        assertEquals(2, dialogData.getNodes().size);
        DialogNode node = dialogData.getNodes().first();
        assertEquals("Idle", node.id());
        assertEquals(1, node.events().size);
        assertEquals("Welcome in my shop!", firstText(node.events()));
    }

    @Test
    void parsesChoiceEffectsFromCommands() throws IOException {
        String yarn = """
            title: Start
            tags: root
            ---
            Welcome, traveler!
            -> Any work for me?
                <<set_flag shop_seen true>>
                <<add_quest Welcome_To_Town>>
                <<quest_step TALK Npc-2>>
                <<goto next_node>>
            ===
            """;
        FileHandle fileHandle = writeTempYarn("quests", yarn);
        YarnDialogLoader loader = new YarnDialogLoader();

        DialogData dialogData = loader.load(fileHandle);

        assertEquals(1, dialogData.choices().size);
        DialogChoice choice = dialogData.choices().first();
        assertEquals("next_node", choice.next());
        Array<DialogEffect> effects = choice.effects();
        assertNotNull(effects);
        assertEquals(3, effects.size);
        assertEquals(DialogEffect.EffectType.SET_FLAG, effects.get(0).type());
        assertEquals("shop_seen", effects.get(0).flag());
        assertEquals(Boolean.TRUE, effects.get(0).value());
        assertEquals(DialogEffect.EffectType.ADD_QUEST, effects.get(1).type());
        assertEquals("Welcome_To_Town", effects.get(1).questId());
        assertEquals(DialogEffect.EffectType.QUEST_STEP, effects.get(2).type());
        assertEquals(QuestStepEvent.Type.TALK, effects.get(2).stepType());
        assertEquals("Npc-2", effects.get(2).target());
    }

    @Test
    void parsesQuestAndFlagDialogsFromTags() throws IOException {
        String yarn = """
            title: Idle
            tags: idle
            ---
            Idle line.
            ===

            title: quest_notStarted
            tags: quest_Welcome_To_Town, notstarted
            ---
            Welcome in our town!
            ===

            title: quest_inProgress
            tags: quest_Welcome_To_Town, inprogress
            ---
            Are you new here?
            ===

            title: quest_completed
            tags: quest_Welcome_To_Town, completed
            ---
            By my friend!
            ===

            title: flag_shop_seen
            tags: flag_shop_seen
            ---
            Good to see you again.
            ===
            """;
        FileHandle fileHandle = writeTempYarn("npc", yarn);
        YarnDialogLoader loader = new YarnDialogLoader();

        DialogData dialogData = loader.load(fileHandle);

        assertNotNull(dialogData.questDialog());
        assertEquals("welcome_to_town", dialogData.questDialog().questId());
        assertEquals("Welcome in our town!", firstText(dialogData.questDialog().notStarted()));
        assertEquals("Are you new here?", firstText(dialogData.questDialog().inProgress()));
        assertEquals("By my friend!", firstText(dialogData.questDialog().completed()));
        assertNotNull(dialogData.flagDialogs());
        assertEquals(1, dialogData.flagDialogs().size);
        assertEquals("shop_seen", dialogData.flagDialogs().first().flag());
        assertEquals("Good to see you again.", firstText(dialogData.flagDialogs().first().lines()));
    }

    private FileHandle writeTempYarn(String name, String content) throws IOException {
        Path file = tempDir.resolve(name + ".yarn");
        Files.writeString(file, content);
        return new FileHandle(file.toFile());
    }

    private static String textOf(ScriptEvent e) {
        assertTrue(e instanceof ScriptEvent.Text, "Expected ScriptEvent.Text but was: " + e);
        return ((ScriptEvent.Text) e).text();
    }

    private static String firstText(Array<ScriptEvent> events) {
        return textOf(events.first());
    }
}
