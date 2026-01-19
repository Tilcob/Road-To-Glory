package com.github.tilcob.game.quest.step;

/**
 * @deprecated Quest steps are superseded by quest Yarn signals.
 */
@Deprecated(forRemoval = false)
public interface QuestStep {
    boolean completed();
    void start();
    void end();

    default Object saveData() {
        return null;
    }
    default void loadData(Object data) {}
}
