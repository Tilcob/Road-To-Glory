# DamageSystem

## Zweck
Verarbeitet Schadensereignisse, reduziert Life, entfernt tote NPCs und triggert Quest-Signale.

## Ablauf
- Entfernt die Damaged-Komponente und reduziert Life entsprechend.
- Bei Tod eines Enemy-NPCs wird ein "kill"-Signal an den QuestManager gesendet.
- Erzeugt Schaden-Feedback über GameViewModel (z. B. Schadenszahlen).

## Wichtige Komponenten & Ereignisse
- Damaged
- Life
- Npc
- QuestManager
- GameViewModel
