package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ability.Ability;
import com.github.tilcob.game.input.Command;

public class AbilityRequestEvent {
    private final Entity entity;
    private final Command command;
    private final Ability ability;
    private final int priority;
    private final boolean resolved;
    private boolean handled;

    private AbilityRequestEvent(Entity entity, Command command, Ability ability, int priority, boolean resolved) {
        this.entity = entity;
        this.command = command;
        this.ability = ability;
        this.priority = priority;
        this.resolved = resolved;
    }

    public static AbilityRequestEvent fromCommand(Entity entity, Command command) {
        return new AbilityRequestEvent(entity, command, null, 0, false);
    }

    public static AbilityRequestEvent fromCommand(Entity entity, Command command, int priority) {
        return new AbilityRequestEvent(entity, command, null, priority, false);
    }

    public static AbilityRequestEvent fromAbility(Entity entity, Ability ability) {
        return new AbilityRequestEvent(entity, null, ability, 0, false);
    }

    public static AbilityRequestEvent fromAbility(Entity entity, Ability ability, int priority) {
        return new AbilityRequestEvent(entity, null, ability, priority, false);
    }

    public static AbilityRequestEvent resolved(Entity entity, Ability ability, int priority) {
        return new AbilityRequestEvent(entity, null, ability, priority, true);
    }

    public Entity getEntity() {
        return entity;
    }

    public Command getCommand() {
        return command;
    }

    public Ability getAbility() {
        return ability;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
