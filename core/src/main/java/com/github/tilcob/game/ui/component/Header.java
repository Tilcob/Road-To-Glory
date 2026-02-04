package com.github.tilcob.game.ui.component;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Header {
    private final Table table;

    public Header(Skin skin, String title) {
        this(skin, title, null, "text_12", "BLACK");
    }

    public Header(Skin skin, String title, String subtitle, String styleName, String color) {
        table = new Table();
        Label titleLabel = new Label(title, skin, styleName);
        titleLabel.setColor(skin.getColor(color));
        table.add(titleLabel).row();

        if (subtitle != null && !subtitle.isBlank()) {
            Label subtitleLabel = new Label(subtitle, skin, "text_08");
            subtitleLabel.setColor(skin.getColor(color));
            table.add(subtitleLabel).padTop(4f).row();
        }
    }

    public Table getTable() {
        return table;
    }

    public Group getRoot() {
        return table;
    }
}
