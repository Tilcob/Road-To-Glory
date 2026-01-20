package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.Equipment;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemStatModifier;
import com.github.tilcob.game.stat.StatKey;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

import java.util.HashMap;
import java.util.Map;

public class EquipmentStatModifierSystem extends IteratingSystem {
    private static final String ITEM_SOURCE_PREFIX = "item:";
    private static final Map<String, StatType> STAT_TYPE_LOOKUP = buildStatTypeLookup();

    public EquipmentStatModifierSystem() {
        super(Family.all(Equipment.class, StatModifierComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Equipment equipment = Equipment.MAPPER.get(entity);
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (equipment == null || modifiers == null) return;

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
}
