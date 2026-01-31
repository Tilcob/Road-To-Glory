package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.SettingsViewModel;

public class SettingsView extends View<SettingsViewModel> {

    public SettingsView(Skin skin, Stage stage, SettingsViewModel viewModel) {
        super(skin, stage, viewModel);


    }

    @Override
    protected void setupUI() {

    }

    @Override
    protected void setupPropertyChanges() {
//        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
//        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
//        viewModel.onPropertyChange(Constants.ON_RIGHT, Boolean.class, this::onRight);
//        viewModel.onPropertyChange(Constants.ON_LEFT, Boolean.class, this::onLeft);
//        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }
}
