package com.github.tilcob.game.stat;

public class StatModifier {
    private StatType statType;
    private float additive;
    private float multiplier;
    private String source;
    private Float durationSeconds;
    private Long expireTimeEpochMs;

    public StatModifier(StatType statType, float additive, float multiplier, String source) {
        this.statType = statType;
        this.additive = additive;
        this.multiplier = multiplier;
        this.source = source;
    }

    public StatModifier(
        StatType statType,
        float additive,
        float multiplier,
        String source,
        Float durationSeconds,
        Long expireTimeEpochMs
    ) {
        this.statType = statType;
        this.additive = additive;
        this.multiplier = multiplier;
        this.source = source;
        this.durationSeconds = durationSeconds;
        this.expireTimeEpochMs = expireTimeEpochMs;
    }

    public StatType getStatType() {
        return statType;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public float getAdditive() {
        return additive;
    }

    public void setAdditive(float additive) {
        this.additive = additive;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Float getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Float durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getExpireTimeEpochMs() {
        return expireTimeEpochMs;
    }

    public void setExpireTimeEpochMs(Long expireTimeEpochMs) {
        this.expireTimeEpochMs = expireTimeEpochMs;
    }
}
