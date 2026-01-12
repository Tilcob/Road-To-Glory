package com.github.tilcob.game.ai.behavior;

import com.github.tilcob.game.npc.NpcType;

import java.util.HashMap;
import java.util.Map;

public class NpcBehaviorRegistry {
    private static final Map<NpcType, NpcBehaviorProfile> PROFILES = new HashMap<>();
    private static final NpcBehaviorProfile DEFAULT_PROFILE = new FriendBehaviorProfile();

    static {
        PROFILES.put(NpcType.FRIEND, new FriendBehaviorProfile());
        PROFILES.put(NpcType.ENEMY, new EnemyBehaviorProfile());
    }

    private NpcBehaviorRegistry() {}

    public static NpcBehaviorProfile get(NpcType type) {
        if (type == null) return DEFAULT_PROFILE;

        return PROFILES.getOrDefault(type, DEFAULT_PROFILE);
    }
}
