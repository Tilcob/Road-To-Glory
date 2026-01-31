package com.github.tilcob.game.system;

import com.badlogic.ashley.core.*;
import com.github.tilcob.game.component.EntityId;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

public class YarnScopeCleanupSystem extends EntitySystem implements EntityListener {
    private final DialogYarnRuntime dialogYarnRuntime;
    private final QuestYarnRuntime questYarnRuntime;

    public YarnScopeCleanupSystem(DialogYarnRuntime dialogYarnRuntime, QuestYarnRuntime questYarnRuntime) {
        this.dialogYarnRuntime = dialogYarnRuntime;
        this.questYarnRuntime = questYarnRuntime;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(EntityId.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {

    }

    @Override
    public void entityRemoved(Entity entity) {
        dialogYarnRuntime.clearVariables(entity);
        questYarnRuntime.clearVariables(entity);
    }
}
