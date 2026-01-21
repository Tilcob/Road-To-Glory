package com.github.tilcob.game.ui.view;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.save.SaveSlot;

public class DebugOverlayView extends Table {
    private final Engine engine;
    private final GameServices services;
    private final Label fpsLabel;
    private final Label entityLabel;
    private final Label systemLabel;
    private final Label saveSlotLabel;

    public DebugOverlayView(Skin skin, Engine engine, GameServices services) {
        super(skin);
        this.engine = engine;
        this.services = services;
        this.fpsLabel = new Label("", skin, "text_08");
        this.entityLabel = new Label("", skin, "text_08");
        this.systemLabel = new Label("", skin, "text_08");
        this.saveSlotLabel = new Label("", skin, "text_08");

        setTouchable(Touchable.disabled);
        setFillParent(true);
        align(Align.topRight);
        pad(8f);
        defaults().right();

        for (Label label : new Label[] { fpsLabel, entityLabel, systemLabel, saveSlotLabel }) {
            label.setColor(Color.WHITE);
            add(label).row();
        }

        pack();
    }

    public void update() {
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        entityLabel.setText("Entities: " + engine.getEntities().size());
        systemLabel.setText("Systems: " + engine.getSystems().size());
        SaveSlot slot = services.getSaveService().getActiveSlot();
        saveSlotLabel.setText("Save Slot: " + (slot == null ? "legacy" : slot.name()));
        pack();
    }
}
