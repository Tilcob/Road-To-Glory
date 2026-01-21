package com.github.tilcob.game.save;

import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;

import java.io.IOException;

public class SaveService {
    private final SaveManager saveManager;
    private final StateManager stateManager;
    private final ChestRegistry chestRegistry;

    public SaveService(SaveManager saveManager, StateManager stateManager, ChestRegistry chestRegistry) {
        this.saveManager = saveManager;
        this.stateManager = stateManager;
        this.chestRegistry = chestRegistry;
    }

    public void loadGame() {
        if (saveManager.exists()) {
            try {
                GameState loaded = saveManager.load();
                stateManager.setGameState(loaded);
                chestRegistry.loadFromState(stateManager.loadChestRegistryState());
            } catch (IOException e) {
                Gdx.app.error("SaveService", "Error loading state: " + e.getMessage());
            }
        } else {
            stateManager.setGameState(new GameState());
        }
    }

    public void saveGame() {
        try {
            stateManager.saveChestRegistryState(chestRegistry.toState());
            saveManager.save(stateManager.getGameState());
        } catch (IOException e) {
            Gdx.app.error("SaveService", "Error saving state: " + e.getMessage());
        }
    }

    public void setActiveSlot(SaveSlot slot) {
        saveManager.setActiveSlot(slot);
    }

    public SaveSlot getActiveSlot() {
        return saveManager.getActiveSlot();
    }

    public java.util.List<SaveSlotInfo> listSlots() {
        return saveManager.listSlots();
    }
}
