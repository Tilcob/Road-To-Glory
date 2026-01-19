package com.github.tilcob.game.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public abstract class HeadlessGdxTest {
    private static HeadlessApplication application;

    @BeforeAll
    static void initGdx() {
        if (application == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            application = new HeadlessApplication(new ApplicationAdapter() {}, config);
            if (ItemDefinitionRegistry.getAll().isEmpty()) {
                for (ItemDefinition definition : ItemLoader.loadAll()) {
                    ItemDefinitionRegistry.register(definition);
                }
            }
            ensureQuestIndex();
        }
    }

    @AfterAll
    static void teardownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }

    private static void ensureQuestIndex() {
        File questsDir = new File("assets/quests");
        File indexFile = new File(questsDir, "index.json");
        if (!questsDir.exists() || indexFile.exists()) {
            return;
        }
        File[] files = questsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yarn"));
        if (files == null) {
            return;
        }
        List<String> entries = Arrays.stream(files)
            .map(File::getName)
            .sorted()
            .toList();
        String json = "[\n" + entries.stream()
            .map(entry -> "  \"" + entry + "\"")
            .reduce((a, b) -> a + ",\n" + b)
            .orElse("") + "\n]\n";
        try {
            Files.writeString(indexFile.toPath(), json);
        } catch (IOException ignored) {
        }
    }
}
