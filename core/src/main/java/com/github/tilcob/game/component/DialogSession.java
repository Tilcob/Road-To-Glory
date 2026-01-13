package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

public class DialogSession implements Component {
    public static final ComponentMapper<DialogSession> MAPPER = ComponentMapper.getFor(DialogSession.class);

    private final Entity npc;
    private final Array<String> lines;
    private int index;

    public DialogSession(Entity npc, Array<String> lines) {
        this.npc = npc;
        this.lines = lines == null ? new Array<>() : lines;
        this.index = 0;
    }

    public Entity getNpc() {
        return npc;
    }

    public String currentLine() {
        if (lines.isEmpty()) {
            return "";
        }
        return lines.get(index);
    }

    public boolean hasLines() {
        return !lines.isEmpty();
    }

    public boolean advance() {
        index++;
        return index < lines.size;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return lines.size;
    }
}
