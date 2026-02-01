package com.github.tilcob.game.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.dialog.DialogChoice;
import com.github.tilcob.game.event.DialogChoiceEvent;
import com.github.tilcob.game.event.DialogEvent;
import com.github.tilcob.game.event.RewardGrantedEvent;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemModel;
import com.github.tilcob.game.ui.model.DialogChoiceDisplay;
import com.github.tilcob.game.ui.model.DialogDisplay;
import com.github.tilcob.game.ui.model.RewardDisplay;

public class UiModelFactory {

    public ItemModel createItemModel(Entity itemEntity) {
        Item item = Item.MAPPER.get(itemEntity);
        Id idComp = Id.MAPPER.get(itemEntity);
        if (idComp == null)
            return null;

        ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
        return new ItemModel(
                idComp.getId(),
                definition.category(),
                definition.name(),
                definition.icon(),
                item.getSlotIndex(),
                item.isEquipped(),
                item.getCount());
    }

    public ItemModel createItemModel(String itemId, int slotIndex) {
        String resolvedId = ItemDefinitionRegistry.resolveId(itemId);
        ItemDefinition definition = ItemDefinitionRegistry.get(resolvedId);
        return new ItemModel(
                -1,
                definition.category(),
                definition.name(),
                definition.icon(),
                slotIndex,
                false,
                1);
    }

    public DialogDisplay createDialogDisplay(DialogEvent event) {
        String speaker = "NPC";
        if (event.entity() != null && Npc.MAPPER.get(event.entity()) != null) {
            speaker = Npc.MAPPER.get(event.entity()).getName();
        }
        return new DialogDisplay(speaker, event.line());
    }

    public DialogChoiceDisplay createDialogChoiceDisplay(DialogChoiceEvent event) {
        Array<String> labels = new Array<>();
        if (event.choices() != null) {
            for (DialogChoice choice : event.choices()) {
                labels.add(choice.text());
            }
        }
        return new DialogChoiceDisplay(labels, event.selectedIndex());
    }

    public RewardDisplay createRewardDisplay(RewardGrantedEvent event) {
        Array<String> items = new Array<>();
        for (String itemId : event.reward().items()) {
            items.add(itemId);
        }
        String title = event.questTitle() == null || event.questTitle().isBlank()
                ? event.questId().replace("_", " ")
                : event.questTitle();
        return new RewardDisplay(title, event.reward().money(), items);
    }
}
