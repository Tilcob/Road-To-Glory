package com.github.tilcob.game.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.ai.behavior.NpcBehaviorRegistry;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.Command;

public enum NpcState implements State<Entity> {
    IDLE {
        @Override
        public void enter(Entity entity) {}

        @Override
        public void update(Entity entity) {
            behaviorProfile(entity).updateIdle(entity);
        }

        @Override
        public void exit(Entity entity) {}

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },
    TALKING {
        @Override
        public void enter(Entity entity) {
            //Controller.MAPPER.get(entity).getPressedCommands().clear();
            //lookAtPlayer(findPlayer(entity), entity);
        }

        @Override
        public void update(Entity entity) {}

        @Override
        public void exit(Entity entity) {}

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            if (telegram.message == Messages.DIALOG_FINISHED) {
                Fsm.MAPPER.get(entity).getNpcFsm().changeState(IDLE);
                return true;
            }
            return false;
        }
    },
    WANDER {
        @Override
        public void enter(Entity entity) {
            Controller controller = Controller.MAPPER.get(entity);
            int direction = MathUtils.random(1, 4);
            switch (direction) {
                case 1 -> controller.getPressedCommands().add(Command.UP);
                case 2 -> controller.getPressedCommands().add(Command.DOWN);
                case 3 -> controller.getPressedCommands().add(Command.LEFT);
                case 4 -> controller.getPressedCommands().add(Command.RIGHT);
            }
        }

        @Override
        public void update(Entity entity) {
            if (Math.random() < 0.01) Fsm.MAPPER.get(entity).getNpcFsm().changeState(IDLE);
        }

        @Override
        public void exit(Entity entity) {
            Controller.MAPPER.get(entity).getPressedCommands().clear();
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },
    CHASE {
        @Override
        public void enter(Entity entity) {

        }

        @Override
        public void update(Entity entity) {
            Entity player = findPlayer(entity);
            NpcBehaviorProfile profile = behaviorProfile(entity);
            if (player == null || !inAggroRange(entity, player, profile.getAggroRange())) {
                Fsm.MAPPER.get(entity).getNpcFsm().changeState(IDLE);
                return;
            }
            chasePlayer(entity, player);
        }

        @Override
        public void exit(Entity entity) {

        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    };

    void chasePlayer(Entity entity, Entity player) {

    }

    boolean inAggroRange(Entity entity, Entity player, float aggroRange) {
        Vector2 entityPos = Transform.MAPPER.get(entity).getPosition();
        Vector2 playerPos = Transform.MAPPER.get(player).getPosition();

        float dx = playerPos.x - entityPos.x;
        float dy = playerPos.y - entityPos.y;

        return dx * dx + dy * dy <= aggroRange * aggroRange;
    }

    NpcBehaviorProfile behaviorProfile(Entity entity) {
        Npc npc = Npc.MAPPER.get(entity);
        if (npc == null) {
            return NpcBehaviorRegistry.get(null);
        }
        return NpcBehaviorRegistry.get(npc.getType());
    }

    Entity findPlayer(Entity entity) {
        PlayerReference playerReference = PlayerReference.MAPPER.get(entity);
        if (playerReference == null) {
            return null;
        }
        return playerReference.getPlayer();
    }
}
