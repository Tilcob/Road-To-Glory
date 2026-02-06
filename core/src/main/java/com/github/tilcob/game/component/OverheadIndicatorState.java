package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Intended transitions:
 * HIDDEN -> FADE_IN -> IDLE, IDLE -> ATTENTION -> IDLE, IDLE/ATTENTION -> FADE_OUT -> HIDDEN.
 * desiredType/desiredVisible can be set to request a new target while the state machine runs.
 */
public class OverheadIndicatorState implements Component {
    public static final ComponentMapper<OverheadIndicatorState> MAPPER =
        ComponentMapper.getFor(OverheadIndicatorState.class);

    public enum State {
        HIDDEN,
        FADE_IN,
        IDLE,
        ATTENTION,
        FADE_OUT
    }

    private State state;
    private float timer;
    private OverheadIndicator.OverheadIndicatorType desiredType;
    private Boolean desiredVisible;

    public OverheadIndicatorState() {
        this(State.HIDDEN, 0f);
    }

    public OverheadIndicatorState(State state, float timer) {
        this.state = state;
        this.timer = timer;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getTimer() {
        return timer;
    }

    public void setTimer(float timer) {
        this.timer = timer;
    }

    public OverheadIndicator.OverheadIndicatorType getDesiredType() {
        return desiredType;
    }

    public void setDesiredType(OverheadIndicator.OverheadIndicatorType desiredType) {
        this.desiredType = desiredType;
    }

    public Boolean getDesiredVisible() {
        return desiredVisible;
    }

    public void setDesiredVisible(Boolean desiredVisible) {
        this.desiredVisible = desiredVisible;
    }
}
