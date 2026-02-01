package com.github.tilcob.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.MusicAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.config.Constants;

public class AudioManager implements Disposable {
    private final AssetManager assetManager;
    private Music currentMusic;
    private MusicAsset currentMusicAsset;
    private float musicVolume;
    private float soundVolume;

    public AudioManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.currentMusic = null;
        this.currentMusicAsset = null;
        this.musicVolume = .5f;
        this.soundVolume = .33f;
    }

    public void playMusic(MusicAsset musicAsset) {
        if (this.currentMusicAsset == musicAsset)
            return;

        if (this.currentMusic != null) {
            this.currentMusic.stop();
        }

        this.currentMusic = assetManager.loadSync(musicAsset);
        this.currentMusic.setLooping(true);
        this.currentMusic.setVolume(musicVolume);
        this.currentMusic.play();
        this.currentMusicAsset = musicAsset;
    }

    public void playSound(SoundAsset soundAsset) {
        Sound sound = assetManager.get(soundAsset);
        sound.play(soundVolume);
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = MathUtils.clamp(musicVolume, 0.0f, 1.0f);

        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = MathUtils.clamp(soundVolume, 0.0f, 1.0f);
    }

    public void setMap(TiledMap tiledMap) {
        String musicAssetStr = tiledMap.getProperties().get(Constants.MUSIC, "", String.class);

        if (musicAssetStr.isBlank())
            return;

        MusicAsset musicAsset = MusicAsset.valueOf(musicAssetStr);
        playMusic(musicAsset);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        if (currentMusic != null) {
            currentMusic.dispose();
            currentMusic = null;
        }
        currentMusicAsset = null;
    }
}
