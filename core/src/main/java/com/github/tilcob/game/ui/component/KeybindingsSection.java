package com.github.tilcob.game.ui.component;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.ui.view.View;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeybindingsSection {
    public static final String KEYBIND_PREFIX = "KEYBIND_";

    private final Skin skin;
    private final Table keybindingsTable;
    private final Map<Command, TextButton> keybindButtons;
    private final Label statusLabel;

    public KeybindingsSection(Skin skin) {
        this.skin = skin;
        this.keybindingsTable = new Table();
        this.keybindButtons = new EnumMap<>(Command.class);
        this.statusLabel = new Label("", skin, "text_12");
        this.statusLabel.setColor(skin.getColor("sand"));
        this.statusLabel.setAlignment(Align.center);
    }

    public void build(
        Table optionsTable,
        Iterable<Command> commands,
        Function<Command, String> bindingLabelProvider,
        Consumer<Command> onCommandClicked,
        Runnable onReset,
        View.OnActorEvent<Table> onRowEnter,
        String resetOptionName
    ) {
        if (keybindingsTable.hasParent()) {
            keybindingsTable.remove();
        }
        keybindingsTable.clear();
        keybindButtons.clear();
        statusLabel.setText("");
        keybindingsTable.top();

        Label header = new Label("Keybindings", skin, "text_12");
        header.setColor(skin.getColor("sand"));
        keybindingsTable.add(header).padTop(20f).row();

        for (Command command : commands) {
            Table row = new Table();
            row.setName(keybindOptionName(command));

            Label label = new Label(formatCommandName(command), skin, "text_12");
            label.setColor(skin.getColor("sand"));
            row.add(label).left().padRight(12f);

            TextButton button = new TextButton(bindingLabelProvider.apply(command), skin);
            button.getLabel().setAlignment(Align.center);
            row.add(button).width(220f);
            keybindButtons.put(command, button);

            if (onCommandClicked != null) {
                View.onClick(button, () -> onCommandClicked.accept(command));
            }
            if (onRowEnter != null) {
                View.onEnter(row, onRowEnter);
            }

            keybindingsTable.add(row).padTop(6f).growX().row();
        }

        TextButton resetButton = new TextButton("Reset to defaults", skin);
        Table resetRow = new Table();
        resetRow.setName(resetOptionName);
        resetRow.add(resetButton).width(220f);
        if (onReset != null) {
            View.onClick(resetButton, onReset::run);
        }
        if (onRowEnter != null) {
            View.onEnter(resetRow, onRowEnter);
        }

        keybindingsTable.add(statusLabel).padTop(6f).row();
        keybindingsTable.add(resetRow).padTop(8f).row();

        optionsTable.add(keybindingsTable).growX().row();
    }

    public Map<Command, TextButton> getKeybindButtons() {
        return keybindButtons;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Table getTable() {
        return keybindingsTable;
    }

    private String formatCommandName(Command command) {
        String name = command.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String keybindOptionName(Command command) {
        return KEYBIND_PREFIX + command.name();
    }
}
