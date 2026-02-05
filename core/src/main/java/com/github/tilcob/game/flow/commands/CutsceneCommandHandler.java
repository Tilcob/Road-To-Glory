package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.Command;

public class CutsceneCommandHandler {
    private final AudioManager audioManager;
    private final Vector2 tmpVector = new Vector2();

    public CutsceneCommandHandler(GameEventBus eventBus, AudioManager audioManager) {
        this.audioManager = audioManager;

        eventBus.subscribe(LockPlayerEvent.class, this::lockPlayer);
        eventBus.subscribe(UnlockPlayerEvent.class, this::unlockPlayer);
        eventBus.subscribe(CameraMoveEvent.class, this::cameraMove);
        eventBus.subscribe(CameraMoveRelativeEvent.class, this::cameraMoveRelative);
        eventBus.subscribe(CameraMoveBackEvent.class, this::cameraMoveBack);
        eventBus.subscribe(PlayAnimationEvent.class, this::playAnimation);
        eventBus.subscribe(PlayIndicatorEvent.class, this::playIndicator);
        eventBus.subscribe(MoveToEvent.class, this::moveTo);
        eventBus.subscribe(FaceEntityEvent.class, this::faceEntity);
        eventBus.subscribe(FadeInEvent.class, this::fadeIn);
        eventBus.subscribe(FadeOutEvent.class, this::fadeOut);
        eventBus.subscribe(PlayMusicEvent.class, this::playMusic);
        eventBus.subscribe(PlaySoundEvent.class, this::playSound);
    }

    private void lockPlayer(LockPlayerEvent event) {
        if (event.player() == null) return;
        if (PlayerInputLock.MAPPER.get(event.player()) == null) {
            event.player().add(PlayerInputLock.allow(Command.INTERACT, Command.PAUSE));
        }
    }

    private void unlockPlayer(UnlockPlayerEvent event) {
        if (event.player() == null) return;
        event.player().remove(PlayerInputLock.class);
    }

    private void cameraMove(CameraMoveEvent event) {
        if (event.player() == null) return;
        event.player().remove(CameraPan.class);
        event.player().remove(CameraPanHome.class);
        event.player().add(new CameraPan(event.x(), event.y(), event.duration()));
    }

    private void cameraMoveRelative(CameraMoveRelativeEvent event) {
        if (event.player() == null) return;
        Transform transform = Transform.MAPPER.get(event.player());
        float targetX = transform.getPosition().x + event.offsetX();
        float targetY = transform.getPosition().y + event.offsetY() + Constants.CAMERA_OFFSET_Y;
        event.player().remove(CameraPan.class);
        event.player().remove(CameraPanHome.class);
        event.player().add(new CameraPan(targetX, targetY, event.duration()));
    }

    private void cameraMoveBack(CameraMoveBackEvent event) {
        if (event.player() == null) return;
        CameraPanHome cameraPanHome = CameraPanHome.MAPPER.get(event.player());
        if (cameraPanHome == null) return;
        event.player().remove(CameraPan.class);
        event.player().add(new CameraPan(cameraPanHome.getPosition().x, cameraPanHome.getPosition().y, event.duration()));
        event.player().remove(CameraPanHome.class);
    }

    private void playAnimation(PlayAnimationEvent event) {
        if (event.player() == null || event.target() == null) return;
        Animation2D animation2D = Animation2D.MAPPER.get(event.target());
        if (animation2D == null || event.type() == null) return;
        animation2D.setType(event.type());
        animation2D.setPlayMode(event.playMode());
    }

    private void playIndicator(PlayIndicatorEvent event) {
        if (event.player() == null || event.target() == null || event.indicatorType() == null) return;

        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(event.target());
        boolean hadIndicator = indicator != null;
        OverheadIndicator.OverheadIndicatorType fallbackType =
            hadIndicator ? indicator.getIndicatorId() : event.indicatorType();
        boolean fallbackVisible = hadIndicator && indicator.isVisible();

        if (indicator == null) {
            Transform transform = Transform.MAPPER.get(event.target());
            float offsetY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
            if (transform != null) {
                offsetY = transform.getSize().y + Constants.DEFAULT_INDICATOR_OFFSET_Y - 8f * Constants.UNIT_SCALE;
            }
            indicator = new OverheadIndicator(
                event.indicatorType(),
                new Vector2(0f, offsetY),
                .6f,
                Color.WHITE.cpy(),
                true
            );
            event.target().add(indicator);
        }

        indicator.setIndicatorId(event.indicatorType());
        indicator.setVisible(true);

        if (event.durationSeconds() != null && event.durationSeconds() > 0f) {
            event.target().remove(IndicatorCommandLifetime.class);
            event.target().add(new IndicatorCommandLifetime(event.durationSeconds(), fallbackType, fallbackVisible));
        }

        OverheadIndicatorAnimation animation = OverheadIndicatorAnimation.MAPPER.get(event.target());
        if (animation == null) {
            event.target().add(new OverheadIndicatorAnimation(0f, 0f, 0f, 0f, 1f));
        } else {
            animation.setTime(0f);
            animation.setBobPhase(0f);
            animation.setPulsePhase(0f);
            animation.setCurrentOffsetY(0f);
            animation.setCurrentScale(1f);
        }
    }

    private void moveTo(MoveToEvent event) {
        if (event.player() == null || event.target() == null) return;
        Transform transform = Transform.MAPPER.get(event.target());
        if (transform == null) return;
        float targetX = transform.getPosition().x + event.x();
        float targetY = transform.getPosition().y + event.y();
        MoveIntent moveIntent = MoveIntent.MAPPER.get(event.target());
        if (moveIntent == null) {
            moveIntent = new MoveIntent();
            event.target().add(moveIntent);
        }
        moveIntent.setTarget(tmpVector.set(targetX, targetY), event.arrivalDistance());
    }

    private void faceEntity(FaceEntityEvent event) {
        if (event.player() == null || event.target() == null) return;
        Facing facing = Facing.MAPPER.get(event.target());
        if (facing == null) {
            facing = new Facing(event.direction());
            event.target().add(facing);
        } else {
            facing.setDirection(event.direction());
        }
    }

    private void fadeIn(FadeInEvent event) {
        applyFade(event.player(), event.alpha(), event.duration());
    }

    private void fadeOut(FadeOutEvent event) {
        applyFade(event.player(), event.alpha(), event.duration());
    }

    private void playMusic(PlayMusicEvent event) {
        if (event.player() == null || event.musicAsset() == null) return;
        audioManager.playMusic(event.musicAsset());
    }

    private void playSound(PlaySoundEvent event) {
        if (event.player() == null || event.soundAsset() == null) return;
        audioManager.playSound(event.soundAsset());
    }


    private void applyFade(Entity player, float targetAlpha, float duration) {
        if (player == null) return;
        ScreenFade fade = ScreenFade.MAPPER.get(player);
        if (fade == null) {
            float initialAlpha = targetAlpha <= 0f ? 1f : 0f;
            fade = new ScreenFade(initialAlpha);
            player.add(fade);
        }
        fade.start(targetAlpha, duration);
    }
}
