package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.DialogSession;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.DialogAdvanceEvent;
import com.github.tilcob.game.event.DialogChoiceSelectEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.Command;

public class DialogSessionSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;

    public DialogSessionSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    private void onCommand(CommandEvent event) {
        if (event.isHandled()) return;
        if (event.getCommand() != Command.INTERACT) return;
        DialogSession dialogSession = DialogSession.MAPPER.get(event.getPlayer());
        if (dialogSession == null) return;

        event.handle();
        if (dialogSession.isAwaitingChoice()) {
            eventBus.fire(new DialogChoiceSelectEvent(event.getPlayer()));
        } else {
            eventBus.fire(new DialogAdvanceEvent(event.getPlayer()));
        }

    }


    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
