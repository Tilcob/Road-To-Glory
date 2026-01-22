package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StartDialogEvent;
import com.github.tilcob.game.input.Command;

import java.util.Locale;
import java.util.function.Supplier;

public class CutsceneYarnBridge {
    private final GameEventBus eventBus;
    private final Supplier<EntityLookup> entityLookup;
    private final Vector2 tmpVector = new Vector2();

    public CutsceneYarnBridge(GameEventBus eventBus, Supplier<EntityLookup> entityLookup) {
        this.eventBus = eventBus;
        this.entityLookup = entityLookup;
    }

    public void registerAll(YarnRuntime runtime) {
        registerCommands(runtime);
    }

    private void registerCommands(YarnCommandRegistry registry) {
        registry.register("lock_player", this::lockPlayer);
        registry.register("unlock_player", this::unlockPlayer);
        registry.register("camera_move", this::cameraMove);
        registry.register("camera_move_back", this::cameraMoveBack);
        registry.register("play_anim", this::playAnimation);
        registry.register("move_to", this::moveTo);
        registry.register("face", this::faceEntity);
        registry.register("fade_in", this::fadeIn);
        registry.register("fade_out", this::fadeOut);
        registry.register("start_dialog", this::startDialog);
        registry.register("set_flag", this::setFlag);
    }

    private void lockPlayer(Entity player, String[] args) {
        if (player == null) return;
        if (PlayerInputLock.MAPPER.get(player) == null) {
            player.add(PlayerInputLock.allow(Command.INTERACT, Command.PAUSE));
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
        player.remove(CameraPanHome.class);
        player.add(new CameraPan(x, y, duration));
    }

    private void cameraMoveBack(Entity player, String[] args) {
        if (player == null) return;
        float duration = args != null && args.length > 0
            ? MathUtils.clamp(parseFloat(args[0], 0f), 0f, 60f)
            : 0f;
        CameraPanHome cameraPanHome = CameraPanHome.MAPPER.get(player);
        if (cameraPanHome == null) return;
        player.remove(CameraPan.class);
        player.add(new CameraPan(cameraPanHome.getPosition().x, cameraPanHome.getPosition().y, duration));
        player.remove(CameraPanHome.class);
    }

    private void playAnimation(Entity player, String[] args) {
        if (args == null || args.length < 2) {
            return;
        }
        Entity target = resolveEntity(player, args[0]);
        if (target == null) {
            return;
        }
        Animation2D animation2D = Animation2D.MAPPER.get(target);
        if (animation2D == null) {
            return;
        }
        Animation2D.AnimationType type = parseAnimationType(args[1]);
        if (type == null) {
            return;
        }
        Animation.PlayMode playMode = parsePlayMode(args.length > 2 ? args[2] : null, type);
        animation2D.setType(type);
        animation2D.setPlayMode(playMode);
    }

    private void moveTo(Entity player, String[] args) {
        if (args == null || args.length < 3) {
            return;
        }
        Entity target = resolveEntity(player, args[0]);
        if (target == null) {
            return;
        }
        float x = parseFloat(args[1], Float.NaN);
        float y = parseFloat(args[2], Float.NaN);
        if (Float.isNaN(x) || Float.isNaN(y)) {
            return;
        }
        float arrivalDistance = args.length > 3 ? Math.max(0f, parseFloat(args[3], 0f)) : 0.1f;
        MoveIntent intent = MoveIntent.MAPPER.get(target);
        if (intent == null) {
            intent = new MoveIntent();
            target.add(intent);
        }
        intent.setTarget(tmpVector.set(x, y), arrivalDistance);
    }

    private void faceEntity(Entity player, String[] args) {
        if (args == null || args.length < 2) {
            return;
        }
        Entity target = resolveEntity(player, args[0]);
        if (target == null) {
            return;
        }
        Facing.FacingDirection direction = parseFacing(args[1]);
        if (direction == null) {
            return;
        }
        Facing facing = Facing.MAPPER.get(target);
        if (facing == null) {
            facing = new Facing(direction);
            target.add(facing);
        } else {
            facing.setDirection(direction);
        }
    }

    private void fadeIn(Entity player, String[] args) {
        applyFade(player, 0f, args);
    }

    private void fadeOut(Entity player, String[] args) {
        applyFade(player, 1f, args);
    }

    private void startDialog(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        String npcId = args[0];
        String nodeId = args.length > 1 ? args[1] : null;
        if (npcId == null || npcId.isBlank()) return;
        eventBus.fire(new StartDialogEvent(player, npcId, nodeId));
    }

    private void setFlag(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) {
            return;
        }
        String flag = args[0];
        boolean value = args.length <= 1 || Boolean.parseBoolean(args[1]);
        if (flag == null || flag.isBlank()) {
            return;
        }
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        flags.set(flag, value);
    }

    private void applyFade(Entity player, float targetAlpha, String[] args) {
        if (player == null) {
            return;
        }
        float duration = args != null && args.length > 0
            ? MathUtils.clamp(parseFloat(args[0], 0f), 0f, 60f)
            : 0f;
        ScreenFade fade = ScreenFade.MAPPER.get(player);
        if (fade == null) {
            float initialAlpha = targetAlpha <= 0f ? 1f : 0f;
            fade = new ScreenFade(initialAlpha);
            player.add(fade);
        }
        fade.start(targetAlpha, duration);
    }

    private Entity resolveEntity(Entity player, String entityId) {
        if (player == null) {
            return null;
        }
        EntityLookup lookup = entityLookup == null ? null : entityLookup.get();
        if (lookup != null) {
            Entity resolved = lookup.find(player, entityId);
            if (resolved != null) {
                return resolved;
            }
        }
        if (entityId == null || entityId.isBlank() || "player".equalsIgnoreCase(entityId)) {
            return player;
        }
        return null;
    }

    private Animation2D.AnimationType parseAnimationType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return Animation2D.AnimationType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Animation.PlayMode parsePlayMode(String value, Animation2D.AnimationType type) {
        if (value == null || value.isBlank()) {
            return switch (type) {
                case IDLE, WALK -> Animation.PlayMode.LOOP;
                default -> Animation.PlayMode.NORMAL;
            };
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("LOOP".equals(normalized)) {
            return Animation.PlayMode.LOOP;
        }
        if ("NORMAL".equals(normalized)) {
            return Animation.PlayMode.NORMAL;
        }
        return switch (type) {
            case IDLE, WALK -> Animation.PlayMode.LOOP;
            default -> Animation.PlayMode.NORMAL;
        };
    }

    private Facing.FacingDirection parseFacing(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return Facing.FacingDirection.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
