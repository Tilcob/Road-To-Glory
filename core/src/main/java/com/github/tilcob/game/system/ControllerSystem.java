package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class ControllerSystem extends IteratingSystem {
    private final ScreenNavigator screenNavigator;
    private final GameEventBus eventBus;

    public ControllerSystem(ScreenNavigator screenNavigator, GameEventBus eventBus) {
        super(Family.all(Controller.class).get());
        this.screenNavigator = screenNavigator;
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        for (Command command : controller.getPressedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, 1f);
                case DOWN -> moveEntity(entity, 0f, -1f);
                case LEFT -> moveEntity(entity, -1f, 0f);
                case RIGHT -> moveEntity(entity, 1f, 0f);
                case SELECT -> startEntityAttack(entity);
                case CANCEL -> {
                    screenNavigator.setScreen(MenuScreen.class);
                    eventBus.fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));
                }
                case INTERACT -> interact(entity);
                case INVENTORY -> showEntityInventory(entity);
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, -1f);
                case DOWN -> moveEntity(entity, 0f, 1f);
                case LEFT -> moveEntity(entity, 1f, 0f);
                case RIGHT -> moveEntity(entity, -1f, 0f);
            }
        }
        controller.getReleasedCommands().clear();
    }

    private void showEntityInventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;

        eventBus.fire(new UiEvent(Command.INVENTORY));
    }

    private void interact(Entity player) {
        if (OpenChestRequest.MAPPER.get(player) != null) {
            OpenChestRequest openChestRequest = OpenChestRequest.MAPPER.get(player);
            Entity chestEntity = openChestRequest.getChest();
            Chest.MAPPER.get(chestEntity).open();
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

    private void moveEntity(Entity entity, float dx, float dy) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().x += dx;
            move.getDirection().y += dy;
        }
    }
}
