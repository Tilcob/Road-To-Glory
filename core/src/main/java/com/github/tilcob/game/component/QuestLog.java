package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class QuestLog implements Component {
    public static final ComponentMapper<QuestLog> MAPPER = ComponentMapper.getFor(QuestLog.class);

    private final Array<Quest> quests = new Array<>();

    public Array<Quest> getQuests() {
        return quests;
    }

    public void add(Quest quest) {
        quests.add(quest);
    }

    public void remove(Quest quest) {
        quests.removeValue(quest, true);
    }

    public Quest getQuestById(String id) {
        for (Quest quest : quests) {
            if (quest.getQuestId().equals(id)) return quest;
        }
        return null;
    }

    public QuestState getQuestStateById(String id) {
        Quest quest = getQuestById(id);
        if (quest == null) return QuestState.NOT_STARTED;
        if (quest.isCompleted()) return QuestState.COMPLETED;
        return QuestState.IN_PROGRESS;
    }
}
