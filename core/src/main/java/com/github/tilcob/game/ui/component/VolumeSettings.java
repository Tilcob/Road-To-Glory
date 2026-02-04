package com.github.tilcob.game.ui.component;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.tilcob.game.ui.view.View;

public class VolumeSettings {
    private final Skin skin;

    public VolumeSettings(Skin skin) {
        this.skin = skin;
    }

    public VolumeSection create(Table contentTable, String title, String optionName, View.OnActorEvent<Table> onEnter) {
        Table table = new Table();
        table.setName(optionName);

        Label label = new Label(title, skin, "text_12");
        label.setColor(skin.getColor("sand"));
        table.add(label).row();

        Slider slider = new Slider(0f, 1f, .05f, false, skin);
        table.add(slider);

        contentTable.add(table).padTop(10f).row();

        if (onEnter != null) {
            View.onEnter(table, onEnter);
        }

        return new VolumeSection(table, slider);
    }

    public record VolumeSection(Table table, Slider slider) {
    }
}
