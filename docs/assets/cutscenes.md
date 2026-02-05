# Cutscene Yarn conventions

Cutscene scripts use standard Yarn Spinner command lines (`<<command args>>`), one per
line in the cutscene node. The cutscene runtime executes commands sequentially and can
pause when a command requests it (timer, camera pan, movement, or dialog start).

## Built-in cutscene commands

The following commands are available in cutscenes. Use the exact command names shown.
- `<varible>` placeholders are mandatory.
- `[varible]` brackets are optional.
- `<entity>` is if the player should be targeted you write `player` or `self`. For other entities type the entity `name` from Tiled in this field.

### Flow control

- `<<wait <seconds>>>`: pause for a number of seconds.
- `<<wait_for_camera>>`: pause until the active camera pan finishes.
- `<<wait_for_move>>`: pause until the active movement intent completes.

### Player control

- `<<lock_player>>`: restricts player input to interact/pause only.
- `<<unlock_player>>`: restores player input.

### Camera

- `<<camera_move <x> <y> <seconds>>>`: pan the camera to a world position.
- `<<camera_move_back <seconds>>>`: pan the camera back to the player's home position.

### Movement and facing

- `<<move_to <entity> <x> <y> [arrivalDistance]>>`: move an entity by a relative offset.
  - `arrivalDistance`: is the tolerance distance for the movement to complete. If nothing is specified, the default is 0.1.
- `<<face <entity> <direction>>`: face an entity `UP`, `DOWN`, `LEFT`, or `RIGHT`.

### Animation

- `<<play_anim <entity> <type> [playMode]>>`: set an entity animation (e.g. `IDLE`,
  `WALK`) with optional `LOOP`/`NORMAL` play mode.
- `<<play_indicator <entity> <indicatorType> [seconds]>>`: plays/updates an overhead indicator on an
  entity, even if no role is configured. `seconds` is optional and controls how long the indicator
  override should stay active before the previous state is restored. Valid `indicatorType` values:
  `QUEST_AVAILABLE`, `QUEST_TURNING`, `DANGER`, `ANGRY`, `INFO`, `TALKING`.

### Screen fades

- `<<fade_in [seconds]>>`: fade the screen in to full visibility.
- `<<fade_out [seconds]>>`: fade the screen out to black.

### Audio

- `<<play_music <musicAsset>>>`: play looping music by asset name (see `MusicAsset` enum).
- `<<play_sound <soundAsset>>>`: play a sound effect by asset name (see `SoundAsset` enum).

### Dialog and flags

- `<<start_dialog <npcId> [nodeId]>>`: start a dialog and pause the cutscene until the
  dialog finishes.
- `<<set_flag <flag> <true|false>>>`: set a dialog flag on the player.

## Example

```yarn
<<lock_player>>
<<camera_move 10 10 2>>
<<wait_for_camera>>
<<start_dialog shopkeeper cutscene_intro>>
<<unlock_player>>
```

````yarn
title: example_new_commands
tags: example
position: 0,0
---
<<lock_player>>
<<fade_out 0.5>>
<<wait 0.6>>
<<fade_in 0.5>>
<<camera_move 10 10 1.5>>
<<wait_for_camera>>
<<camera_move_relative -2 1 1.0>>
<<wait_for_camera>>
<<move_to player 12 8 0.1>>
<<wait_for_move>>
<<face player LEFT>>
<<play_anim player WALK LOOP>>
<<wait 0.5>>
<<play_anim player IDLE LOOP>>
<<camera_move_back 1.2>>
<<wait_for_camera>>
<<unlock_player>>
===
````
