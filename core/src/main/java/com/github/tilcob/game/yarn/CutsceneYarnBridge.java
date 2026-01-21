package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.component.CameraPan;
import com.github.tilcob.game.component.PlayerInputLock;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StartDialogEvent;

public class CutsceneYarnBridge {
    private final GameEventBus eventBus;

    public CutsceneYarnBridge(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void registerAll(YarnRuntime runtime) {
        registerCommands(runtime);
    }

    private void registerCommands(YarnCommandRegistry registry) {
        registry.register("lock_player", this::lockPlayer);
        registry.register("unlock_player", this::unlockPlayer);
        registry.register("camera_move", this::cameraMove);
        registry.register("start_dialog", this::startDialog);
    }

    private void lockPlayer(Entity player, String[] args) {
        if (player == null) return;
        if (PlayerInputLock.MAPPER.get(player) == null) {
            player.add(new PlayerInputLock());
        }
    }

    private void unlockPlayer(Entity player, String[] args) {
        if (player == null) return;
        player.remove(PlayerInputLock.class);
    }

    private void cameraMove(Entity player, String[] args) {
        if (player == null || args == null || args.length < 2) return;
        float x = parseFloat(args[0], Float.NaN);
        float y = parseFloat(args[1], Float.NaN);
        if (Float.isNaN(x) || Float.isNaN(y)) return;
        float duration = args.length > 2 ? MathUtils.clamp(parseFloat(args[2], 0f), 0f, 60f) : 0f;
        player.remove(CameraPan.class);
        player.add(new CameraPan(x, y, duration));
    }

    private void startDialog(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        String npcId = args[0];
        String nodeId = args.length > 1 ? args[1] : null;
        if (npcId == null || npcId.isBlank()) return;
        eventBus.fire(new StartDialogEvent(player, npcId, nodeId));
    }

    private float parseFloat(String value, float fallback) {
        if (value == null) return fallback;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
