package com.github.tilcob.game.save;

public record SaveSlotInfo(SaveSlot slot, boolean exists, long lastModified) {
}
