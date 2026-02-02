package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.XPGainEvent;
import com.github.tilcob.game.event.XpGainRequestEvent;
import com.github.tilcob.game.skill.XpDistributionLoader;

import java.util.Map;

public class XpDistributionSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;

    public XpDistributionSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(XpGainRequestEvent.class, this::onXpGainRequest);
    }

    private void onXpGainRequest(XpGainRequestEvent event) {
        if (event == null || event.entity() == null) return;
        Map<String, Float> distribution = XpDistributionLoader.getDistribution(event.source());
        if (distribution == null || distribution.isEmpty()) return;
        if (event.baseXp() <= 0) return;
        if (!Float.isFinite(event.tileMultiplier()) || event.tileMultiplier() <= 0f) return;

        float totalWeight = 0f;
        for (float weight : distribution.values()) {
            totalWeight += weight;
        }
        if (totalWeight <= 0f) return;

        int totalXp = Math.max(0, Math.round(event.baseXp() * event.tileMultiplier()));
        if (totalXp == 0) return;

        int remaining = totalXp;
        int index = 0;
        int size = distribution.size();
        for (Map.Entry<String, Float> entry : distribution.entrySet()) {
            index++;
            int amount;
            if (index < size) {
                amount = (int) Math.floor(totalXp * (entry.getValue() / totalWeight));
                remaining -= amount;
            } else {
                amount = remaining;
            }
            if (amount <= 0) continue;
            eventBus.fire(new XPGainEvent(event.entity(), entry.getKey(), amount));
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(XpGainRequestEvent.class, this::onXpGainRequest);
    }
}
