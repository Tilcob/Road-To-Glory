package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.ui.model.SettingsViewModel;

import java.util.EnumMap;
import java.util.Map;

public class SettingsView extends View<SettingsViewModel> {
    private Map<Command, TextButton> keybindButtons;
    private Label keybindStatusLabel;
    private Command listeningCommand;
    private Group selectedItem;
    private Table keybindingsTable;

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
        optionsTable.top();
        ScrollPane scrollPane = new ScrollPane(optionsTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setForceScroll(false, true);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setFlickScroll(true);
        scrollPane.setSmoothScrolling(true);
        float scrollHeight = 260f;
        if (stage != null && stage.getViewport() != null) {
            scrollHeight = Math.max(200f, stage.getViewport().getWorldHeight() - 160f);
        }
        contentTable.add(scrollPane).height(scrollHeight).width(520f).row();

        Slider musicSlider = setupVolumesSlider(optionsTable, "Music Volume", SettingsOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, s -> viewModel.setMusicVolume(s.getValue()));

        Slider soundSlider = setupVolumesSlider(optionsTable, "Sound Volume", SettingsOption.SOUND_VOLUME);
        soundSlider.setValue(viewModel.getSoundVolume());
        onChange(soundSlider, s -> viewModel.setSoundVolume(s.getValue()));

        if (keybindButtons == null) {
            keybindButtons = new EnumMap<>(Command.class);
        }
        setupKeybindingsSection(optionsTable);
        setupKeybindingsSection(optionsTable);

        Table backTable = new Table();
        backTable.setName(SettingsOption.BACK.name());
        TextButton back = new TextButton("Back", skin);
        backTable.add(back).width(180f);
        contentTable.add(backTable).padTop(15f).row();

        onClick(back, viewModel::close);
        onEnter(back, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        add(contentTable).expand().center();
        align(Align.center);
    }

    private void setupKeybindingsSection(Table optionsTable) {
        if (keybindingsTable != null) {
            keybindingsTable.remove();
            keybindingsTable.clear();
        }
        keybindButtons.clear();

        keybindingsTable = new Table();
        keybindingsTable.top();

        Label header = new Label("Keybindings", skin, "text_12");
        header.setColor(skin.getColor("sand"));
        keybindingsTable.add(header).padTop(20f).row();

        for (Command command : viewModel.getBindableCommands()) {
            Table row = new Table();
            row.setName(keybindOptionName(command));

            Label label = new Label(formatCommandName(command), skin, "text_12");
            label.setColor(skin.getColor("sand"));
            row.add(label).left().padRight(12f);

            TextButton button = new TextButton(viewModel.getBindingLabel(command), skin);
            button.getLabel().setAlignment(Align.center);
            row.add(button).width(220f);
            keybindButtons.put(command, button);

            onClick(button, () -> viewModel.startListening(command));
            onEnter(row, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

            keybindingsTable.add(row).padTop(6f).growX().row();
        }

        keybindStatusLabel = new Label("", skin, "text_12");
        keybindStatusLabel.setColor(skin.getColor("sand"));
        keybindStatusLabel.setAlignment(Align.center);

        TextButton resetButton = new TextButton("Reset to defaults", skin);
        Table resetRow = new Table();
        resetRow.setName(SettingsOption.RESET_KEYBINDS.name());
        resetRow.add(resetButton).width(220f);
        onClick(resetButton, viewModel::resetToDefaults);
        onEnter(resetRow, item -> selectedItem = viewModel.getUiServices().selectMenuItem(item));

        keybindingsTable.add(keybindStatusLabel).padTop(6f).row();
        keybindingsTable.add(resetRow).padTop(8f).row();

        optionsTable.add(keybindingsTable).growX().row();
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
        viewModel.onPropertyChange(Constants.KEYBIND_LISTENING, Command.class, this::onListeningChanged);
        viewModel.onPropertyChange(Constants.KEYBIND_SAVED, Integer.class, this::onKeybindSaved);
        viewModel.onPropertyChange(Constants.KEYBIND_CONFLICT, String.class, this::onKeybindConflict);
    }

    private void onDown(Object o) {
        selectedItem = viewModel.getUiServices().moveDown(selectedItem);
    }

    private void onUp(Object o) {
        selectedItem = viewModel.getUiServices().moveUp(selectedItem);
    }

    private void onRight(Object ignored) {
        if (selectedItem == null) return;
        String name = selectedItem.getName();
        if (parseKeybindCommand(name) != null) {
            return;
        }
        SettingsOption opt = SettingsOption.valueOf(name);
        if (opt == SettingsOption.MUSIC_VOLUME || opt == SettingsOption.SOUND_VOLUME) {
            Slider slider = (Slider) selectedItem.getChild(1);
            slider.setValue(slider.getValue() + slider.getStepSize());
        }
    }

    private void onLeft(Object ignored) {
        if (selectedItem == null) return;
        String name = selectedItem.getName();
        if (parseKeybindCommand(name) != null) {
            return;
        }
        SettingsOption opt = SettingsOption.valueOf(name);
        if (opt == SettingsOption.MUSIC_VOLUME || opt == SettingsOption.SOUND_VOLUME) {
            Slider slider = (Slider) selectedItem.getChild(1);
            slider.setValue(slider.getValue() - slider.getStepSize());
        }
    }

    private void onSelect(Object ignored) {
        if (selectedItem == null) return;
        Command command = parseKeybindCommand(selectedItem.getName());
        if (command != null) {
            viewModel.startListening(command);
            return;
        }
        SettingsOption opt = SettingsOption.valueOf(selectedItem.getName());
        switch (opt) {
            case BACK -> viewModel.close();
            case RESET_KEYBINDS -> viewModel.resetToDefaults();
            default -> {
            }
        }
    }

    public void resetSelection() {
        Group first = findActor(SettingsOption.MUSIC_VOLUME.name());
        if (first != null) {
            selectedItem = viewModel.getUiServices().selectMenuItem(first);
        }
    }

    public void setOverlayVisible(boolean visible) {
        setVisibleBound(visible);
    }

    private void onListeningChanged(Command command) {
        listeningCommand = command;
        for (Map.Entry<Command, TextButton> entry : keybindButtons.entrySet()) {
            if (entry.getKey() == listeningCommand) {
                entry.getValue().setText("Press a key...");
            } else {
                entry.getValue().setText(viewModel.getBindingLabel(entry.getKey()));
            }
        }
        if (listeningCommand != null) {
            keybindStatusLabel.setText("Press a key...");
        } else {
            keybindStatusLabel.setText("");
        }
    }

    private void onKeybindSaved(Integer keycode) {
        for (Map.Entry<Command, TextButton> entry : keybindButtons.entrySet()) {
            entry.getValue().setText(viewModel.getBindingLabel(entry.getKey()));
        }
        if (keycode != null) {
            keybindStatusLabel.setText("Keybinding updated.");
        } else if (listeningCommand == null) {
            keybindStatusLabel.setText("");
        }
    }

    private void onKeybindConflict(String message) {
        if (message == null || message.isBlank()) {
            if (listeningCommand != null) {
                keybindStatusLabel.setText("Press a key...");
            } else {
                keybindStatusLabel.setText("");
            }
        } else {
            keybindStatusLabel.setText(message);
        }
    }

    private String formatCommandName(Command command) {
        String name = command.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String keybindOptionName(Command command) {
        return "KEYBIND_" + command.name();
    }

    private Command parseKeybindCommand(String name) {
        if (name == null || !name.startsWith("KEYBIND_")) {
            return null;
        }
        try {
            return Command.valueOf(name.substring("KEYBIND_".length()));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private enum SettingsOption {
        MUSIC_VOLUME,
        SOUND_VOLUME,
        RESET_KEYBINDS,
        BACK
    }
}
