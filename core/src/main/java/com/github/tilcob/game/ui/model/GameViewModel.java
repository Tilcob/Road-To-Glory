package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Dialog;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.Trigger;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.dialog.DialogChoice;
import com.github.tilcob.game.event.DialogChoiceEvent;
import com.github.tilcob.game.event.DialogEvent;
import com.github.tilcob.game.event.ExitTriggerEvent;
import com.github.tilcob.game.event.FinishedDialogEvent;

import java.util.Map;

public class GameViewModel extends ViewModel {
    private final AudioManager audioManager;
    private final Viewport viewport;
    private int lifePoints;
    private int maxLife;
    private Map.Entry<Vector2, Integer> playerDamage;
    private final Vector2 tmpVec2;

    public GameViewModel(GameServices services, Viewport viewport) {
        super(services);
        this.audioManager = services.getAudioManager();
        this.viewport = viewport;
        this.lifePoints = 0;
        this.maxLife = 0;
        this.playerDamage = null;
        this.tmpVec2 = new Vector2();

        getEventBus().subscribe(DialogEvent.class, this::onDialog);
        getEventBus().subscribe(DialogChoiceEvent.class, this::onDialogChoices);
        getEventBus().subscribe(ExitTriggerEvent.class, this::onExitTrigger);
        getEventBus().subscribe(FinishedDialogEvent.class, this::onDialogFinished);
    }

    private void onDialog(DialogEvent event) {
        String speaker = "NPC";
        if (event.entity() != null && Npc.MAPPER.get(event.entity()) != null) {
            speaker = Npc.MAPPER.get(event.entity()).getName();
        }
        this.propertyChangeSupport.firePropertyChange(
            Constants.SHOW_DIALOG,
            null,
            new DialogDisplay(speaker, event.line())
        );
    }

    private void onDialogChoices(DialogChoiceEvent event) {
        Array<String> labels = new Array<>();
        if (event.choices() != null) {
            for (DialogChoice choice : event.choices()) {
                labels.add(choice.text());
            }
        }
        if (labels.isEmpty()) {
            this.propertyChangeSupport.firePropertyChange(Constants.HIDE_DIALOG_CHOICES, null, true);
            return;
        }
        this.propertyChangeSupport.firePropertyChange(
            Constants.SHOW_DIALOG_CHOICES,
            null,
            new DialogChoiceDisplay(labels, event.selectedIndex())
        );
    }

    private void onDialogFinished(FinishedDialogEvent event) {
        this.propertyChangeSupport.firePropertyChange(Constants.HIDE_DIALOG, null, true);
    }

    public void playerDamage(int amount, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.playerDamage = Map.entry(position, amount);
        this.propertyChangeSupport.firePropertyChange(Constants.PLAYER_DAMAGE_PC, null, this.playerDamage);
    }

    private void onExitTrigger(ExitTriggerEvent event) {
        Trigger trigger = Trigger.MAPPER.get(event.trigger());
        if (trigger != null && trigger.getType() != Trigger.Type.DIALOG) return;
        if (trigger == null && Dialog.MAPPER.get(event.trigger()) == null) return;
        this.propertyChangeSupport.firePropertyChange(Constants.HIDE_DIALOG, null, true);
    }

    public Vector2 toScreenCoords(Vector2 position) {
        tmpVec2.set(position);
        viewport.project(tmpVec2);
        return tmpVec2;
    }

    public void updateLifeInfo(float maxLife, float life) {
        setMaxLife((int) maxLife);
        setLifePoints((int) life);
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void setLifePoints(int lifePoints) {
        if (this.lifePoints != lifePoints) {
            this.propertyChangeSupport.firePropertyChange(Constants.LIFE_POINTS_PC, this.lifePoints, lifePoints);
            if (this.lifePoints != 0 && this.lifePoints < lifePoints) {
                audioManager.playSound(SoundAsset.LIFE_REG);
            }
        }
        this.lifePoints = lifePoints;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        if (this.maxLife != maxLife) {
            this.propertyChangeSupport.firePropertyChange(Constants.MAX_LIFE_PC, this.maxLife, maxLife);
        }
        this.maxLife = maxLife;
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(DialogEvent.class, this::onDialog);
        getEventBus().unsubscribe(DialogChoiceEvent.class, this::onDialogChoices);
        getEventBus().unsubscribe(ExitTriggerEvent.class, this::onExitTrigger);
        getEventBus().unsubscribe(FinishedDialogEvent.class, this::onDialogFinished);
    }
}
