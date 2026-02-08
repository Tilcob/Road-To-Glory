package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.Command;

import java.util.Iterator;
import java.util.Map;

public class ControllerSystem extends IteratingSystem {
    private final GameEventBus eventBus;

    public ControllerSystem(GameEventBus eventBus) {
        super(Family.all(Controller.class).get());
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        DialogSession dialogSession = DialogSession.MAPPER.get(entity);
        PlayerInputLock inputLock = PlayerInputLock.MAPPER.get(entity);
        ActionLock actionLock = ActionLock.MAPPER.get(entity);
        if (actionLock != null && actionLock.isLockInput()) {
            clearMovement(entity);
            controller.getHeldCommands().clear();
            controller.getPressedCommands().clear();
            controller.getReleasedCommands().clear();
            controller.getCommandBuffer().clear();
            return;
        }

        if (inputLock != null) {
            clearMovement(entity);
            controller.getHeldCommands().removeIf(c -> !inputLock.isAllowed(c));
            controller.getPressedCommands().removeIf(c -> !inputLock.isAllowed(c));
            controller.getReleasedCommands().removeIf(c -> !inputLock.isAllowed(c));
            controller.getCommandBuffer().entrySet().removeIf(e -> !inputLock.isAllowed(e.getKey()));
        }
        boolean choosingDialog = dialogSession != null && dialogSession.isAwaitingChoice();

        updateMovement(entity, controller);
        float nowSeconds = TimeUtils.millis() / 1000f;

        for (Iterator<Map.Entry<Command, Float>> iterator = controller.getCommandBuffer().entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<Command, Float> entry = iterator.next();
            if (nowSeconds - entry.getValue() > Constants.GAMEPLAY_COMMAND_BUFFER_SECONDS) {
                iterator.remove();
                continue;
            }
            switch (entry.getKey()) {
                case UP -> {
                    if (choosingDialog) eventBus.fire(new DialogChoiceNavigateEvent(entity, -1));
                }
                case DOWN -> {
                    if (choosingDialog) eventBus.fire(new DialogChoiceNavigateEvent(entity, 1));
                }
                case SELECT -> select(entity);
                case PAUSE -> eventBus.fire(new PauseEvent(PauseEvent.Action.TOGGLE));
                case INTERACT -> interact(entity);
                case INVENTORY -> inventory(entity);
                case SKILLS -> openSkillTree(entity);
                default -> {
                }
            }
            iterator.remove();
        }
        controller.getPressedCommands().clear();
        controller.getReleasedCommands().clear();
    }

    private void updateMovement(Entity entity, Controller controller) {
        Move move = Move.MAPPER.get(entity);
        if (move == null) return;
        DialogSession session = DialogSession.MAPPER.get(entity);
        if (session != null && session.isAwaitingChoice()) {
            move.getDirection().set(0f, 0f);
            return;
        }

        float x = 0f;
        float y = 0f;
        if (controller.getHeldCommands().contains(Command.UP)) y += 1f;
        if (controller.getHeldCommands().contains(Command.DOWN)) y -= 1f;
        if (controller.getHeldCommands().contains(Command.LEFT)) x -= 1f;
        if (controller.getHeldCommands().contains(Command.RIGHT)) x += 1f;
        move.getDirection().set(x, y);
    }

    private void clearMovement(Entity entity) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().set(0f, 0f);
        }
    }

    private void inventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;
        eventBus.fire(new UiEvent(Command.INVENTORY, UiEvent.Action.PRESS));
    }

    private void interact(Entity player) {
        eventBus.fire(AbilityRequestEvent.fromCommand(player, Command.INTERACT));
    }

    private void select(Entity player) {
        eventBus.fire(AbilityRequestEvent.fromCommand(player, Command.SELECT));
    }

    private void openSkillTree(Entity player) {
        if (Skill.MAPPER.get(player) == null) return;
        eventBus.fire(new UiEvent(Command.SKILLS, UiEvent.Action.PRESS));
    }
}
