package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.InteractIndicatorSuppression;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.input.Command;

public class InteractIndicatorSuppressionSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final ActiveEntityReference activeEntityReference;

    public InteractIndicatorSuppressionSystem(GameEventBus eventBus, ActiveEntityReference activeEntityReference) {
        super(Family.all(InteractIndicatorSuppression.class).get());
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
        if (suppression == null) {
            focused.add(new InteractIndicatorSuppression(Constants.INTERACT_INDICATOR_SUPPRESSION_SECONDS));
        } else {
            suppression.setRemainingSeconds(Constants.INTERACT_INDICATOR_SUPPRESSION_SECONDS);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InteractIndicatorSuppression suppression = InteractIndicatorSuppression.MAPPER.get(entity);
        float remainingSeconds = suppression.getRemainingSeconds() - deltaTime;
        if (remainingSeconds <= 0f) {
            entity.remove(InteractIndicatorSuppression.class);
            return;
        }
        suppression.setRemainingSeconds(remainingSeconds);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
