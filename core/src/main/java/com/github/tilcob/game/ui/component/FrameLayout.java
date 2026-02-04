package com.github.tilcob.game.ui.component;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class FrameLayout {
    private final Table root;

    public FrameLayout(Skin skin, float padding) {
        root = new Table();
        root.setBackground(skin.getDrawable("frame"));
        root.pad(padding);
    }

    public FrameLayout(Skin skin, float padTop, float padLeft, float padBottom, float padRight) {
        root = new Table();
        root.setBackground(skin.getDrawable("frame"));
        root.pad(padTop, padLeft, padBottom, padRight);
    }

    public Table getRoot() {
        return root;
    }
}
