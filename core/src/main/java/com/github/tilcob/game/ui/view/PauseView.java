package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiOverlayEvent;
import com.github.tilcob.game.ui.model.PauseViewModel;

public class PauseView extends View<PauseViewModel> {
    private Group selectedItem;

    public PauseView(Skin skin, Stage stage, PauseViewModel viewModel) {
        super(skin, stage, viewModel);
        Image selectionImage = new Image(skin, "selection");
        selectionImage.setTouchable(Touchable.disabled);

        this.selectedItem = findActor(PauseOption.RESUME.name());
        viewModel.getUiServices().selectMenuItem(selectedItem);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        setRoot(this);

        Table contentTable = new Table();
        contentTable.setBackground(skin.getDrawable("frame"));
        contentTable.pad(30f);

        Label title = new Label("Paused", skin, "text_12");
        title.setColor(skin.getColor("sand"));
        contentTable.add(title).padBottom(15f).row();

        Table buttonTable = new Table();
        contentTable.add(buttonTable).row();

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.setName(PauseOption.RESUME.name());
        buttonTable.add(resumeButton).width(180f).row();
        onClick(resumeButton, viewModel::resumeGame);
        onEnter(resumeButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.setName(PauseOption.SETTINGS.name());
        buttonTable.add(settingsButton).width(180f).padTop(10f).row();
        onClick(settingsButton, () -> viewModel.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.OPEN_SETTINGS)));
        onEnter(settingsButton, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        TextButton quitButton = new TextButton("Quit to Menu", skin);
        quitButton.setName(PauseOption.QUIT.name());
        buttonTable.add(quitButton).width(180f).padTop(10f).row();
        onClick(quitButton, viewModel::quitToMenu);
        onEnter(quitButton, (item) -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        add(contentTable).expand().center();
        align(Align.center);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }

    private void onDown(Object ignored) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    private void onUp(Object ignored) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    private void onSelect(Object ignored) {
        PauseOption option = PauseOption.valueOf(selectedItem.getName());
        switch (option) {
            case RESUME -> viewModel.resumeGame();
            case QUIT -> viewModel.quitToMenu();
            case SETTINGS -> viewModel.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.OPEN_SETTINGS));
        }
    }

    public void resetSelection() {
        Group resumeItem = findActor(PauseOption.RESUME.name());
        if (resumeItem != null) {
            selectedItem = viewModel.getUiServices().selectMenuItem(resumeItem);
        }
    }

    public void selectSettings() {
        Group settingsItem = findActor(PauseOption.SETTINGS.name());
        if (settingsItem != null) {
            selectedItem = viewModel.getUiServices().selectMenuItem(settingsItem);
        }
    }

    private enum PauseOption {
        RESUME,
        QUIT,
        SETTINGS,
    }
}
