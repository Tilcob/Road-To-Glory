package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.quest.Quest;

import java.util.ArrayList;
import java.util.List;

class QuestPanelController {
    private final Skin skin;
    private final Array<Quest> quests = new Array<>();
    private Table questLog;
    private Table questSteps;
    private Quest selectedQuest;

    QuestPanelController(Skin skin) {
        this.skin = skin;
    }

    void bindTables(Table questLog, Table questSteps) {
        this.questLog = questLog;
        this.questSteps = questSteps;
        if (this.questLog != null && this.questSteps != null) {
            rebuildQuestList();
        }
    }

    void updateQuests(Array<Quest> quests) {
        this.quests.clear();
        if (quests != null) {
            this.quests.addAll(quests);
        }
        Quest preserved = findPreservedQuest();
        selectedQuest = preserved;
        if (questLog != null && questSteps != null) {
            rebuildQuestList();
        }
    }

    private Quest findPreservedQuest() {
        if (selectedQuest == null) {
            return null;
        }
        for (Quest quest : quests) {
            if (quest.getQuestId().equals(selectedQuest.getQuestId())) {
                return quest;
            }
        }
        return null;
    }

    private void rebuildQuestList() {
        questLog.clear();
        if (quests.isEmpty()) {
            Label emptyLabel = new Label("No quests available.", skin, "text_08");
            emptyLabel.setColor(skin.getColor("BLACK"));
            questLog.add(emptyLabel).left().row();
            updateQuestSteps(null);
            return;
        }

        List<Quest> incomplete = new ArrayList<>();
        List<Quest> completed = new ArrayList<>();

        for (Quest quest : quests) {
            if (quest.isCompleted()) {
                completed.add(quest);
            } else {
                incomplete.add(quest);
            }
        }
        Quest defaultQuest = selectedQuest;
        for (Quest quest : incomplete) {
            buildQuestRow(quest);
            if (defaultQuest == null) {
                defaultQuest = quest;
            }
        }
        for (Quest quest : completed) {
            buildQuestRow(quest);
            if (defaultQuest == null) {
                defaultQuest = quest;
            }
        }
        if (defaultQuest != null) {
            setSelectedQuest(defaultQuest);
        } else {
            updateQuestSteps(null);
        }
    }

    private void buildQuestRow(Quest quest) {
        Table row = new Table();
        row.setTouchable(Touchable.enabled);
        row.pad(4.0f);
        Label label = new Label(resolveQuestTitle(quest), skin, "text_08");
        label.setColor(skin.getColor("BLACK"));
        label.setWrap(true);
        Image image = new Image(skin.getDrawable("Green_icon_outline_checkmark"));
        image.setVisible(quest.isRewardClaimed());
        row.add(label).left().expandX().fillX();
        row.add(image).right().padLeft(4.0f);

        row.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSelectedQuest(quest);
            }
        });

        questLog.add(row).expandX().fillX().row();
    }

    private void setSelectedQuest(Quest quest) {
        selectedQuest = quest;
        updateQuestSteps(quest);
    }

    private void updateQuestSteps(Quest quest) {
        questSteps.clear();
        if (quest == null) {
            Label emptyLabel = new Label("Select a quest to see its steps.", skin, "text_08");
            emptyLabel.setColor(skin.getColor("BLACK"));
            questSteps.add(emptyLabel).left().expandX().fillX().row();
            return;
        }

        Label titleLabel = new Label(resolveQuestTitle(quest), skin, "text_10");
        titleLabel.setColor(skin.getColor("BLACK"));
        titleLabel.setWrap(true);
        questSteps.add(titleLabel).left().expandX().fillX().row();

        if (quest.getDescription() != null && !quest.getDescription().isBlank()) {
            Label descLabel = new Label(quest.getDescription(), skin, "text_08");
            descLabel.setColor(skin.getColor("BLACK"));
            descLabel.setWrap(true);
            questSteps.add(descLabel).left().expandX().fillX().padTop(4.0f).row();
        }

        Label stepsHeader = new Label("Steps:", skin, "text_08");
        stepsHeader.setColor(skin.getColor("BLACK"));
        questSteps.add(stepsHeader).left().expandX().fillX().padTop(6.0f).row();

        List<String> journals = quest.getStepJournals();
        if (journals == null || journals.isEmpty()) {
            Label emptySteps = new Label("No steps available.", skin, "text_08");
            emptySteps.setColor(skin.getColor("BLACK"));
            questSteps.add(emptySteps).left().padLeft(12.0f).padTop(4.0f).row();
            return;
        }

        int currentStep = quest.getCurrentStep();
        for (int i = 0; i < journals.size(); i++) {
            String stepText = journals.get(i);
            if (stepText == null || stepText.isBlank()) {
                stepText = "Step " + (i + 1);
            }

            boolean completed = quest.isCompleted() || i < currentStep;
            Table stepRow = new Table();
            Label stepLabel = new Label(stepText, skin, "text_08");
            stepLabel.setColor(skin.getColor("BLACK"));
            stepLabel.setWrap(true);
            stepRow.add(stepLabel).left().expandX().fillX();

            if (completed) {
                Image image = new Image(skin.getDrawable("Green_icon_outline_checkmark"));
                stepRow.add(image).right().padLeft(4.0f);
            }
            questSteps.add(stepRow).expandX().fillX().padLeft(12.0f).padBottom(4.0f).row();
        }
    }

    private String resolveQuestTitle(Quest quest) {
        String questTitle = quest.getTitle();
        if (questTitle == null || questTitle.isBlank()) {
            questTitle = quest.getQuestId().replace("_", " ");
        }
        return questTitle;
    }
}
