package com.github.tilcob.game.dialog;

import com.github.tilcob.game.event.QuestStepEvent;

/**
 * Dialog JSON effects for quests and dialog flags.
 *
 * <p>Rewards (currency, items, experience, etc.) are intentionally excluded from dialog effects.
 * If rewards are ever needed from dialog, introduce a dedicated dialog event type handled by a
 * reward-specific system instead of mutating economy state directly in dialog processing.</p>
 */
public class DialogEffect {
    public enum EffectType {
        ADD_QUEST,
        SET_FLAG,
        QUEST_STEP
    }

    private EffectType type;
    private String questId;
    private String flag;
    private Boolean value;
    private QuestStepEvent.Type stepType;
    private String target;

    public DialogEffect() {
    }

    public EffectType type() {
        return type;
    }

    public String questId() {
        return questId;
    }

    public String flag() {
        return flag;
    }

    public Boolean value() {
        return value;
    }

    public QuestStepEvent.Type stepType() {
        return stepType;
    }

    public String target() {
        return target;
    }
}
