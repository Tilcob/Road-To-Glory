package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.Equipment;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.event.UpdateEquipmentEvent;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemStatModifier;
import com.github.tilcob.game.stat.StatKey;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

import java.util.HashMap;
import java.util.Map;

public class EquipmentStatModifierSystem extends IteratingSystem implements Disposable {
    private static final String ITEM_SOURCE_PREFIX = "item:";
    private static final Map<String, StatType> STAT_TYPE_LOOKUP = buildStatTypeLookup();

    private final GameEventBus eventBus;

    public EquipmentStatModifierSystem(GameEventBus eventBus) {
        super(Family.all(Equipment.class, StatModifierComponent.class).get());
        this.eventBus = eventBus;

        eventBus.subscribe(UpdateEquipmentEvent.class, this::onUpdateEquipment);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Equipment equipment = Equipment.MAPPER.get(entity);
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (equipment == null || modifiers == null) return;
        if (!equipment.consumeDirty()) return;
        recalcModifiers(entity, equipment, modifiers);
    }

    private void recalcModifiers(Entity entity, Equipment equipment, StatModifierComponent modifiers) {
        modifiers.removeModifiersBySourcePrefix(ITEM_SOURCE_PREFIX);

        for (ObjectMap.Entry<ItemCategory, Entity> entry : equipment.getEquippedSlots()) {
            Entity itemEntity = entry.value;
            if (itemEntity == null) {
                continue;
            }
            Item item = Item.MAPPER.get(itemEntity);
            if (item == null) {
                continue;
            }
            ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
            String source = ITEM_SOURCE_PREFIX + definition.id();
            for (ItemStatModifier itemModifier : definition.statModifiers()) {
                StatType statType = resolveStatType(itemModifier.stat());
                if (statType == null) {
                    continue;
                }
                modifiers.addModifier(new StatModifier(
                    statType,
                    itemModifier.additive(),
                    itemModifier.multiplier(),
                    source
                ));
            }
        }
        eventBus.fire(new StatRecalcEvent(entity));
    }

    private static Map<String, StatType> buildStatTypeLookup() {
        Map<String, StatType> lookup = new HashMap<>();
        for (StatType type : StatType.values()) {
            lookup.put(type.getId(), type);
        }
        return Map.copyOf(lookup);
    }

    private static StatType resolveStatType(StatKey key) {
        if (key == null) {
            return null;
        }
        return STAT_TYPE_LOOKUP.get(key.id());
    }

    private void onUpdateEquipment(UpdateEquipmentEvent event) {
        if (event == null || event.player() == null) return;
        Equipment equipment = Equipment.MAPPER.get(event.player());
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(event.player());
        if (equipment == null || modifiers == null) return;

        equipment.consumeDirty();
        recalcModifiers(event.player(), equipment, modifiers);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(UpdateEquipmentEvent.class, this::onUpdateEquipment);
    }
}
