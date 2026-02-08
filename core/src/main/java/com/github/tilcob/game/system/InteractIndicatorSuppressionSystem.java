package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.InteractIndicatorSuppression;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.input.Command;

public class InteractIndicatorSuppressionSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final ActiveEntityReference activeEntityReference;

    public InteractIndicatorSuppressionSystem(GameEventBus eventBus, ActiveEntityReference activeEntityReference) {
        this.eventBus = eventBus;
        this.activeEntityReference = activeEntityReference;

        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    private void onCommand(CommandEvent event) {
        if (event.getCommand() != Command.INTERACT) return;
        if (!event.isHandled()) return;
        if (activeEntityReference == null) return;

        Entity focused = activeEntityReference.getFocused();
        if (focused == null) return;
        if (OverheadIndicator.MAPPER.get(focused) == null) return;

        InteractIndicatorSuppression suppression = InteractIndicatorSuppression.MAPPER.get(focused);
        if (suppression == null) focused.add(new InteractIndicatorSuppression());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
