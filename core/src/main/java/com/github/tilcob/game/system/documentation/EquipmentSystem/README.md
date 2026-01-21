# EquipmentSystem

## Zweck
Verwaltet Ausrüsten/Abnehmen von Items und synchronisiert Inventar- und Equipment-Slots.

## Ablauf
- Hält eine Player-Referenz über processEntity.
- Bei EquipItemEvent wird das Item validiert (Slot, Kategorie, Requirements) und ins Equipment verschoben.
- Beim Unequip wird das Item in einen freien Inventar-Slot gelegt.
- Feuert UpdateInventoryEvent und UpdateEquipmentEvent.

## Wichtige Komponenten & Ereignisse
- Equipment
- Inventory
- EquipItemEvent
- UpdateInventoryEvent
