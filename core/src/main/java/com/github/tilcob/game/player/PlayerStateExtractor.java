package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.SkillTreeStateSnapshot;

public class PlayerStateExtractor {
    public static PlayerState fromEntity(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        Life life = Life.MAPPER.get(entity);
        Inventory inventory = Inventory.MAPPER.get(entity);
        Equipment equipment = Equipment.MAPPER.get(entity);
        Skill skill = Skill.MAPPER.get(entity);
        StatModifierComponent statModifiers = StatModifierComponent.MAPPER.get(entity);
        PlayerState state = new PlayerState();

        if (transform == null || life == null || inventory == null) return state;

        state.setPosX(transform.getPosition().x);
        state.setPosY(transform.getPosition().y);
        state.setLife(life.getLife());
        for (Entity itemEntity : inventory.getItems()) {
            Item item = Item.MAPPER.get(itemEntity);
            if (item == null) continue;
            for (int i = 0; i < item.getCount(); i++) {
                state.getItemsByName().add(item.getItemId());
            }
            state.getItemSlots().add(new PlayerState.ItemSlotState(
                item.getItemId(),
                item.getSlotIndex(),
                item.getCount()
            ));
        }

        if (equipment != null) {
            ObjectMap<ItemCategory, Entity> equipped = equipment.getEquippedSlots();
            for (ObjectMap.Entry<ItemCategory, Entity> entry : equipped.entries()) {
                Item item = Item.MAPPER.get(entry.value);
                if (item == null) continue;
                state.getEquipmentSlots().put(entry.key, new PlayerState.EquipmentSlotState(
                    item.getItemId(),
                    item.getSlotIndex()
                ));
            }
        }

        if (skill != null && !skill.getTrees().isEmpty()) {
            for (var entry : skill.getTrees().entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                SkillTreeStateSnapshot snapshot = new SkillTreeStateSnapshot();
                snapshot.setCurrentLevel(entry.getValue().getCurrentLevel());
                snapshot.setSkillPoints(entry.getValue().getSkillPoints());
                snapshot.setUnlockedNodes(entry.getValue().getUnlockedNodes());
                state.getSkillTrees().put(entry.getKey(), snapshot);
            }
        }

        if (statModifiers != null && statModifiers.getModifiers() != null) {
            for (var modifier : statModifiers.getModifiers()) {
                if (modifier == null) continue;
                state.getStatModifiers().add(new PlayerState.StatModifierState(
                    modifier.getStatType(),
                    modifier.getAdditive(),
                    modifier.getMultiplier(),
                    modifier.getSource(),
                    modifier.getDurationSeconds(),
                    modifier.getExpireTimeEpochMs()
                ));
            }
        }

        return state;
    }
}
