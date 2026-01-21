# CameraSystem

## Zweck
Bewegt die Kamera weich zur Zielposition des Kamera-Targets und begrenzt die Position auf die Map-Grenzen.

## Ablauf
- Berechnet eine Zielposition aus Transform + Kamera-Offset.
- Clampt die Zielposition gegen die Map-Größe und interpoliert mit einem Smoothing-Faktor.
- SetMap liest Kachel- und Map-Größe aus den Tiled-Properties und initialisiert die Kamera-Position.

## Wichtige Komponenten & Ereignisse
- CameraFollow
- Transform
- TiledMap
- Camera
