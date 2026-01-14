package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.MenuViewModel;

public class MenuView extends View<MenuViewModel> {
    private final Image selectionImage;
    private Group selectedItem;

    public MenuView(Skin skin, Stage stage, MenuViewModel viewModel) {
        super(skin, stage, viewModel);

        this.selectionImage = new Image(skin, "selection");
        this.selectionImage.setTouchable(Touchable.disabled);

        this.selectedItem = findActor(MenuOption.START_GAME.name());
        selectMenuItem(selectedItem);
    }

    private void selectMenuItem(Group menuItem) {
        if (selectionImage.getParent() != null) {
            selectionImage.getParent().removeActor(selectionImage);
        }

        float extraSize = 7f;
        float halfExtraSize = extraSize * .5f;
        float resizeTime = .2f;

        selectedItem = menuItem;
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
        onEnter(textButton, this::selectMenuItem);
        contentTable.row();

        Slider musicSlider = setupVolumesSlider(contentTable, "Music Volume", MenuOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, (slider) -> viewModel.setMusicVolume(slider.getValue()));

        Slider soundSlider = setupVolumesSlider(contentTable, "Sound Volume", MenuOption.SOUND_VOLUME);
        soundSlider.setValue(viewModel.getSoundVolume());
        onChange(soundSlider, (slider) -> viewModel.setSoundVolume(slider.getValue()));

        textButton = new TextButton("Quit Game", skin);
        textButton.setName(MenuOption.QUIT_GAME.name());
        contentTable.add(textButton).padTop(10.0f);
        onClick(textButton, viewModel::quitGame);
        onEnter(textButton, this::selectMenuItem);
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

        onEnter(table, this::selectMenuItem);
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

    /**
     * Moves selection to the next menu item.
     */
    public void onDown(Object o) {
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = (currentIdx + 1) % numOptions;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));

        // stage.setDebugAll(true); to see debug infos
    }

    /**
     * Moves selection to the previous menu item.
     */
    public void onUp(Object o) {
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = currentIdx == 0 ? numOptions - 1 : currentIdx - 1;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    public void onRight(Object o) {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) this.selectedItem.getChild(1);
                slider.setValue(slider.getValue() + slider.getStepSize());
            }
        }
    }

    public void onLeft(Object o) {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case MUSIC_VOLUME, SOUND_VOLUME -> {
                Slider slider = (Slider) this.selectedItem.getChild(1);
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
