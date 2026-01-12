package com.github.tilcob.game.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;

public enum AnimationState implements State<Entity> {
    IDLE {
        @Override
        public void enter(Entity entity) {
            setupAnimation(entity, Animation2D.AnimationType.IDLE, Animation.PlayMode.LOOP);
        }

        @Override
        public void update(Entity entity) {
            Move move = Move.MAPPER.get(entity);
            if (move != null && !move.isRooted() && !move.getDirection().isZero()) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(WALK);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack != null && attack.isAttacking()) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(ATTACK);
                return;
            }

            Damaged damaged = Damaged.MAPPER.get(entity);
            if (damaged != null) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    WALK {
        @Override
        public void enter(Entity entity) {
            setupAnimation(entity, Animation2D.AnimationType.WALK, Animation.PlayMode.LOOP);
        }

        @Override
        public void update(Entity entity) {
            Move move = Move.MAPPER.get(entity);
            if (move.getDirection().isZero() || move.isRooted()) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    ATTACK {
        @Override
        public void enter(Entity entity) {
            setupAnimation(entity, Animation2D.AnimationType.ATTACK, Animation.PlayMode.NORMAL);
        }

        @Override
        public void update(Entity entity) {
            Attack attack = Attack.MAPPER.get(entity);
            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            if (attack.canAttack() || attack.isInRecovery() || animation2D.isFinished()) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    DAMAGED {
        @Override
        public void enter(Entity entity) {
            setupAnimation(entity, Animation2D.AnimationType.DAMAGED, Animation.PlayMode.NORMAL);
        }

        @Override
        public void update(Entity entity) {
            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            if (animation2D.isFinished()) {
                AnimationFsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    };

    private static void setupAnimation(Entity entity, Animation2D.AnimationType idle, Animation.PlayMode playMode) {
        Animation2D animation2D = Animation2D.MAPPER.get(entity);
        animation2D.setType(idle);
        animation2D.setPlayMode(playMode);
        if (animation2D.getSpeed() <= 0f) {
            animation2D.setSpeed(Constants.DEFAULT_ANIMATION_SPEED);
        }
    }
}
