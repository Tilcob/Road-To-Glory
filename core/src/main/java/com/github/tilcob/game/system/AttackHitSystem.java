package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Damaged;
import com.github.tilcob.game.event.AttackHitEvent;
import com.github.tilcob.game.event.GameEventBus;

public class AttackHitSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final Array<AttackHitEvent> pendingHits;

    public AttackHitSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        this.pendingHits = new Array<>();

        eventBus.subscribe(AttackHitEvent.class, this::queueHit);
    }

    @Override
    public void update(float deltaTime) {
        if (pendingHits.isEmpty()) return;
        for (AttackHitEvent event : pendingHits) {
            Entity target = event.target();
            Damaged damaged = Damaged.MAPPER.get(target);
            if (damaged == null) {
                target.add(new Damaged(event.damage(), event.attacker()));
            } else {
                damaged.addDamage(event.damage(), event.attacker());
            }
        }
        pendingHits.clear();
    }

    private void queueHit(AttackHitEvent event) {
        pendingHits.add(event);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AttackHitEvent.class, this::queueHit);
        pendingHits.clear();
    }
}
