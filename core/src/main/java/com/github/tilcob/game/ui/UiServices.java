package com.github.tilcob.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.github.tilcob.game.audio.AudioManager;

public class UiServices {
    private static final String TAG = UiServices.class.getSimpleName();

    private final AudioManager audioManager;
    private Skin skin;
    private Image selectionImage;

    public UiServices(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public Group moveUp(Group selectedItem) {
        Group menuContentTable = selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(selectedItem, true);
        if (currentIdx == -1) {
            Gdx.app.error(TAG, "'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = currentIdx == 0 ? numOptions - 1 : currentIdx - 1;
        return selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    public Group moveDown(Group selectedItem) {
        Group menuContentTable = selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(selectedItem, true);
        if (currentIdx == -1) {
            Gdx.app.error(TAG, "'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = (currentIdx + 1) % numOptions;
        return selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    public void moveRight(Group selectedItem) {
        MenuOption menuOption = MenuOption.valueOf(selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) selectedItem.getChild(1);
                slider.setValue(slider.getValue() + slider.getStepSize());
            }
        }
    }

    public void moveLeft(Group selectedItem) {
        MenuOption menuOption = MenuOption.valueOf(selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) selectedItem.getChild(1);
                slider.setValue(slider.getValue() - slider.getStepSize());
            }
        }
    }

    public Group selectMenuItem(Group menuItem) {
        if (selectionImage.getParent() != null) {
            selectionImage.getParent().removeActor(selectionImage);
        }

        float extraSize = 7f;
        float halfExtraSize = extraSize * .5f;
        float resizeTime = .2f;

        menuItem.addActor(selectionImage);
        selectionImage.setPosition(-halfExtraSize, -halfExtraSize);
        selectionImage.setSize(menuItem.getWidth() + extraSize, menuItem.getHeight() + extraSize);
        selectionImage.clearActions();
        selectionImage.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtraSize, -halfExtraSize, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtraSize, halfExtraSize, resizeTime, Interpolation.linear)
            )
        )));
        return menuItem;
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

    public void setSkin(Skin skin) {
        this.skin = skin;
        selectionImage = new Image(skin, "selection");
    }

    public void setSelectionImage(String image) {
        this.selectionImage = new Image(skin, image);
    }
}
