package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

import java.util.Iterator;
import java.util.Map;

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
                case SELECT -> startEntityAttack(entity);
                case CANCEL -> {
                    screenNavigator.setScreen(MenuScreen.class);
                    eventBus.fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));
                }
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

        float x = 0f;
        float y = 0f;
        if (controller.getHeldCommands().contains(Command.UP)) y += 1f;
        if (controller.getHeldCommands().contains(Command.DOWN)) y -= 1f;
        if (controller.getHeldCommands().contains(Command.LEFT)) x -= 1f;
        if (controller.getHeldCommands().contains(Command.RIGHT)) x += 1f;
        move.getDirection().set(x, y);
    }

    private void showEntityInventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;

        eventBus.fire(new UiEvent(Command.INVENTORY, UiEvent.Action.PRESS));
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
}
