package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.InteractIndicatorSuppression;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.config.Constants;

public class InteractIndicatorSuppressionSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final ActiveEntityReference activeEntityReference;
    private ImmutableArray<Entity> entities;

    public InteractIndicatorSuppressionSystem(GameEventBus eventBus, ActiveEntityReference activeEntityReference) {
        this.eventBus = eventBus;
        this.activeEntityReference = activeEntityReference;
        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    @Override
    public void update(float deltaTime) {
        if (entities == null) {
            entities = getEngine().getEntitiesFor(
                com.badlogic.ashley.core.Family.all(InteractIndicatorSuppression.class).get());
        }
        if (entities == null || entities.size() == 0) {
            return;
        }
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity entity = entities.get(i);
            InteractIndicatorSuppression suppression = InteractIndicatorSuppression.MAPPER.get(entity);
            if (suppression == null) {
                continue;
            }
            float remaining = suppression.getRemainingSeconds() - deltaTime;
            if (remaining <= 0f) {
                entity.remove(InteractIndicatorSuppression.class);
            } else {
                suppression.setRemainingSeconds(remaining);
            }
        }
    }

    private void onCommand(CommandEvent event) {
        if (event.getCommand() != Command.INTERACT) {
            return;
        }
        Entity focused = activeEntityReference == null ? null : activeEntityReference.getFocused();
        if (focused == null) {
            return;
        }
        InteractIndicatorSuppression suppression = InteractIndicatorSuppression.MAPPER.get(focused);
        if (suppression == null) {
            focused.add(new InteractIndicatorSuppression(Constants.INTERACT_INDICATOR_SUPPRESSION_SECONDS));
        } else {
            suppression.setRemainingSeconds(Constants.INTERACT_INDICATOR_SUPPRESSION_SECONDS);
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
