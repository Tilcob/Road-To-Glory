package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.component.Skill;
import com.github.tilcob.game.event.LevelUpEvent;
import com.github.tilcob.game.save.states.SkillTreeState;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.SkillUnlockEvent;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.skill.SkillTreeLoader;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;

public class SkillTreeViewModel extends ViewModel {
    private boolean open = false;
    private static final String DEFAULT_TREE_ID = "combat";
    private String activeTreeId;

    public SkillTreeViewModel(GameServices services) {
        super(services);
        activeTreeId = resolveInitialTreeId();
        gameEventBus.subscribe(LevelUpEvent.class, this::onLevelUp);
        gameEventBus.subscribe(SkillUnlockEvent.class, this::onSkillUnlock);
    }

    @Override
    protected void onUiEvent(UiEvent event) {
        if (event.action() == UiEvent.Action.RELEASE) return;
        if (event.command() == Command.SKILLS) {
            setOpen(!open);
        } else if (event.command() == Command.CANCEL || event.command() == Command.PAUSE) {
            if (open) setOpen(false);
        }
    }

    public void setOpen(boolean open) {
        this.open = setOpen(open, this.open, Constants.OPEN_SKILLS);
    }

    public boolean isOpen() {
        return open;
    }

    public SkillTreeDefinition getTreeDefinition() {
        return SkillTreeLoader.get(activeTreeId);
    }

    public SkillTreeDefinition getTreeDefinition(String treeId) {
        return SkillTreeLoader.get(treeId);
    }

    public SkillTreeState getTreeState() {
        Skill comp = Skill.MAPPER.get(services.getEntityLookup().getPlayer());
        if (comp == null) return null;
        return comp.getTreeState(activeTreeId);
    }

    public void unlockNode(String nodeId) {
        if (services.getEntityLookup().getPlayer() == null) return;
        getEventBus().fire(new SkillUnlockEvent(services.getEntityLookup().getPlayer(), activeTreeId, nodeId));
    }

    private void onLevelUp(LevelUpEvent event) {
        if (!isDefaultTreeForPlayer(event.entity(), event.treeId())) return;
        propertyChangeSupport.firePropertyChange(Constants.SKILL_TREE_UPDATED, null, true);
    }

    private void onSkillUnlock(SkillUnlockEvent event) {
        if (!isDefaultTreeForPlayer(event.entity(), event.treeId())) return;
        propertyChangeSupport.firePropertyChange(Constants.SKILL_TREE_UPDATED, null, true);
    }

    private boolean isDefaultTreeForPlayer(Object entity, String treeId) {
        if (!activeTreeId.equals(treeId)) return false;
        return services.getEntityLookup().getPlayer() == entity;
    }

    public String getActiveTreeId() {
        return activeTreeId;
    }

    public void setActiveTreeId(String treeId) {
        if (treeId == null || treeId.isBlank() || treeId.equals(activeTreeId)) return;
        String previousTreeId = activeTreeId;
        activeTreeId = treeId;
        propertyChangeSupport.firePropertyChange(Constants.SKILL_TREE_UPDATED, previousTreeId, treeId);
    }

    public java.util.List<String> getTreeIds() {
        return SkillTreeLoader.getIds();
    }

    private String resolveInitialTreeId() {
        java.util.List<String> treeIds = SkillTreeLoader.getIds();
        if (!treeIds.isEmpty()) {
            return treeIds.get(0);
        }
        return DEFAULT_TREE_ID;
    }

    @Override
    public void dispose() {
        super.dispose();
        gameEventBus.unsubscribe(LevelUpEvent.class, this::onLevelUp);
        gameEventBus.unsubscribe(SkillUnlockEvent.class, this::onSkillUnlock);
    }
}
