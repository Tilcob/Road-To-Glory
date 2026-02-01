package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.component.SkillComponent;
import com.github.tilcob.game.component.SkillTreeState;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.SkillUnlockEvent;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.skill.SkillTreeLoader;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;

public class SkillTreeViewModel extends ViewModel {
    private boolean open = false;
    // For now support just one tree or iterate? Let's assume for this MVP we show
    // "combat"
    private static final String DEFAULT_TREE_ID = "combat";

    public SkillTreeViewModel(GameServices services) {
        super(services);
    }

    @Override
    protected void onUiEvent(UiEvent event) {
        if (event.action() == UiEvent.Action.RELEASE)
            return;

        if (event.command() == Command.SKILLS) {
            // Toggle
            setOpen(!open);
        } else if (event.command() == Command.CANCEL || event.command() == Command.PAUSE) {
            if (open)
                setOpen(false);
        }
    }

    public void setOpen(boolean open) {
        if (this.open == open)
            return;
        this.open = open;
        propertyChangeSupport.firePropertyChange(Constants.OPEN_SKILLS, !open, open);
    }

    public boolean isOpen() {
        return open;
    }

    public SkillTreeDefinition getTreeDefinition() {
        return SkillTreeLoader.get(DEFAULT_TREE_ID);
    }

    public SkillTreeState getTreeState() {
        SkillComponent comp = SkillComponent.MAPPER.get(services.getEntityLookup().getPlayer());
        if (comp == null)
            return null;
        return comp.getTreeState(DEFAULT_TREE_ID);
    }

    public void unlockNode(String nodeId) {
        getEventBus().fire(new SkillUnlockEvent(services.getEntityLookup().getPlayer(), DEFAULT_TREE_ID, nodeId));
    }
}
