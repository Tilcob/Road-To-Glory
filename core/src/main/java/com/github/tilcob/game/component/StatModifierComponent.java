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

    public void removeModifiersBySourcePrefix(String sourcePrefix) {
        if (sourcePrefix == null || sourcePrefix.isBlank()) {
            return;
        }
        for (int i = modifiers.size - 1; i >= 0; i--) {
            StatModifier modifier = modifiers.get(i);
            String source = modifier.getSource();
            if (source != null && source.startsWith(sourcePrefix)) {
                modifiers.removeIndex(i);
            }
        }
    }
}
