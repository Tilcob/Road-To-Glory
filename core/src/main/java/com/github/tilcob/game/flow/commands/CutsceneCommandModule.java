package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.assets.MusicAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.component.Animation2D;
import com.github.tilcob.game.component.Facing;
import com.github.tilcob.game.component.ScreenFade;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;
import com.github.tilcob.game.yarn.EntityLookup;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class CutsceneCommandModule {
    public static final float MIN_ARRIVAL_DISTANCE = .1f;

    private final Supplier<EntityLookup> entityLookup;

    public CutsceneCommandModule(Supplier<EntityLookup> entityLookup) {
        this.entityLookup = entityLookup;
    }

    public void register(CommandRegistry registry) {
        registry.register("lock_player", (call, ctx) ->
            List.of(new FlowAction.EmitEvent(new LockPlayerEvent(ctx.player()))));

        registry.register("unlock_player", (call, ctx) ->
            List.of(new FlowAction.EmitEvent(new UnlockPlayerEvent(ctx.player()))));

        registry.register("camera_move", (call, ctx) -> {
            float x = parseFloat(call.arguments().get(0), 0f);
            float y = parseFloat(call.arguments().get(1), 0f);
            float duration = parseFloat(call.arguments().get(2), 0f) == 0f
                ? 0f
                : MathUtils.clamp(parseFloat(call.arguments().get(2), 0f), 0f, 60f);
            return List.of(new FlowAction.EmitEvent(new CameraMoveEvent(ctx.player(), x, y, duration)));
        });

        registry.register("camera_move_relative", (call, ctx) -> {
            float x = parseFloat(call.arguments().get(0), 0f);
            float y = parseFloat(call.arguments().get(1), 0f);
            float duration = parseFloat(call.arguments().get(2), 0f) == 0f
                ? 0f
                : MathUtils.clamp(parseFloat(call.arguments().get(2), 0f), 0f, 60f);
            return List.of(new FlowAction.EmitEvent(new CameraMoveRelativeEvent(ctx.player(), x, y, duration)));
        });

        registry.register("camera_move_back", (call, ctx) -> {
            float duration = parseFloat(call.arguments().get(0), 0f) == 0f
                ? 0f
                : MathUtils.clamp(parseFloat(call.arguments().get(0), 0f), 0f, 60f);
            return List.of(new FlowAction.EmitEvent(new CameraMoveBackEvent(ctx.player(), duration)));
        });

        registry.register("play_anim", (call, ctx) -> {
            Entity target = resolveEntity(ctx.player(), call.arguments().get(0));
            Animation2D.AnimationType type = parseAnimationType(call.arguments().get(1));
            Animation.PlayMode playMode = parsePlayMode(
                call.arguments().size() > 2 ? call.arguments().get(2) : null, type
            );
            return List.of(new FlowAction.EmitEvent(new PlayAnimationEvent(ctx.player(), target, type, playMode)));
        });

        registry.register("move_to", (call, ctx) -> {
            Entity target = resolveEntity(ctx.player(), call.arguments().get(0));
            float x = parseFloat(call.arguments().get(1), 0f);
            float y = parseFloat(call.arguments().get(2), 0f);
            if (call.arguments().size() > 3) {
                float arrivalDistance = Math.max(MIN_ARRIVAL_DISTANCE, parseFloat(call.arguments().get(3), MIN_ARRIVAL_DISTANCE));
                return List.of(new FlowAction.EmitEvent(new MoveToEvent(ctx.player(), target, x, y, arrivalDistance)));
            }
            return List.of(new FlowAction.EmitEvent(new MoveToEvent(ctx.player(), target, x, y)));
        });

        registry.register("face", (call, ctx) -> {
            Entity target = resolveEntity(ctx.player(), call.arguments().get(0));
            Facing.FacingDirection direction = parseFacing(call.arguments().get(1));
            return List.of(new FlowAction.EmitEvent(new FaceEntityEvent(ctx.player(), target, direction)));
        });

        registry.register("fade_in", (call, ctx) -> {
            float duration = MathUtils.clamp(parseFloat(call.arguments().get(0), 0f), 0f, 60f);
            return List.of(new FlowAction.EmitEvent(new FadeInEvent(ctx.player(), duration)));
        });

        registry.register("fade_out", (call, ctx) -> {
            float duration = MathUtils.clamp(parseFloat(call.arguments().get(0), 0f), 0f, 60f);
            return List.of(new FlowAction.EmitEvent(new FadeOutEvent(ctx.player(), duration)));
        });

        registry.register("start_dialog", (call, ctx) -> {
            String npcId = call.arguments().get(0);
            String nodeId = call.arguments().get(1);
            return List.of(new FlowAction.EmitEvent(new StartDialogEvent(ctx.player(), npcId, nodeId)));
        });

        registry.register("set_flag", (call, ctx) -> {
            String flag = call.arguments().get(0);
            boolean value = call.arguments().size() < 2 || Boolean.parseBoolean(call.arguments().get(1));
            return List.of(new FlowAction.EmitEvent(new SetFlagEvent(ctx.player(), flag, value)));
        });

        registry.register("play_music", (call, ctx) -> {
            MusicAsset musicAsset = parseMusicAsset(call.arguments().get(0));
            return List.of(new FlowAction.EmitEvent(new PlayMusicEvent(ctx.player(), musicAsset)));
        });

        registry.register("play_sound", (call, ctx) -> {
            SoundAsset soundAsset = parseSoundAsset(call.arguments().get(0));
            return List.of(new FlowAction.EmitEvent(new PlaySoundEvent(ctx.player(), soundAsset)));
        });
    }

    private Entity resolveEntity(Entity player, String entityId) {
        if (player == null) return null;

        EntityLookup lookup = entityLookup == null ? null : entityLookup.get();
        if (lookup != null) {
            Entity resolved = lookup.find(player, entityId);
            if (resolved != null) return resolved;
        }
        if (entityId == null || entityId.isBlank() || "player".equalsIgnoreCase(entityId)) return player;
        return null;
    }

    private Animation2D.AnimationType parseAnimationType(String type) {
        if (type == null || type.isBlank()) return null;

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        try {
            return Animation2D.AnimationType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Animation.PlayMode parsePlayMode(String playMode, Animation2D.AnimationType type) {
        if (playMode == null || playMode.isBlank()) {
            return switch (type) {
                case IDLE, WALK -> Animation.PlayMode.LOOP;
                default -> Animation.PlayMode.NORMAL;
            };
        }
        String normalized = playMode.trim().toUpperCase(Locale.ROOT);
        if ("LOOP".equals(normalized)) return Animation.PlayMode.LOOP;

        if ("NORMAL".equals(normalized)) return Animation.PlayMode.NORMAL;

        return switch (type) {
            case IDLE, WALK -> Animation.PlayMode.LOOP;
            default -> Animation.PlayMode.NORMAL;
        };
    }

    private Facing.FacingDirection parseFacing(String direction) {
        if (direction == null || direction.isBlank()) return null;

        String normalized = direction.trim().toUpperCase(Locale.ROOT);
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

    private MusicAsset parseMusicAsset(String musicAsset) {
        if (musicAsset == null || musicAsset.isBlank()) return null;

        String normalized = musicAsset.trim().toUpperCase(Locale.ROOT);
        try {
            return MusicAsset.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SoundAsset parseSoundAsset(String soundAsset) {
        if (soundAsset == null || soundAsset.isBlank()) return null;

        String normalized = soundAsset.trim().toUpperCase(Locale.ROOT);
        try {
            return SoundAsset.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
