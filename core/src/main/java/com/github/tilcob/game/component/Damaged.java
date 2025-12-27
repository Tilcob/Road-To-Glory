package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Damaged implements Component {
    public static final ComponentMapper<Damaged> MAPPER = ComponentMapper.getFor(Damaged.class);

    private float damage;

    public Damaged(float damage) {
        this.damage = damage;
    }

    public void addDamage(float value) {
        damage += value;
    }

    public float getDamage() {
        return damage;
    }
}
