package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.component.*;

public class AiSystem extends IteratingSystem {

    public AiSystem() {
        super(Family.all(Npc.class).exclude(Player.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerReference reference = PlayerReference.MAPPER.get(entity);
        if (reference.getPlayer() == null) {
            ImmutableArray<Entity> players = getEngine().getEntitiesFor(Family.all(Player.class).get());
            if (players.size() > 0) reference.setPlayer(players.first());
        }
        NpcFsm.MAPPER.get(entity).getNpcFsm().update();
    }
}
