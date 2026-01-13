package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.dialog.DialogChoice;

public record DialogChoiceEvent(Array<DialogChoice> choices, int selectedIndex, Entity npc) {
}
