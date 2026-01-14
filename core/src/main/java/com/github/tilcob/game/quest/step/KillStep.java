package com.github.tilcob.game.quest.step;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.quest.KillEvent;

public class KillStep implements QuestStep {
    private final String enemy;
    private final int amount;
    private final GameEventBus eventBus;
    private int killed;
    private boolean completed;

    public KillStep(String enemy, int amount, GameEventBus eventBus) {
        this.enemy = enemy;
        this.amount = amount;
        this.eventBus = eventBus;
        this.killed = 0;
        this.completed = false;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void start() {
        eventBus.subscribe(KillEvent.class, this::onEvent);
    }

    private void onEvent(KillEvent event) {
        if (completed) {
            return;
        }
        if (event.enemy() != null && event.enemy().equals(enemy)) {
            killed += event.count();
            if (killed >= amount) {
                completed = true;
                end();
            }
        }
    }

    @Override
    public void end() {
        eventBus.unsubscribe(KillEvent.class, this::onEvent);
    }

    @Override
    public Object saveData() {
        return killed;
    }

    @Override
    public void loadData(Object data) {
        if (data instanceof Integer value) {
            killed = value;
            if (killed >= amount) {
                completed = true;
            }
        }
    }
}
