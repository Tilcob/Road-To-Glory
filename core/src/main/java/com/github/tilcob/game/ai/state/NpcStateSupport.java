package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.ai.behavior.NpcBehaviorRegistry;
import com.github.tilcob.game.component.Facing;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.PlayerReference;
import com.github.tilcob.game.component.Transform;

public final class NpcStateSupport {
    private NpcStateSupport() {
    }

    public static void chasePlayer(Entity entity, Entity player) {
    }

    public static void lookAtPlayer(Entity player, Entity entity) {
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
