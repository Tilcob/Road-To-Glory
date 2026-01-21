package com.github.tilcob.game.config;

import com.badlogic.gdx.math.Vector2;

public class Constants {
    private Constants() {}

    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("game.debug", "false"));

    // Window
    // 16 units wide and 9 units high. 1 unit ~ 1 Tile (32px * 32px || 16px * 16px || ...)
    public static final float WIDTH = 16f;
    public static final float HEIGHT = 9f;
    public static final int WINDOW_FACTOR = 100;

    public static final float UNIT_SCALE = 1f / 16f;
    public static final float FRAME_DURATION = 1 / 8f;
    public static final float FIXED_INTERVAL = 1 / 60f;
    public static final float DEFAULT_ANIMATION_SPEED = 1f;
    public static final float DEFAULT_ARRIVAL_DISTANCE = 0.1f;
    public static final float DEFAULT_DAMAGE_DELAY = .2f;
    public static final float GAMEPLAY_COMMAND_BUFFER_SECONDS = 0.15f;
    public static final float WANDER_RADIUS = 1.5f;
    public static final float HEARING_RANGE = 2.5f;
    public static final float AGGRO_FORGET_TIME = 2.5f;
    public static final float LEASH_RANGE = 6f;
    public static final float PATROL_WAIT_TIME = 1.5f;

    // Tiled Layers
    public static final String OBJECT_LAYER = "objects";
    public static final String TRIGGER_LAYER = "trigger";
    public static final String GROUND_LAYER = "ground";
    public static final String BACKGROUND_LAYER = "background";
    public static final String FORE_LAYER = "foreground";


    public static final String ENVIRONMENT = "environment";

    // Tiled: Custom Properties
    public static final String ANIMATION = "animation";
    public static final String ATLAS_ASSET = "atlasAsset";
    public static final String ANIMATION_SPEED = "animationSpeed";
    public static final String SPEED = "speed";
    public static final String CONTROLLER = "controller";
    public static final String Z = "z";
    public static final String MAP_ASSET = "mapAsset";
    public static final String FRICTION = "friction";
    public static final String RESTITUTION = "restitution";
    public static final String DENSITY = "density";
    public static final String SENSOR = "sensor";
    public static final String PROP = "Prop";
    public static final String CAMERA_FOLLOW = "cameraFollow";
    public static final String LIFE = "life";
    public static final String LIFE_REGENERATION = "lifeRegeneration";
    public static final String ATTACK_SOUND = "attackSound";
    public static final String DAMAGE = "damage";
    public static final String ATTACK_WINDUP = "attackWindup";
    public static final String ATTACK_COOLDOWN = "attackCooldown";
    public static final String ATTACK_SENSOR = "attack_sensor_";
    public static final String ID = "id";
    public static final String BODY_TYPE = "bodyType";
    public static final String HAS_INVENTORY = "hasInventory";
    public static final String TRIGGER_TYPE = "triggerType";
    public static final String TO_MAP = "toMap";
    public static final String SPAWN_CLASS = "PlayerSpawn";
    public static final String LOOT = "loot";
    public static final String QUEST_ID = "questId";
    public static final String NPC_TYPE = "npcType";
    public static final String DIALOG_REQUEST = "dialog_request";
    public static final String PATROL_POINTS = "patrolPoints";
    public static final String PATROL_LOOP = "patrolLoop";
    public static final String PATROL_WAIT = "patrolWait";

    // Map Properties
    public static final String MAP_WIDTH = "width";
    public static final String MAP_HEIGHT = "height";
    public static final String TILE_WIDTH = "tilewidth";
    public static final String TILE_HEIGHT = "tileheight";
    public static final String MUSIC = "music";
    public static final String TYPE = "type";   // That's the class (Klasse) field in Tiled
    public static final String TRIGGER_CLASS = "Trigger";


    // Box2D / Physics constants
    public static final Vector2 GRAVITY = Vector2.Zero;
    public static final Vector2 DEFAULT_PHYSIC_SCALING = new Vector2(1f,1f);
    public static final int MAX_NUM_OF_VERTICES = 8; // must be in between 3 and 8
    public static final float AGGRO_RANGE = 5f; // 5 tiles range
    public static final float ENEMY_ATTACK_RANGE = 0.75f;
    public static final float WANDER_TIMER_INTERVAL = 5f;

    // Camera
    public static final float CAMERA_OFFSET_Y = 1f;

    // Property Changes
    public static final String LIFE_POINTS_PC = "lifePoints";
    public static final String MAX_LIFE_PC = "maxLife";
    public static final String PLAYER_DAMAGE_PC = "playerDamage";
    public static final String OPEN_INVENTORY = "openInventory";
    public static final String ADD_ITEMS_TO_INVENTORY = "addItems";
    public static final String ON_LEFT = "onLeft";
    public static final String ON_RIGHT = "onRight";
    public static final String ON_UP = "onUp";
    public static final String ON_DOWN = "onDown";
    public static final String ON_SELECT = "onSelect";
    public static final String ADD_QUESTS = "addQuests";
    public static final String SHOW_DIALOG = "showDialog";
    public static final String HIDE_DIALOG = "hideDialog";
    public static final String SHOW_DIALOG_CHOICES = "showDialogChoices";
    public static final String HIDE_DIALOG_CHOICES = "hideDialogChoices";
    public static final String SHOW_REWARD_DIALOG = "showRewardDialog";
    public static final String HIDE_REWARD_DIALOG = "hideRewardDialog";

    // Player properties
    public static final int PLAYER_ID = 4;
    public static final int INVENTORY_CAPACITY = 18;
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 6;
    public static final String BASE_STAMINA = "stamina";
    public static final String BASE_STRENGTH = "strength";
}
