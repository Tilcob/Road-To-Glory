package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Counters;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.event.*;

public class GeneralCommandHandler {

    public GeneralCommandHandler(GameEventBus eventBus) {
        eventBus.subscribe(DialogSetFlagEvent.class, this::parseFlagEvent);
        eventBus.subscribe(DialogIncCounterEvent.class, this::parseCounterEvent);
        eventBus.subscribe(QuestSetFlagEvent.class, this::parseFlagEvent);
        eventBus.subscribe(QuestIncCounterEvent.class, this::parseCounterEvent);
        eventBus.subscribe(CutsceneSetFlagEvent.class, this::parseFlagEvent);
    }

    private void setFlag(Entity player, String flag, boolean value) {
        if (player == null ) return;
        if (flag == null || flag.isBlank()) return;
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        flags.set(flag, value);
    }

    private void incCounter(Entity player, String counter, int delta) {;
        if (player == null) return;
        if (counter == null || counter.isBlank()) return;
        Counters counters = Counters.MAPPER.get(player);
        if (counters == null) {
            counters = new Counters();
            player.add(counters);
        }
        counters.increment(counter, delta);
    }

    private <T> void parseFlagEvent(T event) {
        if (event instanceof DialogSetFlagEvent dialogEvent ) {
            setFlag(dialogEvent.player(), dialogEvent.flag(), dialogEvent.value());
        } else if (event instanceof QuestSetFlagEvent questEvent) {
            setFlag(questEvent.player(), questEvent.flag(), questEvent.value());
        } else if (event instanceof CutsceneSetFlagEvent cutsceneEvent) {
            setFlag(cutsceneEvent.player(), cutsceneEvent.flag(), cutsceneEvent.value());
        }
    }

    private <T> void parseCounterEvent(T event) {
        if (event instanceof DialogIncCounterEvent dialogEvent) {
            incCounter(dialogEvent.player(), dialogEvent.counter(), dialogEvent.delta());
        } else if (event instanceof QuestIncCounterEvent questEvent) {
            incCounter(questEvent.player(), questEvent.counter(), questEvent.delta());
        }
    }
}
