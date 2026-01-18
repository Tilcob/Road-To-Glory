package com.github.tilcob.game.quest.step;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.quest.CollectItemEvent;

public class CollectItemStep implements QuestStep {
    private final String itemId;
    private final int amount;
    private final GameEventBus eventBus;
    private int collected;
    private boolean completed;

    public CollectItemStep(String itemId, int amount, GameEventBus eventBus) {
        this.itemId = itemId;
        this.amount = amount;
        this.eventBus = eventBus;
        this.collected = 0;
        this.completed = false;
    }

    @Override
    public boolean completed() {
        return completed;
    }

    @Override
    public void start() {
        eventBus.subscribe(CollectItemEvent.class, this::onEvent);
    }

    private void onEvent(CollectItemEvent event) {
        if (completed || event.itemId().equals(itemId)) {
            return;
        }
        collected += event.count();
        if (collected >= amount) {
            completed = true;
            end();
        }
    }

    @Override
    public void end() {
        eventBus.unsubscribe(CollectItemEvent.class, this::onEvent);
    }

    @Override
    public Object saveData() {
        return collected;
    }

    @Override
    public void loadData(Object data) {
        if (data instanceof Integer value) {
            collected = value;
            if (collected >= amount) {
                completed = true;
            }
        }
    }
}
