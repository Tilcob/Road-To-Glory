package com.github.tilcob.game.save;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tilcob.game.save.states.GameState;
import java.io.File;
import java.io.IOException;

public class SaveManager {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);;

    private final File saveFile;

    public SaveManager(String path) {
        this.saveFile = new File(path);
    }

    public void save(GameState gameState) throws IOException {
        MAPPER.writeValue(saveFile, gameState);
    }

    public GameState load() throws IOException {
        if (!saveFile.exists()) {
            throw new IOException("Savegame doesn't exist");
        }
        GameState state = MAPPER.readValue(saveFile, GameState.class);

        //migrateIfNeeded(state);

        return state;
    }

    private void migrateIfNeeded(GameState state) {
        int version = state.getSaveVersion();

        // TODO
    }

    public boolean exists() {
        return saveFile.exists();
    }
}
