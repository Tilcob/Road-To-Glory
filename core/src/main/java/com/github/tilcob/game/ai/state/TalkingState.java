package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.Messages;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.AnimationFsm;
import com.github.tilcob.game.component.NpcFsm;

public class TalkingState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        //Controller.MAPPER.get(entity).getPressedCommands().clear();
        //NpcStateSupport.lookAtPlayer(NpcStateSupport.findPlayer(entity), entity);
    }

    @Override
    public void update(Entity entity) {
    }

    @Override
    public void exit(Entity entity) {
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        if (telegram.message == Messages.DIALOG_FINISHED) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return true;
        }
        return false;
    }
}
