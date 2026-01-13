package com.github.tilcob.game;

import com.github.tilcob.game.config.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeTest {
    @Test
    void constantsAreConfigured() {
        assertTrue(Constants.WIDTH > 0);
        assertTrue(Constants.HEIGHT > 0);
    }
}
