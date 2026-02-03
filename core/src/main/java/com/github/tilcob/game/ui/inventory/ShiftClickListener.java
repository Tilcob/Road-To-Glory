package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.tilcob.game.input.InputService;

public class ShiftClickListener extends ClickListener {
    private final ShiftClickHandler handler;
    private final ShiftClickContext context;

    public ShiftClickListener(ShiftClickHandler handler, ShiftClickContext context) {
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        if (!InputService.isShiftPressed()) {
            return;
        }
        Actor listenerActor = event.getListenerActor();
        if (listenerActor == null) {
            return;
        }
        Object payload = listenerActor.getUserObject();
        if (!(payload instanceof ShiftClickPayload shiftPayload)) {
            return;
        }
        event.stop();
        handler.handleShiftClick(shiftPayload.item(), shiftPayload.sourceContext(), context);
    }
}
