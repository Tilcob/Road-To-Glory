# InventorySystem

## Zweck
Verwaltet Inventarlogik: Item-Additionen, Stapeln, Verschieben, Droppen und Splitting.

## Ablauf
- Verarbeitet Inventory.itemsToAdd: fügt Items in freie Slots oder bestehende Stacks ein und sendet Quest-Signale.
- Reagiert auf Drag-and-Drop/Equip/Unequip/Drop/Split-Events und aktualisiert Slots oder Item-Entities.
- Erzeugt Map-Items beim Droppen und feuert UpdateInventoryEvent.

## Wichtige Komponenten & Ereignisse
- Inventory
- Item
- GameEventBus
- QuestManager
- Skin
