# StatRecalcSystem

## Purpose
Calculates final stats from base values and modifiers and applies them to entities.

## Flow
- Responds to `StatRecalcEvent` and sums additive/multiplicative modifiers per `StatType`.
- Sets `finalStats` and calls `StatApplier.apply`.
- Writes debug logs for sources (item/buff/levelup).

## Key components & events
- StatComponent
- StatModifierComponent
- StatApplier
- StatRecalcEvent
