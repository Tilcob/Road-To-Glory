package com.github.tilcob.game.ai.behavior;

import com.github.tilcob.game.npc.NpcType;

import java.util.EnumMap;
import java.util.Map;

public class NpcBehaviorRegistry {
    private static final Map<NpcType, NpcBehaviorProfile> PROFILES = new EnumMap<>(NpcType.class);
    private static final NpcBehaviorProfile DEFAULT_PROFILE = new FriendBehaviorProfile();

    static {
        register(NpcType.FRIEND, new FriendBehaviorProfile());
        register(NpcType.ENEMY, new EnemyBehaviorProfile());
        register(NpcType.GUARD, new GuardBehaviorProfile());
    }

    private NpcBehaviorRegistry() {}

    public static NpcBehaviorProfile get(NpcType type) {
        if (type == null) return DEFAULT_PROFILE;

        return PROFILES.getOrDefault(type, DEFAULT_PROFILE);
    }

    public static void register(NpcType type, NpcBehaviorProfile profile) {
        if (type == null || profile == null) return;
        PROFILES.put(type, profile);
    }
}
