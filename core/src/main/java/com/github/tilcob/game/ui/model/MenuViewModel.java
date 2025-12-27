package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.screen.GameScreen;

public class MenuViewModel extends ViewModel {
    private final AudioManager audioManager;

    public MenuViewModel(GdxGame game) {
        super(game);
        this.audioManager = game.getAudioManager();
    }

    public float getMusicVolume() {
        return audioManager.getMusicVolume();
    }

    public float getSoundVolume() {
        return audioManager.getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        audioManager.setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        audioManager.setSoundVolume(volume);
    }

    public void startGame() {
        game.setScreen(GameScreen.class);
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
