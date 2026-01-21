# TriggerSystem

## Zweck
Führt Trigger-Logik für Map-Sensoren aus und delegiert an spezifische TriggerHandler.

## Ablauf
- Hält eine Handler-Map für Trigger.Type und verarbeitet Trigger-Queues pro Tick.
- Löst Exit-Handler bei ExitTriggerEvent aus.
- Indexiert Entities mit Tiled-IDs, um Trigger auf echte Entities zu mappen.

## Wichtige Komponenten & Ereignisse
- Trigger
- TriggerHandler
- ExitTriggerEvent
- Tiled
