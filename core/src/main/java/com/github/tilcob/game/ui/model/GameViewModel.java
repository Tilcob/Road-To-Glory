package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.Constants;

import java.util.Map;

public class GameViewModel extends ViewModel {
    private final AudioManager audioManager;
    private int lifePoints;
    private int maxLife;
    private Map.Entry<Vector2, Integer> playerDamage;
    private final Vector2 tmpVec2;

    public GameViewModel(GdxGame game) {
        super(game);
        this.audioManager = game.getAudioManager();
        this.lifePoints = 0;
        this.maxLife = 0;
        this.playerDamage = null;
        this.tmpVec2 = new Vector2();
    }

    public void playerDamage(int amount, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.playerDamage = Map.entry(position, amount);
        this.propertyChangeSupport.firePropertyChange(Constants.PLAYER_DAMAGE_PC, null, this.playerDamage);
    }

    public Vector2 toScreenCoords(Vector2 position) {
        tmpVec2.set(position);
        game.getViewport().project(tmpVec2);
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
}
