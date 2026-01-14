package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.dialog.DialogChoice;
import com.github.tilcob.game.dialog.DialogEffect;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.DialogChoiceResolvedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestStepEvent;

public class DialogConsequenceSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;

    public DialogConsequenceSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(DialogChoiceResolvedEvent.class, this::onChoiceResolved);
    }

    private void onChoiceResolved(DialogChoiceResolvedEvent event) {
        DialogChoice choice = event.choice();
        if (choice == null) {
            return;
        }
        Array<DialogEffect> effects = choice.effects();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (DialogEffect effect : effects) {
            if (effect == null || effect.type() == null) {
                continue;
            }
            switch (effect.type()) {
                case ADD_QUEST -> applyAddQuest(event.player(), effect);
                case SET_FLAG -> applyFlag(event.player(), effect);
                case QUEST_STEP -> applyQuestStep(effect);
            }
        }
    }

    private void applyAddQuest(Entity player, DialogEffect effect) {
        if (effect.questId() == null) {
            return;
        }
        eventBus.fire(new AddQuestEvent(player, effect.questId()));
    }

    private void applyFlag(Entity player, DialogEffect effect) {
        if (effect.flag() == null) {
            return;
        }
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        boolean value = effect.value() == null || effect.value();
        flags.set(effect.flag(), value);
    }

    private void applyQuestStep(DialogEffect effect) {
        if (effect.stepType() == null || effect.target() == null) {
            return;
        }
        eventBus.fire(new QuestStepEvent(effect.stepType(), effect.target()));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogChoiceResolvedEvent.class, this::onChoiceResolved);
    }
}
