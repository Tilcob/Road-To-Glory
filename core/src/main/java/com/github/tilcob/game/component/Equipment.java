package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.item.ItemCategory;

public class Equipment implements Component {
    public static final ComponentMapper<Equipment> MAPPER = ComponentMapper.getFor(Equipment.class);

    private final ObjectMap<ItemCategory, Entity> equippedSlots = new ObjectMap<>();
    private boolean dirty = true;

    public ObjectMap<ItemCategory, Entity> getEquippedSlots() {
        return equippedSlots;
    }

    public Entity getEquipped(ItemCategory slot) {
        return equippedSlots.get(slot);
    }

    public Entity equip(ItemCategory slot, Entity itemEntity) {
        Entity previous = equippedSlots.get(slot);
        if (previous != null) {
            setEquippedState(previous, false);
        }
        if (itemEntity == null) {
            dirty = true;
            equippedSlots.remove(slot);
            return previous;
        }
        equippedSlots.put(slot, itemEntity);
        setEquippedState(itemEntity, true);
        dirty = true;
        return previous;
    }

    public Entity unequip(ItemCategory slot) {
        Entity previous = equippedSlots.remove(slot);
        if (previous != null) {
            setEquippedState(previous, false);
        }
        dirty = true;
        return previous;
    }

    private void setEquippedState(Entity entity, boolean equipped) {
        Item item = Item.MAPPER.get(entity);
        if (item != null) {
            item.setEquipped(equipped);
        }
    }

    public boolean consumeDirty() {
        if (!dirty) return false;
        dirty = false;
        return true;
    }
}
