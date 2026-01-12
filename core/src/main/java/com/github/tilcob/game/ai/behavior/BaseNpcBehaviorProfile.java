package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.PlayerReference;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public abstract class BaseNpcBehaviorProfile implements NpcBehaviorProfile {

    @Override
    public float getAggroRange() {
        return Constants.AGGRO_RANGE;
    }

    protected Entity findPlayer(Entity entity) {
        PlayerReference reference = PlayerReference.MAPPER.get(entity);
        if (reference == null) {
            return null;
        }
        return reference.getPlayer();
    }

    protected boolean inAggroRange(Entity entity, Entity player, float aggroRange) {
        Vector2 entityPos = Transform.MAPPER.get(entity).getPosition();
        Vector2 playerPos = Transform.MAPPER.get(player).getPosition();

        float dx = playerPos.x - entityPos.x;
        float dy = playerPos.y - entityPos.y;

        return dx * dx + dy * dy <= aggroRange * aggroRange;
    }
}
