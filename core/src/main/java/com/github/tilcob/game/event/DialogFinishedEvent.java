package com.github.tilcob.game.event;


import com.badlogic.ashley.core.Entity;

public record DialogFinishedEvent(Entity npc, Entity player) {
}
