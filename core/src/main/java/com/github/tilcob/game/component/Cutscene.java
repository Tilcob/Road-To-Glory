package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Cutscene implements Component {
    public static final ComponentMapper<Cutscene> MAPPER = ComponentMapper.getFor(Cutscene.class);

    private String cutsceneId;
    private State state;
    private int lineIndex;
    private float waitTimerSeconds;
    private boolean awaitingDialog;
    private boolean awaitingCamera;
    private boolean awaitingMove;

    public Cutscene(String cutsceneId) {
        this.cutsceneId = cutsceneId;
        this.state = State.REQUEST;
        this.lineIndex = 0;
        this.waitTimerSeconds = 0f;
        this.awaitingDialog = false;
        this.awaitingCamera = false;
        this.awaitingMove = false;
    }

    public String getCutsceneId() {
        return cutsceneId;
    }

    public void setCutsceneId(String cutsceneId) {
        this.cutsceneId = cutsceneId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public float getWaitTimerSeconds() {
        return waitTimerSeconds;
    }

    public void setWaitTimerSeconds(float waitTimerSeconds) {
        this.waitTimerSeconds = Math.max(0f, waitTimerSeconds);
    }

    public boolean isAwaitingDialog() {
        return awaitingDialog;
    }

    public void setAwaitingDialog(boolean awaitingDialog) {
        this.awaitingDialog = awaitingDialog;
    }

    public boolean isAwaitingCamera() {
        return awaitingCamera;
    }

    public void setAwaitingCamera(boolean awaitingCamera) {
        this.awaitingCamera = awaitingCamera;
    }

    public boolean isAwaitingMove() {
        return awaitingMove;
    }

    public void setAwaitingMove(boolean awaitingMove) {
        this.awaitingMove = awaitingMove;
    }

    public enum State {
        IDLE,
        REQUEST,
        ACTIVE,
        FINISHED,
    }
}
