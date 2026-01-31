package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.SettingsViewModel;

public class SettingsView extends View<SettingsViewModel> {
    private Group selectedItem;

    public SettingsView(Skin skin, Stage stage, SettingsViewModel viewModel) {
        super(skin, stage, viewModel);

        Image selectionImage = new Image(skin, "selection");
        selectionImage.setTouchable(Touchable.disabled);

        //this.selectedItem = findActor(MenuOption.START_GAME.name());
        viewModel.getUiServices().selectMenuItem(selectedItem);
    }

    @Override
    protected void setupUI() {

    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_RIGHT, Boolean.class, this::onRight);
        viewModel.onPropertyChange(Constants.ON_LEFT, Boolean.class, this::onLeft);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }

    private void onDown(Object o) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    private void onUp(Object o) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    private void onRight(Object o) {
    }

    private void onLeft(Object o) {
    }

    private void onSelect(Object o) {

    }

    private enum SettingsOption {

    }
}
