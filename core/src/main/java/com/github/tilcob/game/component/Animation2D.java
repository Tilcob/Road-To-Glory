package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.tilcob.game.assets.AtlasAsset;

public class Animation2D implements Component {
    public static final ComponentMapper<Animation2D> MAPPER = ComponentMapper.getFor(Animation2D.class);

    private final AtlasAsset atlasAsset;
    private final String atlasKey;
    private AnimationType type;
    private Facing.FacingDirection facingDirection;
    private Animation.PlayMode playMode;
    private float speed;
    private float stateTime;
    private Animation<TextureRegion> animation;
    private boolean dirty;

    public Animation2D(AtlasAsset atlasAsset,
                       String atlasKey,
                       AnimationType type,
                       Animation.PlayMode playMode,
                       float speed) {
        this.atlasAsset = atlasAsset;
        this.atlasKey = atlasKey;
        this.type = type;
        this.facingDirection = null;
        this.playMode = playMode;
        this.speed = speed;
        this.stateTime = 0f;
        this.animation = null;
    }

    public enum AnimationType {
        IDLE, WALK, ATTACK, DAMAGED;

        private final String atlasKey;
        AnimationType() {
            this.atlasKey = name().toLowerCase();
        }

        public String getAtlasKey() {
            return atlasKey;
        }
    }

    public AtlasAsset getAtlasAsset() {
        return atlasAsset;
    }

    public String getAtlasKey() {
        return atlasKey;
    }

    public AnimationType getType() {
        return type;
    }

    public Facing.FacingDirection getDirection() {
        return facingDirection;
    }

    public Animation.PlayMode getPlayMode() {
        return playMode;
    }

    public float getSpeed() {
        return speed;
    }

    public float getStateTime() {
        return stateTime;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setType(AnimationType type) {
        this.type = type;
        this.dirty = true;
    }

    public void setAnimation(Animation<TextureRegion> animation, Facing.FacingDirection facingDirection) {
        this.animation = animation;
        this.facingDirection = facingDirection;
        this.stateTime = 0;
        this.dirty = false;
    }

    public void setPlayMode(Animation.PlayMode playMode) {
        this.playMode = playMode;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        if (animation == null) {
            return false;
        }
        return animation.isAnimationFinished(stateTime);
    }

    public float incAndGetStateTime(float deltaTime) {
        this.stateTime += speed * deltaTime;
        return this.stateTime;
    }
}
