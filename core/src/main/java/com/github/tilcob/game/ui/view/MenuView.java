package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiOverlayEvent;
import com.github.tilcob.game.ui.component.FrameLayout;
import com.github.tilcob.game.ui.component.MenuList;
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
        setRoot(this);

        Image image = new Image(skin, "banner");
        add(image).row();

        setupMenuContent();

        Label label = new Label("by Tilcob 2025", skin, "text_10");
        label.setColor(skin.getColor("WHITE"));
        add(label).padRight(2.0f).expandX().align(Align.bottomRight);
    }

    private void setupMenuContent() {
        MenuList menuList = new MenuList();
        FrameLayout frameLayout = new FrameLayout(skin, 25.0f, 40.0f, 20.0f, 40.0f);
        Table contentTable = frameLayout.getRoot();
        Table listTable = menuList.getListTable();

        contentTable.add(menuList.getRootTable()).row();

        TextButton textButton = new TextButton("Start Game", skin);
        textButton.setName(MenuOption.START_GAME.name());
        menuList.addItem(textButton);
        onClick(textButton, viewModel::startGame);
        onEnter(textButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        TextButton settings = new TextButton("Settings", skin);
        settings.setName(MenuOption.SETTINGS.name());
        menuList.addItem(settings);
        listTable.getCell(settings).padTop(10f);
        onClick(settings, () -> viewModel.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.OPEN_SETTINGS)));
        onEnter(settings, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        textButton = new TextButton("Quit Game", skin);
        textButton.setName(MenuOption.QUIT_GAME.name());
        menuList.addItem(textButton);
        listTable.getCell(textButton).padTop(10.0f);
        onClick(textButton, viewModel::quitGame);
        onEnter(textButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));
        add(contentTable).row();
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }

    public void onDown(Object o) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    public void onUp(Object o) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    public void onSelect(Object o) {
        MenuOption menuOption = MenuOption.valueOf(this.selectedItem.getName());
        switch (menuOption) {
            case START_GAME -> viewModel.startGame();
            case QUIT_GAME -> viewModel.quitGame();
            case SETTINGS -> viewModel.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.OPEN_SETTINGS));
        }
    }

    public void selectSettings() {
        Group settingsItem = findActor(MenuOption.SETTINGS.name());
        if (settingsItem != null) {
            selectedItem = viewModel.getUiServices().selectMenuItem(settingsItem);
        }
    }

    private enum MenuOption {
        START_GAME,
        SETTINGS,
        QUIT_GAME
    }
}
