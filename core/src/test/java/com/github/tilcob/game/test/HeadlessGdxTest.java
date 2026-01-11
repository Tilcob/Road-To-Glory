package com.github.tilcob.game.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class HeadlessGdxTest {
    private static HeadlessApplication application;

    @BeforeAll
    static void initGdx() {
        if (application == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        }
    }

    @AfterAll
    static void teardownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
}
