package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.stat.StatType;

import java.util.EnumMap;
import java.util.Map;

public class StatComponent implements Component {
    public static final ComponentMapper<StatComponent> MAPPER = ComponentMapper.getFor(StatComponent.class);

    private final EnumMap<StatType, Float> baseStats = new EnumMap<>(StatType.class);
    private final EnumMap<StatType, Float> finalStats = new EnumMap<>(StatType.class);

    public Map<StatType, Float> getBaseStats() {
        return baseStats;
    }

    public Map<StatType, Float> getFinalStats() {
        return finalStats;
    }

    public float getBaseStat(StatType type) {
        return baseStats.getOrDefault(type, 0f);
    }

    public void setBaseStat(StatType type, float value) {
        baseStats.put(type, value);
    }

    public float getFinalStat(StatType type) {
        return finalStats.getOrDefault(type, 0f);
    }

    public void setFinalStat(StatType type, float value) {
        finalStats.put(type, value);
    }
}
