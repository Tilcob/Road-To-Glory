# EquipmentSystem

## Purpose
Manages equipping/unequipping items and keeps inventory and equipment slots in sync.

## Flow
- Keeps a player reference via `processEntity`.
- On `EquipItemEvent`, validates the item (slot, category, requirements) and moves it into equipment.
- On unequip, places the item into a free inventory slot.
- Fires `UpdateInventoryEvent` and `UpdateEquipmentEvent`.

## Key components & events
- Equipment
- Inventory
- EquipItemEvent
- UpdateInventoryEvent
