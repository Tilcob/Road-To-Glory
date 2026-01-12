package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.Controller;
import com.github.tilcob.game.component.Fsm;
import com.github.tilcob.game.input.Command;

public class WanderState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        Controller controller = Controller.MAPPER.get(entity);
        int direction = MathUtils.random(1, 4);
        switch (direction) {
            case 1 -> controller.getPressedCommands().add(Command.UP);
            case 2 -> controller.getPressedCommands().add(Command.DOWN);
            case 3 -> controller.getPressedCommands().add(Command.LEFT);
            case 4 -> controller.getPressedCommands().add(Command.RIGHT);
        }
    }

    @Override
    public void update(Entity entity) {
        if (Math.random() < 0.01) {
            Fsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
        }
    }

    @Override
    public void exit(Entity entity) {
        Controller.MAPPER.get(entity).getPressedCommands().clear();
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
