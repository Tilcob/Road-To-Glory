package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class Damaged implements Component {
    public static final ComponentMapper<Damaged> MAPPER = ComponentMapper.getFor(Damaged.class);

    private float damage;
    private Entity source;

    public Damaged(float damage, Entity source) {
        this.damage = damage;
        this.source = source;
    }

    public void addDamage(float value, Entity source) {
        damage += value;
        if (this.source == null) {
            this.source = source;
        }
    }

    public float getDamage() {
        return damage;
    }

    public Entity getSource() {
        return source;
    }
}
