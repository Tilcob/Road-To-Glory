package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Counters;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class DialogYarnBridge {
    public void registerAll(YarnRuntime runtime) {
        registerCommands(runtime);
    }

    private void registerCommands(YarnCommandRegistry registry) {
        registry.register("give_money", this::giveMoney);
        registry.register("give_item", this::giveItem);
        registry.register("set_flag", this::setFlag);
        registry.register("inc_counter", this::incrementCounter);
    }

    private void giveMoney(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        int amount = parseInt(args[0], 0);
        if (amount <= 0) return;
        Wallet wallet = Wallet.MAPPER.get(player);
        if (wallet == null) {
            wallet = new Wallet();
            player.add(wallet);
        }
        wallet.earn(amount);
    }

    private void giveItem(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        String itemId = args[0];
        int count = args.length > 1 ? parseInt(args[1], 1) : 1;
        if (itemId == null || itemId.isBlank() || count <= 0) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) {
            inventory = new Inventory();
            player.add(inventory);
        }
        String resolved = ItemDefinitionRegistry.resolveId(itemId);
        for (int i = 0; i < count; i++) inventory.getItemsToAdd().add(resolved);
    }

    private void setFlag(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) {
            return;
        }
        String flag = args[0];
        boolean value = args.length <= 1 || Boolean.parseBoolean(args[1]);
        if (flag == null || flag.isBlank()) {
            return;
        }
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        flags.set(flag, value);
    }

    private void incrementCounter(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) {
            return;
        }
        String counter = args[0];
        int amount = args.length > 1 ? parseInt(args[1], 1) : 1;
        if (counter == null || counter.isBlank()) {
            return;
        }
        Counters counters = Counters.MAPPER.get(player);
        if (counters == null) {
            counters = new Counters();
            player.add(counters);
        }
        counters.increment(counter, amount);
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
