package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.AttackHitEvent;
import com.github.tilcob.game.event.ExitTriggerEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.npc.NpcType;

public class GameContactListener implements ContactListener {
    private final GameEventBus eventBus;

    public GameContactListener(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void beginContact(Contact contact) {
        Collision collision = getCollision(contact);
        if (collision == null)
            return;

        handleAttackContact(collision.entityA(), collision.fixtureA(),
            collision.entityB(), collision.fixtureB());
        handleAttackContact(collision.entityB(), collision.fixtureB(),
            collision.entityA(), collision.fixtureA());

        onEnter(collision.entityA(), collision.fixtureA(), collision.entityB(), collision.fixtureB());
    }

    @Override
    public void endContact(Contact contact) {
        Collision collision = getCollision(contact);
        if (collision == null)
            return;

        if (isPlayer(collision.entityA(), collision.fixtureA()) && isAttackFixture(collision.fixtureA()))
            return;
        if (isPlayer(collision.entityB(), collision.fixtureB()) && isAttackFixture(collision.fixtureB()))
            return;

        onExit(collision.entityA(), collision.fixtureA(), collision.entityB(), collision.fixtureB());
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private void onEnter(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if (hasTriggerComponent(entityA) && isPlayer(entityB, fixtureB)) {
            Trigger trigger = Trigger.MAPPER.get(entityA);
            trigger.add(entityB);
        } else if (hasTriggerComponent(entityB) && isPlayer(entityA, fixtureA)) {
            Trigger trigger = Trigger.MAPPER.get(entityB);
            trigger.add(entityA);
        } else if (isSensor(fixtureA) && isPlayer(entityB, fixtureB)) {
            addTrigger(fixtureA, entityA, entityB);
        } else if (isSensor(fixtureB) && isPlayer(entityA, fixtureA)) {
            addTrigger(fixtureB, entityB, entityA);
        }
    }

    private void onExit(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if (hasTriggerComponent(entityA) && isPlayer(entityB, fixtureB)) {
            Trigger trigger = Trigger.MAPPER.get(entityA);
            trigger.remove(entityB);
            eventBus.fire(new ExitTriggerEvent(entityB, entityA));
        } else if (hasTriggerComponent(entityB) && isPlayer(entityA, fixtureA)) {
            Trigger trigger = Trigger.MAPPER.get(entityB);
            trigger.remove(entityA);
            eventBus.fire(new ExitTriggerEvent(entityA, entityB));
        } else if (isSensor(fixtureA) && isPlayer(entityB, fixtureB)) {
            if (hasTriggerComponent(entityA)) {
                Trigger trigger = Trigger.MAPPER.get(entityA);
                trigger.remove(entityB);
            }
            eventBus.fire(new ExitTriggerEvent(entityB, entityA));
        } else if (isSensor(fixtureB) && isPlayer(entityA, fixtureA)) {
            if (hasTriggerComponent(entityB)) {
                Trigger trigger = Trigger.MAPPER.get(entityB);
                trigger.remove(entityA);
            }
            eventBus.fire(new ExitTriggerEvent(entityA, entityB));
        }
    }

    private void handleAttackContact(Entity attacker, Fixture attackFixture, Entity target, Fixture targetFixture) {
        if (!isAttackFixture(attackFixture)) return;
        if (isAttackFixture(targetFixture)) return;

        Attack attack = Attack.MAPPER.get(attacker);
        if (attack == null || !attack.isDamageWindowActive()) return;
        if (attacker == target) return;
        if (isFriendlyFire(attacker, target)) return;

        Life life = Life.MAPPER.get(target);
        if (life == null) return;

        if (attack.hasHit(target)) return;
        attack.registerHit(target);
        eventBus.fire(new AttackHitEvent(attacker, target, attack.getDamage()));
    }

    private boolean isFriendlyFire(Entity attacker, Entity target) {
        Npc targetNpc = Npc.MAPPER.get(target);
        if (targetNpc != null && targetNpc.getType() == NpcType.FRIEND) return true;

        Npc attackerNpc = Npc.MAPPER.get(attacker);
        if (attackerNpc != null && attackerNpc.getType() == NpcType.FRIEND) {
            return Player.MAPPER.get(target) != null
                || (targetNpc != null && targetNpc.getType() == NpcType.FRIEND);
        }

        return Player.MAPPER.get(attacker) != null && Player.MAPPER.get(target) != null;
    }

    private void addTrigger(Fixture trigger, Entity triggerEntity, Entity player) {
        MapObject mapObject = (MapObject) trigger.getUserData();
        if (mapObject == null)
            return;

        String typeStr = mapObject.getProperties().get(Constants.TRIGGER_TYPE, "", String.class);
        String questId = mapObject.getProperties().get(Constants.QUEST_ID, "", String.class);
        if (typeStr.isBlank())
            throw new GdxRuntimeException("Missing or false trigger type: " + typeStr);

        Trigger triggerComp = new Trigger(Trigger.Type.valueOf(typeStr));
        triggerComp.add(player);
        triggerEntity.add(triggerComp);
        if (!questId.isBlank())
            triggerEntity.add(new Quest(questId));
    }

    private boolean isSensor(Fixture fixture) {
        if (fixture.getUserData() instanceof MapObject mapObject) {
            return mapObject.getProperties().get(Constants.TYPE, "", String.class)
                    .equals(Constants.TRIGGER_CLASS)
                    && mapObject.getProperties().get(Constants.SENSOR, false, Boolean.class);
        }
        return false;
    }

    private boolean hasTriggerComponent(Entity triggerEntity) {
        Trigger trigger = Trigger.MAPPER.get(triggerEntity);
        return trigger != null;
    }

    private boolean isPlayer(Entity entity, Fixture fixture) {
        return Player.MAPPER.get(entity) != null && !fixture.isSensor();
    }

    private boolean isAttackFixture(Fixture fixture) {
        if (fixture.getUserData() instanceof MapObject mapObject) {
            return mapObject.getName().contains("attack")
                    && mapObject.getProperties().get(Constants.SENSOR, false, Boolean.class);
        }
        return false;
    }

    private Collision getCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (!(fixtureA.getBody().getUserData() instanceof Entity entityA) ||
                !(fixtureB.getBody().getUserData() instanceof Entity entityB))
            return null;

        return new Collision(fixtureA, fixtureB, entityA, entityB);
    }

    public record Collision(Fixture fixtureA, Fixture fixtureB, Entity entityA, Entity entityB) {
    }
}
