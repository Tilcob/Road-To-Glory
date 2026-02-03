package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.component.Skill;
import com.github.tilcob.game.save.states.SkillTreeState;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.skill.SkillTreeLoader;
import com.github.tilcob.game.skill.data.SkillNodeDefinition;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

import java.util.Map;

public class SkillSystem extends IteratingSystem {
    private final GameEventBus eventBus;

    public SkillSystem(GameEventBus eventBus) {
        super(Family.all(Skill.class).get());
        this.eventBus = eventBus;

        eventBus.subscribe(ExpGainEvent.class, this::onXpGain);
        eventBus.subscribe(SkillUnlockEvent.class, this::onSkillUnlock);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Nothing to process per frame currently
    }

    private void onXpGain(ExpGainEvent event) {
        Skill skillComp = Skill.MAPPER.get(event.entity());
        if (skillComp == null) return;

        SkillTreeDefinition def = SkillTreeLoader.get(event.treeId());
        if (def == null || def.getXpTable() == null) return;

        SkillTreeState state = skillComp.getTreeState(event.treeId());
        state.setCurrentXp(state.getCurrentXp() + event.amount());

        checkLevelUp(event.entity(), event.treeId(), skillComp, state, def);
    }

    private void checkLevelUp(Entity entity, String treeId, Skill skill, SkillTreeState state, SkillTreeDefinition def) {
        int[] xpTable = def.getXpTable();
        int levelsGained = 0;

        while (state.getCurrentLevel() < xpTable.length && state.getCurrentXp() >= xpTable[state.getCurrentLevel()]) {
            state.setCurrentLevel(state.getCurrentLevel() + 1);
            if (skill != null) {
                skill.addSharedSkillPoints(1);
            }
            levelsGained++;
        }

        if (levelsGained > 0) {
            eventBus.fire(new LevelUpEvent(entity, treeId, levelsGained, state.getCurrentLevel() + 1));
        }
    }

    private void onSkillUnlock(SkillUnlockEvent event) {
        Skill skillComp = Skill.MAPPER.get(event.entity());
        if (skillComp == null) return;

        SkillTreeDefinition treeDef = SkillTreeLoader.get(event.treeId());
        if (treeDef == null || treeDef.getNodes() == null) return;
        SkillTreeState state = skillComp.getTreeState(event.treeId());

        if (state.isUnlocked(event.nodeId())) return;

        SkillNodeDefinition nodeDef = treeDef.getNodes().stream()
                .filter(n -> n.getId().equals(event.nodeId()))
                .findFirst()
                .orElse(null);

        if (nodeDef == null) return;
        int displayLevel = state.getCurrentLevel() + 1;
        if (skillComp.getSharedSkillPoints() < nodeDef.getCost()) return;
        if (displayLevel < nodeDef.getRequiredLevel()) return;

        if (nodeDef.getParentIds() != null) {
            for (String parentId : nodeDef.getParentIds()) {
                if (!state.isUnlocked(parentId)) return;
            }
        }

        if (!skillComp.spendSharedSkillPoints(nodeDef.getCost())) return;
        state.addUnlockedNode(event.nodeId());

        applyModifiers(event.entity(), nodeDef);

        Gdx.app.log("SkillSystem", "Unlocked node: " + event.nodeId() + " in " + event.treeId());
    }

    private void applyModifiers(Entity entity, SkillNodeDefinition nodeDef) {
        if (nodeDef.getModifiers() == null || nodeDef.getModifiers().isEmpty()) return;

        StatModifierComponent modifierComp = StatModifierComponent.MAPPER.get(entity);
        if (modifierComp == null) return;

        for (Map.Entry<StatType, Float> entry : nodeDef.getModifiers().entrySet()) {
            String source = "skill:" + nodeDef.getId();
            modifierComp.addModifier(new StatModifier(entry.getKey(), entry.getValue(), 0f, source));
        }

        eventBus.fire(new StatRecalcEvent(entity));
    }
}
