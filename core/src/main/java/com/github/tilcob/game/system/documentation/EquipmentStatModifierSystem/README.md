# EquipmentStatModifierSystem

## Purpose
Transfers stat modifiers from equipped items into `StatModifierComponent`.

## Flow
- Reacts to equipment changes (dirty) and removes old `item:` modifiers.
- Reads `ItemDefinition.statModifiers` for each equipped item and adds `StatModifier` entries.
- Triggers `StatRecalcEvent` after updates.

## Key components & events
- Equipment
- StatModifierComponent
- ItemDefinitionRegistry
- StatRecalcEvent
