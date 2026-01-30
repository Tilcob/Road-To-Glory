package com.github.tilcob.game.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ContentHotReload {
    private static final String TAG = ContentHotReload.class.getSimpleName();

    private final ContentReloadService reloadService;
    private final List<FileHandle> watchRoots;
    private final float pollIntervalSeconds;
    private float pollTimer = 0f;
    private long lastKnownTimestamp;
    private final Deque<Long> recentReloads = new ArrayDeque<>();
    private final int maxReloadHistory = 5;

    public ContentHotReload(ContentReloadService reloadService, List<FileHandle> watchRoots) {
        this(reloadService, watchRoots, 1.0f);
    }

    public ContentHotReload(ContentReloadService reloadService, List<FileHandle> watchRoots, float pollIntervalSeconds) {
        this.reloadService = reloadService;
        this.watchRoots = watchRoots;
        this.pollIntervalSeconds = Math.max(0.2f, pollIntervalSeconds);
        this.lastKnownTimestamp = scanNewestTimestamp();
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            triggerReload("Manual (F5)");
            return;
        }

        pollTimer += delta;
        if (pollTimer < pollIntervalSeconds) return;
        pollTimer = 0f;

        long newest = scanNewestTimestamp();
        if (newest > lastKnownTimestamp) {
            lastKnownTimestamp = newest;
            triggerReload("Auto (file changed)");
        }
    }

    private void triggerReload(String reason) {
        long now = System.currentTimeMillis();
        recentReloads.addLast(now);
        while (recentReloads.size() > maxReloadHistory) recentReloads.removeFirst();
        if (recentReloads.size() >= 3) {
            long first = recentReloads.getFirst();
            long last = recentReloads.getLast();
            if (last - first < 250) {
                return;
            }
        }

        try {
            reloadService.reloadAll();
            lastKnownTimestamp = scanNewestTimestamp();
            Gdx.app.log(TAG, "Reload OK: " + reason);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Reload FAILED: " + reason, e);
        }
    }

    private long scanNewestTimestamp() {
        long newest = 0;
        for (FileHandle root : watchRoots) {
            newest = Math.max(newest, scan(root));
        }
        return newest;
    }

    private long scan(FileHandle handle) {
        if (handle == null || !handle.exists()) return 0;

        long best = safeLastModified(handle);
        if (handle.isDirectory()) {
            FileHandle[] list = handle.list();
            if (list != null) {
                for (FileHandle child : list) {
                    best = Math.max(best, scan(child));
                }
            }
        }
        return best;
    }

    private long safeLastModified(FileHandle handle) {
        try {
            return handle.lastModified();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
