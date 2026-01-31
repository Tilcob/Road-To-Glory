package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.MenuViewModel;

public class MenuView extends View<MenuViewModel> {
    private Group selectedItem;

    public MenuView(Skin skin, Stage stage, MenuViewModel viewModel) {
        super(skin, stage, viewModel);

        Image selectionImage = new Image(skin, "selection");
        selectionImage.setTouchable(Touchable.disabled);

        this.selectedItem = findActor(MenuOption.START_GAME.name());
        viewModel.getUiServices().selectMenuItem(selectedItem);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        Image image = new Image(skin, "banner");
        add(image).row();

        setupMenuContent();

        Label label = new Label("by Tilcob 2025", skin, "text_10");
        label.setColor(skin.getColor("WHITE"));
        add(label).padRight(2.0f).expandX().align(Align.bottomRight);
    }

    private void setupMenuContent() {
        Table contentTable = new Table();
        contentTable.setBackground(skin.getDrawable("frame"));
        contentTable.padLeft(40.0f);
        contentTable.padRight(40.0f);
        contentTable.padTop(25.0f);
        contentTable.padBottom(20.0f);

        TextButton textButton = new TextButton("Start Game", skin);
        textButton.setName(MenuOption.START_GAME.name());
        contentTable.add(textButton);
        onClick(textButton, viewModel::startGame);
        onEnter(textButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));
        contentTable.row();

        Slider musicSlider = setupVolumesSlider(contentTable, "Music Volume", MenuOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getUiServices().getMusicVolume());
        onChange(musicSlider, (slider) -> viewModel.getUiServices().setMusicVolume(slider.getValue()));

        Slider soundSlider = setupVolumesSlider(contentTable, "Sound Volume", MenuOption.SOUND_VOLUME);
        soundSlider.setValue(viewModel.getUiServices().getSoundVolume());
        onChange(soundSlider, (slider) -> viewModel.getUiServices().setSoundVolume(slider.getValue()));

        textButton = new TextButton("Quit Game", skin);
        textButton.setName(MenuOption.QUIT_GAME.name());
        contentTable.add(textButton).padTop(10.0f);
        onClick(textButton, viewModel::quitGame);
        onEnter(textButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));
        add(contentTable).row();
    }

    private Slider setupVolumesSlider(Table contentTable, String title, MenuOption menuOption) {
        Table table = new Table();
        table.setName(menuOption.name());

        Label label = new Label(title, skin, "text_12");
        label.setColor(skin.getColor("sand"));
        table.add(label).row();

        Slider slider = new Slider(0f, 1f, .05f, false, skin);
        table.add(slider);
        contentTable.add(table).padTop(10.0f).row();

        onEnter(table, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));
        return slider;
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_RIGHT, Boolean.class, this::onRight);
        viewModel.onPropertyChange(Constants.ON_LEFT, Boolean.class, this::onLeft);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }

    public void onDown(Object o) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    public void onUp(Object o) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    public void onRight(Object o) {
        MenuOption menuOption = MenuOption.valueOf(selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) selectedItem.getChild(1);
                slider.setValue(slider.getValue() + slider.getStepSize());
            }
        }
    }

    public void onLeft(Object o) {
        MenuOption menuOption = MenuOption.valueOf(selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) selectedItem.getChild(1);
                slider.setValue(slider.getValue() - slider.getStepSize());
            }
        }
    }

    public void onSelect(Object o) {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case START_GAME -> viewModel.startGame();
            case QUIT_GAME -> viewModel.quitGame();
        }
    }

    private enum MenuOption {
        START_GAME,
        MUSIC_VOLUME,
        SOUND_VOLUME,
        QUIT_GAME
    }
}
