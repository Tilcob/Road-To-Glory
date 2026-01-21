# AnimationSystem

## Purpose
Updates 2D animations based on facing and `AnimationType`, and writes the current frame into the `Graphic` component.

## Flow
- Detects changes on `Animation2D` (dirty) or facing changes, then builds a new animation from the TextureAtlas.
- Caches animations by atlas/key/type/direction to avoid repeated lookups.
- Sets the PlayMode and transfers the current keyframe to `Graphic`.
- Syncs attack animation windup duration with the animation duration.

## Key components & events
- Animation2D
- Graphic
- Facing
- Attack
- AssetManager
- AtlasAsset
