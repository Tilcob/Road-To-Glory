package com.github.tilcob.game.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;

import java.util.List;

public class ContentHotReload {
    private static final String TAG = ContentHotReload.class.getSimpleName();

    private final List<FileHandle> watchFiles;
    private final float pollIntervalSeconds;
    private float pollTimer = 0f;
    private long lastTimestamp;
    private boolean reloadRequested;
    private boolean reloading;
    private float cooldownSeconds = 0.35f;
    private float cooldownTimer = 0f;

    public ContentHotReload(List<FileHandle> watchFiles) {
        this(watchFiles, 1.0f);
    }

    public ContentHotReload(List<FileHandle> watchFiles, float pollIntervalSeconds) {
        this.watchFiles = watchFiles;
        this.pollIntervalSeconds = Math.max(0.2f, pollIntervalSeconds);
        this.lastTimestamp = newestTimestamp();
    }

    public void update(float delta) {
        cooldownTimer = Math.max(0f, cooldownTimer - delta);
        if (reloading) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            requestReload("Manual reload (F5)");
            return;
        }

        pollTimer += delta;
        if (pollTimer < pollIntervalSeconds) return;
        pollTimer = 0f;

        long now = newestTimestamp();
        if (now > lastTimestamp) {
            lastTimestamp = now;
            requestReload("Auto reload (index changed)");
        }
    }

    public boolean consumeReloadRequested() {
        if (!reloadRequested) return false;
        reloadRequested = false;
        return true;
    }

    public void beginReload() {
        reloading = true;
    }

    public void endReload() {
        reloading = false;
        cooldownTimer = cooldownSeconds;
        lastTimestamp = newestTimestamp();
    }

    private void requestReload(String reason) {
        if (cooldownTimer > 0f) return;
        reloadRequested = true;
        lastTimestamp = newestTimestamp();
        Gdx.app.log(TAG, reason);
    }

    private long newestTimestamp() {
        long newest = 0;
        for (FileHandle f : watchFiles) {
            if (f == null || !f.exists()) continue;
            try {
                newest = Math.max(newest, f.lastModified());
            } catch (Exception ignored) {
            }
        }
        return newest;
    }
}
