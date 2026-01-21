# ChestSystem

## Zweck
Öffnet Kisten, überträgt Items in das Inventar des Spielers und lässt Rest-Items in der Kiste.

## Ablauf
- Prüft OpenChestRequest auf valide Chest-Entity.
- Wenn die Kiste offen ist, wird versucht, Inhalte in bestehende Stacks oder freie Slots zu verschieben.
- Übertragene Items werden zu Inventory.itemsToAdd hinzugefügt, der Rest verbleibt in der Chest.
- Entfernt den OpenChestRequest und schließt die Kiste.

## Wichtige Komponenten & Ereignisse
- OpenChestRequest
- Chest
- Inventory
- ItemDefinitionRegistry
