package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.ability.Ability;
import com.github.tilcob.game.component.Attack;
import com.github.tilcob.game.event.AbilityRequestEvent;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.Command;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class AbilitySystem extends EntitySystem implements Disposable {
    private static final float REQUEST_BUFFER_SECONDS = 0.05f;

    private final GameEventBus eventBus;
    private final Map<Command, Ability> commandToAbility = new EnumMap<>(Command.class);
    private final Map<Entity, AbilityRequestWindow> requestWindows = new IdentityHashMap<>();

    public AbilitySystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        commandToAbility.put(Command.SELECT, Ability.ATTACK);
        eventBus.subscribe(AbilityRequestEvent.class, this::onRequest);
    }

    private void onRequest(AbilityRequestEvent event) {
        if (event.isResolved()) return;
        if (event.isHandled()) return;
        if (event.getCommand() != null) {
            handleCommand(event);
            return;
        }
        if (event.getAbility() != null) {
            handleAbility(event.getEntity(), event.getAbility(), event.getPriority());
        }
    }

    private void handleCommand(AbilityRequestEvent event) {
        Ability ability = commandToAbility.get(event.getCommand());
        if (ability != null) {
            handleAbility(event.getEntity(), ability, event.getPriority());
            return;
        }
        eventBus.fire(new CommandEvent(event.getEntity(), event.getCommand()));
    }

    private void handleAbility(Entity entity, Ability ability, int priority) {
        if (entity == null || ability == null) return;
        if (!canUseAbility(entity, ability)) return;
        float nowSeconds = TimeUtils.millis() / 1000f;
        AbilityRequestWindow window = requestWindows.computeIfAbsent(entity, ignored -> new AbilityRequestWindow());
        if (!window.shouldAccept(ability, priority, nowSeconds)) return;
        window.record(ability, priority, nowSeconds);
        eventBus.fire(AbilityRequestEvent.resolved(entity, ability, priority));
    }

    private boolean canUseAbility(Entity entity, Ability ability) {
        return switch (ability) {
            case ATTACK -> {
                Attack attack = Attack.MAPPER.get(entity);
                yield attack != null;
            }
        };
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AbilityRequestEvent.class, this::onRequest);
    }

    private static final class AbilityRequestWindow {
        private final Map<Ability, Float> lastRequestAt = new EnumMap<>(Ability.class);
        private final Map<Ability, Integer> lastPriority = new EnumMap<>(Ability.class);

        private boolean shouldAccept(Ability ability, int priority, float nowSeconds) {
            Float lastAt = lastRequestAt.get(ability);
            Integer lastPriorityValue = lastPriority.get(ability);
            if (lastAt == null || lastPriorityValue == null) return true;
            if (nowSeconds - lastAt >= REQUEST_BUFFER_SECONDS) return true;
            return priority > lastPriorityValue;
        }

        private void record(Ability ability, int priority, float nowSeconds) {
            lastRequestAt.put(ability, nowSeconds);
            lastPriority.put(ability, priority);
        }
    }
}
