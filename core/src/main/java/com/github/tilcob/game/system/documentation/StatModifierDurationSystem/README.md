# StatModifierDurationSystem

## Purpose
Removes expired buff stat modifiers and triggers stat recalculations.

## Flow
- Iterates modifiers, initializes expiry times for `buff:` modifiers, and removes expired entries.
- Fires `StatRecalcEvent` when modifiers are removed.

## Key components & events
- StatModifierComponent
- StatRecalcEvent
- StatModifier
