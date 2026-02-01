package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.SettingsViewModel;

public class SettingsView extends View<SettingsViewModel> {
    private Group selectedItem;

    public SettingsView(Skin skin, Stage stage, SettingsViewModel viewModel) {
        super(skin, stage, viewModel);

        Image selectionImage = new Image(skin, "selection");
        selectionImage.setTouchable(Touchable.disabled);

        this.selectedItem = findActor(SettingsOption.MUSIC_VOLUME.name());
        viewModel.getUiServices().selectMenuItem(selectedItem);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        setRoot(this);
        setVisibleBound(false);

        Table contentTable = new Table();
        contentTable.setBackground(skin.getDrawable("frame"));
        contentTable.pad(30f);

        Label title = new Label("Settings", skin, "text_12");
        title.setColor(skin.getColor("sand"));
        contentTable.add(title).padBottom(15f).row();

        Table optionsTable = new Table();
        contentTable.add(optionsTable).row();

        Slider musicSlider = setupVolumesSlider(optionsTable, "Music Volume", SettingsOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, s -> viewModel.setMusicVolume(s.getValue()));

        Slider soundSlider = setupVolumesSlider(optionsTable, "Sound Volume", SettingsOption.SOUND_VOLUME);
        soundSlider.setValue(viewModel.getSoundVolume());
        onChange(soundSlider, s -> viewModel.setSoundVolume(s.getValue()));

        Table backTable = new Table();
        backTable.setName(SettingsOption.BACK.name());
        TextButton back = new TextButton("Back", skin);
        backTable.add(back).width(180f);
        optionsTable.add(backTable).padTop(15f).row();

        onClick(back, viewModel::close);
        onEnter(back, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        add(contentTable).expand().center();
        align(Align.center);
    }

    private Slider setupVolumesSlider(Table contentTable, String title, SettingsOption option) {
        Table table = new Table();
        table.setName(option.name());

        Label label = new Label(title, skin, "text_12");
        label.setColor(skin.getColor("sand"));
        table.add(label).row();

        Slider slider = new Slider(0f, 1f, .05f, false, skin);
        table.add(slider);

        contentTable.add(table).padTop(10f).row();

        onEnter(table, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));
        return slider;
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_RIGHT, Boolean.class, this::onRight);
        viewModel.onPropertyChange(Constants.ON_LEFT, Boolean.class, this::onLeft);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
        viewModel.onPropertyChange(Constants.ON_CANCEL, Boolean.class, (ignored) -> viewModel.close());
    }

    private void onDown(Object o) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    private void onUp(Object o) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    private void onRight(Object ignored) {
        SettingsOption opt = SettingsOption.valueOf(selectedItem.getName());
        if (opt == SettingsOption.MUSIC_VOLUME || opt == SettingsOption.SOUND_VOLUME) {
            Slider slider = (Slider) selectedItem.getChild(1);
            slider.setValue(slider.getValue() + slider.getStepSize());
        }
    }

    private void onLeft(Object ignored) {
        SettingsOption opt = SettingsOption.valueOf(selectedItem.getName());
        if (opt == SettingsOption.MUSIC_VOLUME || opt == SettingsOption.SOUND_VOLUME) {
            Slider slider = (Slider) selectedItem.getChild(1);
            slider.setValue(slider.getValue() - slider.getStepSize());
        }
    }

    private void onSelect(Object ignored) {
        if (selectedItem == null) return;
        SettingsOption opt = SettingsOption.valueOf(selectedItem.getName());
        if (opt == SettingsOption.BACK) {
            viewModel.close();
        }
    }

    public void resetSelection() {
        Group first = findActor(SettingsOption.MUSIC_VOLUME.name());
        if (first != null) {
            selectedItem = viewModel.getUiServices().selectMenuItem(first);
        }
    }

    private enum SettingsOption {
        MUSIC_VOLUME,
        SOUND_VOLUME,
        BACK
    }
}
