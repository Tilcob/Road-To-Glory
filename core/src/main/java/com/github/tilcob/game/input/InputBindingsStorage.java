package com.github.tilcob.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class InputBindingsStorage {
    private final String defaultsPath;
    private final String localPath;
    private final Json json;

    public InputBindingsStorage(String defaultsPath, String localPath) {
        this.defaultsPath = defaultsPath;
        this.localPath = localPath;
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
    }

    public InputBindings load() {
        FileHandle localFile = Gdx.files.local(localPath);
        if (localFile.exists()) {
            return readBindings(localFile);
        }
        FileHandle defaultsFile = Gdx.files.internal(defaultsPath);
        InputBindings bindings = readBindings(defaultsFile);
        save(bindings);
        return bindings;
    }

    public void save(InputBindings bindings) {
        if (bindings == null) {
            return;
        }
        FileHandle localFile = Gdx.files.local(localPath);
        json.toJson(bindings.toBindingFile(), localFile);
    }

    private InputBindings readBindings(FileHandle file) {
        try {
            InputBindings.BindingFile data = json.fromJson(InputBindings.BindingFile.class, file);
            return InputBindings.fromBindingFile(data);
        } catch (Exception ex) {
            return InputBindings.defaultBindings();
        }
    }
}
