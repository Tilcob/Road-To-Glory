package com.github.tilcob.game.save.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

import java.util.*;

public class PlayerState {
    private float posX;
    private float posY;
    private float life;
    @JsonIgnore
    private List<String> items = new ArrayList<>();
    private List<String> itemsByName = new ArrayList<>();
    private List<ItemSlotState> itemSlots = new ArrayList<>();
    private Map<ItemCategory, EquipmentSlotState> equipmentSlots = new EnumMap<>(ItemCategory.class);
    private Map<String, SkillTreeStateSnapshot> skillTrees = new HashMap<>();

    public PlayerState() { }

    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }

    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getLife() { return life; }
    public void setLife(float life) { this.life = life; }

    public List<String> getItemsByName() {
        return itemsByName;
    }

    public void setItemsByName(List<String> itemsByName) {
        this.itemsByName = itemsByName;
    }

    public List<ItemSlotState> getItemSlots() {
        return itemSlots;
    }

    public void setItemSlots(List<ItemSlotState> itemSlots) {
        this.itemSlots = itemSlots;
    }

    public Map<ItemCategory, EquipmentSlotState> getEquipmentSlots() {
        return equipmentSlots;
    }

    public void setEquipmentSlots(Map<ItemCategory, EquipmentSlotState> equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
    }

    public Map<String, SkillTreeStateSnapshot> getSkillTrees() {
        return skillTrees;
    }

    public void setSkillTrees(Map<String, SkillTreeStateSnapshot> skillTrees) {
        this.skillTrees = skillTrees;
    }

    @JsonIgnore
    public List<String> getItems() {
        return items;
    }
    @JsonIgnore
    public void setItems(List<String> items) {
        this.items = items;
    }

    @JsonIgnore
    public void rebuildItemsByName() {
        items.clear();
        List<String> normalized = new ArrayList<>();
        for (String name : itemsByName) {
            String resolved = normalizeItemId(name);
            if (resolved == null) {
                Gdx.app.error("PlayerState", "Unknown item id: " + name);
                continue;
            }
            items.add(resolved);
            normalized.add(resolved);
        }
        itemsByName = normalized;

        if (itemSlots != null) {
            List<ItemSlotState> normalizedSlots = new ArrayList<>();
            for (ItemSlotState slotState : itemSlots) {
                if (slotState == null) continue;
                String resolved = normalizeItemId(slotState.getItemId());
                if (resolved == null) {
                    Gdx.app.error("PlayerState", "Unknown item id in slot: " + slotState.getItemId());
                    continue;
                }
                slotState.setItemId(resolved);
                normalizedSlots.add(slotState);
            }
            itemSlots = normalizedSlots;
        }

        if (equipmentSlots != null && !equipmentSlots.isEmpty()) {
            Map<ItemCategory, EquipmentSlotState> normalizedSlots = new EnumMap<>(ItemCategory.class);
            for (var entry : equipmentSlots.entrySet()) {
                EquipmentSlotState slotState = entry.getValue();
                if (slotState == null) continue;
                String resolved = normalizeItemId(slotState.getItemId());
                if (resolved == null) {
                    Gdx.app.error("PlayerState", "Unknown equipment item id: " + slotState.getItemId());
                    continue;
                }
                slotState.setItemId(resolved);
                normalizedSlots.put(entry.getKey(), slotState);
            }
            equipmentSlots = normalizedSlots;
        }
    }

    private String normalizeItemId(String name) {
        if (name == null) {
            return null;
        }
        String resolved = ItemDefinitionRegistry.resolveId(name);
        if (!ItemDefinitionRegistry.isKnownId(resolved)) {
            return null;
        }
        return resolved;
    }

    @JsonIgnore
    public Vector2 getPositionAsVector() {
        return new Vector2(posX, posY);
    }

    public static class ItemSlotState {
        private String itemId;
        private int slotIndex;
        private int count;

        public ItemSlotState() { }

        public ItemSlotState(String itemId, int slotIndex, int count) {
            this.itemId = itemId;
            this.slotIndex = slotIndex;
            this.count = count;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        public void setSlotIndex(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class EquipmentSlotState {
        private String itemId;
        private int slotIndex;

        public EquipmentSlotState() { }

        public EquipmentSlotState(String itemId, int slotIndex) {
            this.itemId = itemId;
            this.slotIndex = slotIndex;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        public void setSlotIndex(int slotIndex) {
            this.slotIndex = slotIndex;
        }
    }
}
