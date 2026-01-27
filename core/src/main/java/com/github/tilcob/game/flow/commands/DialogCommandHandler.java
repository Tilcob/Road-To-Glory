package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Counters;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class DialogCommandHandler {

    public DialogCommandHandler(GameEventBus eventBus) {
        eventBus.subscribe(DialogCommandModule.DialogGiveMoneyEvent.class, this::giveMoney);
        eventBus.subscribe(DialogCommandModule.DialogGiveItemEvent.class, this::giveItem);
        eventBus.subscribe(DialogCommandModule.DialogSetFlagEvent.class, this::setFlag);
        eventBus.subscribe(DialogCommandModule.DialogIncCounterEvent.class, this::incCounter);
    }

    private void giveMoney(DialogCommandModule.DialogGiveMoneyEvent event) {
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

    private void giveItem(DialogCommandModule.DialogGiveItemEvent event) {
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

    private void setFlag(DialogCommandModule.DialogSetFlagEvent event) {
        Entity player = event.player();
        String flag = event.flag();
        boolean value = event.value();

        if (player == null ) return;
        if (flag == null || flag.isBlank()) return;
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        flags.set(flag, value);
    }

    private void incCounter(DialogCommandModule.DialogIncCounterEvent event) {
        Entity player = event.player();
        String counter = event.counter();
        int delta = event.delta();

        if (player == null) return;
        if (counter == null || counter.isBlank()) return;
        Counters counters = Counters.MAPPER.get(player);
        if (counters == null) {
            counters = new Counters();
            player.add(counters);
        }
        counters.increment(counter, delta);
    }
}
