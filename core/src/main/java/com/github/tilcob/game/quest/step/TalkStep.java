package com.github.tilcob.game.quest.step;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.quest.TalkEvent;

public class TalkStep implements QuestStep {
    private final String npc;
    private final GameEventBus eventBus;
    private boolean completed = false;

    public TalkStep(String npc, GameEventBus eventBus) {
        this.npc = npc;
        this.eventBus = eventBus;
    }

    public String getNpc() {
        return npc;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void start() {
        eventBus.subscribe(TalkEvent.class, this::onEvent);
    }

    private void onEvent(TalkEvent event) {
        if (event.npc().equals(npc)) {
            completed = true;
            end();
        }
    }

    @Override
    public void end() {
        eventBus.unsubscribe(TalkEvent.class, this::onEvent);
    }
}
