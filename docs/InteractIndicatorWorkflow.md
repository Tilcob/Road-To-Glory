# InteractIndicator Workflow

## Zweck
Dokumentiert den End-to-End-Ablauf für Interaktions-Indikatoren (Overhead Icons) von der Zielauswahl bis zum Rendering.

## Ablauf pro Frame
1. **Komponente anheften** (`OverheadIndicatorAttachSystem`)
    - Für Entities mit `Transform` und (`Interactable` oder `Chest`) wird ein `OverheadIndicator` erzeugt, falls noch keiner existiert.
    - Zusätzlich wird eine `OverheadIndicatorAnimation` angelegt.

2. **Fokus bestimmen** (`InteractionFocusSystem`)
    - Das System bewertet alle `Interactable`-Kandidaten relativ zum Spieler.
    - Das beste Ziel wird in `ActiveEntityReference.setFocused(...)` gespeichert.

3. **Indikator-Typ auflösen** (`OverheadIndicatorStateSystem`)
    - Für NPCs mit `OverheadIndicator + NpcRole + Npc` wird genau ein finaler Typ gewählt.
    - Quellen: Rolle (`DANGER`, `MERCHANT`, ...), Dialogzustand (`TALK_BUSY`, `TALK_CHOICE`, `INTERACT_HINT`, ...), Questzustand (`QUEST_AVAILABLE`, `QUEST_TURNING`).
    - Bei Konflikten entscheidet eine Prioritätsskala (Quest > Gefahr/Shop > Talk-Hinweise > Fallback).

4. **Sichtbarkeit per Distanz steuern** (`OverheadIndicatorVisibilitySystem`)
    - Sichtbarkeit nutzt Hysterese (`INDICATOR_SHOW_DISTANCE` / `INDICATOR_HIDE_DISTANCE`), um Flackern an Grenzwerten zu vermeiden.

5. **Animation aktualisieren** (`OverheadIndicatorAnimationSystem`)
    - Bob/Pulse Werte werden pro Frame berechnet und in `OverheadIndicatorAnimation` geschrieben.

6. **Rendern** (`OverheadIndicatorRenderSystem`)
    - Das passende Icon wird über `OverheadIndicatorRegistry` aufgelöst.
    - Position, Offset und Animationswerte werden kombiniert und im World-Overlay gezeichnet.

## System-Reihenfolge (Gameplay Installer)
Die relevanten Systeme werden im Gameplay-Installer in dieser Reihenfolge registriert:

1. `OverheadIndicatorAttachSystem`
2. `InteractionFocusSystem`
3. `OverheadIndicatorStateSystem`
4. `IndicatorCommandLifetimeSystem`

Die sichtbaren/rendernden Indikator-Systeme laufen in ihren jeweiligen Render-/Visual-Phasen und nutzen den zuvor aufgelösten Zustand.

## Wichtige Datenabhängigkeiten
- `ActiveEntityReference`: liefert den fokussierten Interaktionspartner.
- `Dialog` / `DialogSession`: beeinflussen Talk-Indikatoren.
- `QuestLog` + `QuestYarnRegistry`: liefern Quest-Indikatoren.
- `OverheadIndicatorRegistry`: Mapping von Typ -> Visual/Atlas-Region.

## Kurzbeispiel
- Spieler schaut einen NPC an und ist in Reichweite.
- `InteractionFocusSystem` setzt den NPC als Fokus.
- `OverheadIndicatorStateSystem` löst auf `INTERACT_HINT` auf (sofern kein höher priorisierter Quest-/Gefahr-Status aktiv ist).
- Visibility/Animation/Render zeigen anschließend den finalen Icon-State stabil und animiert an.
