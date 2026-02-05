# Overhead Indicator Workflow

This document describes the **complete workflow** for overhead indicators (e.g. quest exclamation marks) in *Road To Glory* – from a PNG file to rendering the indicator above an NPC.

---

## 1. Goal

Overhead indicators are small icons that **hover above entities** (NPCs, map objects) to visualize states such as *quest available*.

Key properties:
- loaded **via a TextureAtlas**
- **not part of the entity sprite**
- **centrally registered**
- position, size and animation are **configurable**

---

## 2. Asset Workflow (PNG → Atlas)

### 2.1 Prepare the PNG

Recommended sizes:
- **32×32 px** → default (quests, important markers)
- **16×16 px** → subtle markers (talk, info)

Rules:
- square images
- no whitespace padding
- pixel art: clean edges, optionally a 1 px outline

Example:
```
quest_available.png
```

---

### 2.2 Place the PNG in the TexturePacker input directory

Put the PNG into the directory used by your **TexturePacker** as input:

```
assets_raw/indicators/quest_available.png
```

> ⚠️ Do not place PNGs directly into the runtime assets directory (`assets/graphics/...`).

---

### 2.3 Repack the atlas

Run the existing Gradle task:

```bash
./gradlew packIndicatorsAtlas
```

Result:
```
assets/graphics/indicators.atlas
assets/graphics/indicators.png
```

The **atlas region name** equals the PNG file name:
```
quest_available
```

---

## 3. AtlasAsset

The atlas is already registered in the `AtlasAsset` enum:

```java
INDICATORS("graphics/indicators.atlas");
```

The atlas is automatically queued and loaded during game startup.

---

## 4. OverheadIndicatorRegistry

### 4.1 Purpose

`OverheadIndicatorRegistry` maps:

- an `OverheadIndicatorType`
- to an atlas
- to a region name
- optionally scale and vertical offset

Registration happens **once at startup**, not per entity.

---

### 4.2 Registration

**Location:** `GameLoader.queueAll()` (or equivalent content initialization method)

```java
OverheadIndicatorRegistry.clear();

OverheadIndicatorRegistry.register(
    OverheadIndicatorType.QUEST_AVAILABLE,
    AtlasAsset.INDICATORS,
    "quest_available"
);
```

This makes the indicator available globally.

---

## 5. Assigning Indicators to Entities

Entities receive an overhead indicator via components, typically during map/entity setup or via systems reacting to game state.

Example:
- NPC has `NpcRole.QUEST_GIVER`
- Quest state system assigns `OverheadIndicatorType.QUEST_AVAILABLE`

The render system resolves the type via the registry.

---

## 6. Rendering

During rendering, the system:

1. retrieves the `TextureRegion` from the atlas
2. converts pixel size to world units (`UNIT_SCALE`)
3. applies scale
4. applies vertical offset and animation
5. draws the region via `SpriteBatch`

Typical code:

```java
float w = region.getRegionWidth() * UNIT_SCALE * scale;
float h = region.getRegionHeight() * UNIT_SCALE * scale;

float drawX = position.x + (entityWidth - w) * 0.5f;
float drawY = position.y + offsetY + animationOffsetY;

batch.draw(region, drawX, drawY, w, h);
```

---

## 7. Offsets and Scaling

### Recommended defaults

| PNG Size | Render Scale | Vertical Offset |
|--------|-------------|----------------|
| 32×32  | 0.6         | −4 px          |
| 16×16  | 1.0         | −2 px          |

Offsets are applied in **world units**:

```java
offsetY -= 4f * UNIT_SCALE; // 4 pixels
```

---

## 8. Hot Reload (Optional)

If hot reload is enabled:

1. add / modify PNG
2. repack atlas
3. reload atlas asset
4. clear and re-register the `OverheadIndicatorRegistry`

This allows live iteration without restarting the game.

---

## 9. Mental Model

> **PNG → TexturePacker → Atlas → Registry → Entity → Render System**

Each step has a single responsibility and keeps the system modular and extensible.

---

## 10. Common Mistakes

- registering indicators per entity instead of once
- using PNG textures directly instead of atlas regions
- forgetting to repack the atlas after adding a PNG
- incorrect region names
- offsets specified in pixels instead of world units

---

This workflow is designed to scale cleanly as more indicators are added.

