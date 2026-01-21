# EquipmentStatModifierSystem

## Zweck
Überträgt Stat-Modifikatoren aus ausgerüsteten Items in die StatModifierComponent.

## Ablauf
- Reagiert auf Equipment-Änderungen (dirty) und entfernt alte item: Modifier.
- Liest ItemDefinition.statModifiers für jedes ausgerüstete Item und fügt StatModifier hinzu.
- Triggert StatRecalcEvent nach dem Aktualisieren.

## Wichtige Komponenten & Ereignisse
- Equipment
- StatModifierComponent
- ItemDefinitionRegistry
- StatRecalcEvent
