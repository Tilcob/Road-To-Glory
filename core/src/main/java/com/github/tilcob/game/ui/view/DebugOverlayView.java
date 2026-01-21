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
import com.github.tilcob.game.debug.DebugLogBuffer;
import com.github.tilcob.game.save.SaveSlot;

public class DebugOverlayView extends Table {
    private final Engine engine;
    private final GameServices services;
    private final Label consoleLabel;
    private final Label fpsLabel;
    private final Label entityLabel;
    private final Label systemLabel;
    private final Label saveSlotLabel;
    private final Table statsTable;
    private final Table consoleTable;

    public DebugOverlayView(Skin skin, Engine engine, GameServices services) {
        super(skin);
        this.engine = engine;
        this.services = services;
        this.fpsLabel = new Label("", skin, "text_08");
        this.entityLabel = new Label("", skin, "text_08");
        this.systemLabel = new Label("", skin, "text_08");
        this.saveSlotLabel = new Label("", skin, "text_08");
        this.consoleLabel = new Label("", skin, "text_08");
        this.statsTable = new Table(skin);
        this.consoleTable = new Table(skin);

        setTouchable(Touchable.disabled);
        setFillParent(true);
        pad(8f);

        statsTable.align(Align.topRight);
        statsTable.defaults().right();

        for (Label label : new Label[] { fpsLabel, entityLabel, systemLabel, saveSlotLabel }) {
            label.setColor(Color.WHITE);
            statsTable.add(label).row();
        }

        consoleLabel.setColor(Color.WHITE);
        consoleLabel.setWrap(true);
        consoleLabel.setAlignment(Align.bottomLeft);

        consoleTable.align(Align.bottomLeft);
        consoleTable.add(consoleLabel).width(420f).left().bottom();

        add(statsTable).expand().top().right();
        row();
        add(consoleTable).expand().bottom().left();
    }

    public void update() {
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        entityLabel.setText("Entities: " + engine.getEntities().size());
        systemLabel.setText("Systems: " + engine.getSystems().size());
        SaveSlot slot = services.getSaveService().getActiveSlot();
        saveSlotLabel.setText("Save Slot: " + (slot == null ? "legacy" : slot.name()));
        DebugLogBuffer buffer = DebugLogBuffer.getActive();
        if (buffer != null) consoleLabel.setText(String.join("\n", buffer.getLines()));
        pack();
    }
}
