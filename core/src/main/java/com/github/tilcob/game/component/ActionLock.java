package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ActionLock implements Component {
    public static final ComponentMapper<ActionLock> MAPPER = ComponentMapper.getFor(ActionLock.class);

    private boolean lockMovement;
    private boolean lockFacing;
    private boolean lockInput;

    public boolean isLockMovement() {
        return lockMovement;
    }

    public boolean isLockFacing() {
        return lockFacing;
    }

    public boolean isLockInput() {
        return lockInput;
    }

    public void setLockMovement(boolean lockMovement) {
        this.lockMovement = lockMovement;
    }

    public void setLockFacing(boolean lockFacing) {
        this.lockFacing = lockFacing;
    }

    public void setLockInput(boolean lockInput) {
        this.lockInput = lockInput;
    }
}
