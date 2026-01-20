package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.stat.StatModifier;

public class StatModifierComponent implements Component {
    public static final ComponentMapper<StatModifierComponent> MAPPER = ComponentMapper.getFor(StatModifierComponent.class);

    private final Array<StatModifier> modifiers = new Array<>();

    public Array<StatModifier> getModifiers() {
        return modifiers;
    }

    public void addModifier(StatModifier modifier) {
        modifiers.add(modifier);
    }

    public void removeModifier(StatModifier modifier) {
        modifiers.removeValue(modifier, true);
    }
}
