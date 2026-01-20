package com.github.tilcob.game.stat;

public record StatKey(String id) {
    public StatKey {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Stat key id must be non-empty.");
        }
    }
}
