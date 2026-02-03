# ChestSystem

## Purpose
Opens chests, transfers items to the player's inventory, and leaves remaining items in the chest.

## Flow
- Checks `OpenChestRequest` for a valid chest entity.
- If the chest is open, tries to move contents into existing stacks or free slots.
- Adds transferred items to `Inventory.itemsToAdd`; remaining items stay in the chest.
- Removes the `OpenChestRequest` and closes the chest.

## Key components & events
- OpenChestRequest
- Chest
- Inventory
- ItemDefinitionRegistry
