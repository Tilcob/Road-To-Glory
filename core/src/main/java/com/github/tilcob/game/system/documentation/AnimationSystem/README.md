# AnimationSystem

## Zweck
Aktualisiert 2D-Animationen basierend auf Facing und AnimationType und schreibt den aktuellen Frame in die Graphic-Komponente.

## Ablauf
- Erkennt Änderungen an Animation2D (dirty) oder Facing-Wechsel und baut dann eine neue Animation aus dem TextureAtlas.
- Cached Animationen per Atlas/Key/Typ/Richtung, um wiederholte Lookups zu vermeiden.
- Setzt das PlayMode und überträgt den aktuellen Keyframe in Graphic.
- Synchronisiert bei Attack-Animationen die Windup-Dauer mit der Animationsdauer.

## Wichtige Komponenten & Ereignisse
- Animation2D
- Graphic
- Facing
- Attack
- AssetManager
- AtlasAsset
