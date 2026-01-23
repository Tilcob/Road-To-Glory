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
        PlayerInputLock lock = PlayerInputLock.MAPPER.get(entity);
        if (lock != null) {
            clearMovement(entity);
            controller.getHeldCommands().removeIf(c -> !lock.isAllowed(c));
            controller.getPressedCommands().removeIf(c -> !lock.isAllowed(c));
            controller.getReleasedCommands().removeIf(c -> !lock.isAllowed(c));
            controller.getCommandBuffer().entrySet().removeIf(e -> !lock.isAllowed(e.getKey()));
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
                case SELECT -> startEntityAttack(entity);
                case PAUSE -> eventBus.fire(new PauseEvent(PauseEvent.Action.TOGGLE));
                case INTERACT -> interact(entity);
                case INVENTORY -> showEntityInventory(entity);
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

    private void showEntityInventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;

        eventBus.fire(new UiEvent(Command.INVENTORY, UiEvent.Action.PRESS));
    }

    private void interact(Entity player) {
        if (RewardDialogState.MAPPER.get(player) != null) {
            eventBus.fire(new DialogAdvanceEvent(player));
            return;
        }
        DialogSession dialogSession = DialogSession.MAPPER.get(player);
        if (dialogSession != null) {
            if (dialogSession.isAwaitingChoice()) {
                eventBus.fire(new DialogChoiceSelectEvent(player));
            } else {
                eventBus.fire(new DialogAdvanceEvent(player));
            }
            return;
        }
        eventBus.fire(new DialogAdvanceEvent(player));

        if (OpenChestRequest.MAPPER.get(player) != null) {
            OpenChestRequest openChestRequest = OpenChestRequest.MAPPER.get(player);
            Entity chestEntity = openChestRequest.getChest();
            Chest.MAPPER.get(chestEntity).open();
            return;
        } else {
            eventBus.fire(new CloseChestEvent(player, null));
        }
        if (StartDialogRequest.MAPPER.get(player) != null) {
            StartDialogRequest startDialogRequest = StartDialogRequest.MAPPER.get(player);
            Entity npc = startDialogRequest.getNpc();
            PlayerReference.MAPPER.get(npc).setPlayer(player);
            Dialog.MAPPER.get(npc).setState(Dialog.State.REQUEST);
        }
    }

    private void startEntityAttack(Entity entity) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.canAttack()) {
            attack.startAttack();
        }
    }
}
