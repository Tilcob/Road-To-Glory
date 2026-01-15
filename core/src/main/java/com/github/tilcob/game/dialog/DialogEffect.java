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

    public static DialogEffect addQuest(String questId) {
        DialogEffect effect = new DialogEffect();
        effect.type = EffectType.ADD_QUEST;
        effect.questId = questId;
        return effect;
    }

    public static DialogEffect setFlag(String flag, Boolean value) {
        DialogEffect effect = new DialogEffect();
        effect.type = EffectType.SET_FLAG;
        effect.flag = flag;
        effect.value = value;
        return effect;
    }

    public static DialogEffect questStep(QuestStepEvent.Type stepType, String target) {
        DialogEffect effect = new DialogEffect();
        effect.type = EffectType.QUEST_STEP;
        effect.stepType = stepType;
        effect.target = target;
        return effect;
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
