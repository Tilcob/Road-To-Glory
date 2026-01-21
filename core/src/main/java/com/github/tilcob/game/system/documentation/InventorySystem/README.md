# InventorySystem

## Purpose
Manages inventory logic: adding items, stacking, moving, dropping, and splitting.

## Flow
- Processes `Inventory.itemsToAdd`: inserts items into empty slots or existing stacks and sends quest signals.
- Reacts to drag-and-drop/equip/unequip/drop/split events and updates slots or item entities.
- Spawns map items when dropping and fires `UpdateInventoryEvent`.

## Key components & events
- Inventory
- Item
- GameEventBus
- QuestManager
- Skin
