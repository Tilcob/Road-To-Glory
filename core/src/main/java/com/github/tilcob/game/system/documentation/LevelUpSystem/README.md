# LevelUpSystem

## Zweck
Vergibt Level-Up-Stat-Modifier und stößt eine Neuberechnung der Stats an.

## Ablauf
- Empfängt LevelUpEvent und stellt sicher, dass StatComponent/StatModifierComponent existieren.
- Entfernt alte levelup:-Modifier und fügt neue Modifier basierend auf totalLevels hinzu.
- Feuert StatRecalcEvent und schreibt Debug-Logs.

## Wichtige Komponenten & Ereignisse
- LevelUpEvent
- StatModifierComponent
- StatRecalcEvent
