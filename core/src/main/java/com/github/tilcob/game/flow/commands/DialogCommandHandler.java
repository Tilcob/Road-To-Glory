package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.DialogGiveItemEvent;
import com.github.tilcob.game.event.DialogGiveMoneyEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class DialogCommandHandler {

    public DialogCommandHandler(GameEventBus eventBus) {
        eventBus.subscribe(DialogGiveMoneyEvent.class, this::giveMoney);
        eventBus.subscribe(DialogGiveItemEvent.class, this::giveItem);
    }

    private void giveMoney(DialogGiveMoneyEvent event) {
        Entity player = event.player();
        int amount = event.amount();

        if (player == null) return;
        if (amount <= 0) return;
        Wallet wallet = Wallet.MAPPER.get(player);
        if (wallet == null) {
            wallet = new Wallet();
            player.add(wallet);
        }
        wallet.earn(amount);
    }

    private void giveItem(DialogGiveItemEvent event) {
        Entity player = event.player();
        String itemId = event.itemId();
        int count = event.count();

        if (itemId == null || itemId.isBlank() || count <= 0) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) {
            inventory = new Inventory();
            player.add(inventory);
        }
        String resolved = ItemDefinitionRegistry.resolveId(itemId);
        for (int i = 0; i < count; i++) inventory.getItemsToAdd().add(resolved);
    }
}
