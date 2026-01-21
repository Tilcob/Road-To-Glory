# ControllerSystem

## Purpose
Translates input commands into movement and interaction actions for entities with controllers.

## Flow
- Sets movement direction based on held commands (blocked during dialog selection).
- Processes commands from the command buffer (e.g., Attack, Pause, Interact, Inventory).
- Drives dialog navigation, starts attacks, and triggers interactions with chests or NPCs.

## Key components & events
- Controller
- Move
- DialogSession
- GameEventBus
- Command
