package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.quest.Quest;

public class QuestLog implements Component {
    public static final ComponentMapper<QuestLog> MAPPER = ComponentMapper.getFor(QuestLog.class);

    private final Array<Quest> quests = new Array<>();

    public Array<Quest> getQuests() {
        return quests;
    }

    public void add(Quest quest) {
        quests.add(quest);
        if (!quest.getSteps().isEmpty()) quest.getSteps().get(0).start();
    }

    public void remove(Quest quest) {
        quests.removeValue(quest, true);
    }
}
