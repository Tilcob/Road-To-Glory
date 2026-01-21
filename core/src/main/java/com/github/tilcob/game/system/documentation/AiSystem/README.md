# AiSystem

## Zweck
Steuert die NPC-KI und sorgt dafür, dass jedes NPC-Entity eine Referenz auf den Spieler erhält, bevor die Zustandsmaschine aktualisiert wird.

## Ablauf
- Filtert Entities mit Npc und ohne Player.
- Legt bei Bedarf die PlayerReference auf das erste gefundene Player-Entity.
- Ruft pro Tick das Update der NpcFsm-Zustandsmaschine auf.

## Wichtige Komponenten & Ereignisse
- Npc
- Player
- PlayerReference
- NpcFsm
