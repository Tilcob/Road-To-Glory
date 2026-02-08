package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.github.tilcob.game.ability.Ability;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.ai.behavior.NpcBehaviorRegistry;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.AbilityRequestEvent;
import com.github.tilcob.game.event.GameEventBus;

public final class NpcStateSupport {
    private static GameEventBus eventBus;

    private NpcStateSupport() {
    }

    public static void setEventBus(GameEventBus eventBus) {
        NpcStateSupport.eventBus = eventBus;
    }

    public static void requestAbility(Entity entity, Ability ability, int priority) {
        if (eventBus == null || entity == null || ability == null) return;
        eventBus.fire(AbilityRequestEvent.fromAbility(entity, ability, priority));
    }

    public static void chasePlayer(Entity entity, Entity player) {
    }

    public static void lookAtPlayer(Entity player, Entity entity) {
        if (Physic.MAPPER.get(entity).getBody().getType() == BodyDef.BodyType.StaticBody) return;

        Facing facingEntity = Facing.MAPPER.get(entity);
        Facing facingPlayer = Facing.MAPPER.get(player);

        switch (facingPlayer.getDirection()) {
            case UP -> facingEntity.setDirection(Facing.FacingDirection.DOWN);
            case DOWN -> facingEntity.setDirection(Facing.FacingDirection.UP);
            case LEFT -> facingEntity.setDirection(Facing.FacingDirection.RIGHT);
            case RIGHT -> facingEntity.setDirection(Facing.FacingDirection.LEFT);
        }
    }

    public static boolean inAggroRange(Entity entity, Entity player, float aggroRange) {
        Vector2 entityPos = Transform.MAPPER.get(entity).getPosition();
        Vector2 playerPos = Transform.MAPPER.get(player).getPosition();

        float dx = playerPos.x - entityPos.x;
        float dy = playerPos.y - entityPos.y;

        return dx * dx + dy * dy <= aggroRange * aggroRange;
    }

    public static boolean canAggro(Entity entity, Entity player) {
        if (player == null) return false;
        NpcBehaviorProfile profile = behaviorProfile(entity);
        if (!profile.canChase()) return false;

        Transform playerTransform = Transform.MAPPER.get(player);
        if (playerTransform == null) return false;

        boolean canSee = inAggroRange(entity, player, profile.getAggroRange())
            && hasLineOfSight(entity, player);
        boolean canHear = profile.getHearingRange() > 0f
            && inAggroRange(entity, player, profile.getHearingRange());

        if (canSee || canHear) {
            AggroMemory memory = AggroMemory.MAPPER.get(entity);
            if (memory != null) {
                memory.markSeen(playerTransform.getPosition());
            }
            return true;
        }
        return false;
    }

    public static boolean hasLineOfSight(Entity entity, Entity player) {
        Physic physic = Physic.MAPPER.get(entity);
        if (physic == null) return true;

        Vector2 from = Transform.MAPPER.get(entity).getPosition();
        Vector2 to = Transform.MAPPER.get(player).getPosition();
        if (from == null || to == null) return true;

        final boolean[] blocked = {false};
        physic.getBody().getWorld().rayCast((fixture, point, normal, fraction) -> {
            Object bodyUserData = fixture.getBody().getUserData();
            if (Constants.ENVIRONMENT.equals(bodyUserData)) {
                blocked[0] = true;
                return 0;
            }
            return -1;
        }, from, to);

        return !blocked[0];
    }

    public static Entity findPlayer(Entity entity) {
        PlayerReference playerReference = PlayerReference.MAPPER.get(entity);
        if (playerReference == null) {
            return null;
        }
        return playerReference.getPlayer();
    }

    public static NpcBehaviorProfile behaviorProfile(Entity entity) {
        Npc npc = Npc.MAPPER.get(entity);
        if (npc == null) {
            return NpcBehaviorRegistry.get(null);
        }
        return NpcBehaviorRegistry.get(npc.getType());
    }
}
