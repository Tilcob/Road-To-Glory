package com.github.tilcob.game.quest.step;


public interface QuestStep {
    boolean completed();
    void start();
    void end();

    default Object saveData() {
        return null;
    }
    default void loadData(Object data) {}
}
