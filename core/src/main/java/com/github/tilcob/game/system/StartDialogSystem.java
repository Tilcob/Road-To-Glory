package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Dialog;
import com.github.tilcob.game.component.PlayerReference;
import com.github.tilcob.game.component.StartDialogRequest;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.Command;

public class StartDialogSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;

    public StartDialogSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    private void onCommand(CommandEvent event) {
        if (event.isHandled()) return;
        if (event.getCommand() != Command.INTERACT) return;
        StartDialogRequest startDialogRequest = StartDialogRequest.MAPPER.get(event.getPlayer());
        if (startDialogRequest == null) return;
        event.handle();

        Entity npc = startDialogRequest.getNpc();
        PlayerReference.MAPPER.get(npc).setPlayer(event.getPlayer());
        Dialog.MAPPER.get(npc).setState(Dialog.State.REQUEST);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
