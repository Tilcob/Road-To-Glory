# StatRecalcSystem

## Zweck
Berechnet finale Stats aus Basiswerten und Modifikatoren und wendet sie auf Entities an.

## Ablauf
- Reagiert auf StatRecalcEvent und summiert additive/multiplikative Modifier pro StatType.
- Setzt finalStats und ruft StatApplier.apply auf.
- Schreibt Debug-Logs für Quellen (item/buff/levelup).

## Wichtige Komponenten & Ereignisse
- StatComponent
- StatModifierComponent
- StatApplier
- StatRecalcEvent
