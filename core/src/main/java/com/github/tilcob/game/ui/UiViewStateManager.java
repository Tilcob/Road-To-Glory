package com.github.tilcob.game.ui;

import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.PauseEvent;
import com.github.tilcob.game.ui.model.ChestViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.SettingsViewModel;
import com.github.tilcob.game.ui.model.SkillTreeViewModel;

public final class UiViewStateManager {
    private final GameEventBus eventBus;
    private final InventoryViewModel inventoryViewModel;
    private final ChestViewModel chestViewModel;
    private final SkillTreeViewModel skillTreeViewModel;
    private final SettingsViewModel settingsViewModel;
    private boolean paused;

    public UiViewStateManager(
        GameEventBus eventBus,
        InventoryViewModel inventoryViewModel,
        ChestViewModel chestViewModel,
        SkillTreeViewModel skillTreeViewModel,
        SettingsViewModel settingsViewModel
    ) {
        this.eventBus = eventBus;
        this.inventoryViewModel = inventoryViewModel;
        this.chestViewModel = chestViewModel;
        this.skillTreeViewModel = skillTreeViewModel;
        this.settingsViewModel = settingsViewModel;

        if (inventoryViewModel != null) {
            inventoryViewModel.onPropertyChange(Constants.OPEN_INVENTORY, Boolean.class, this::onInventoryOpen);
        }
        if (chestViewModel != null) {
            chestViewModel.onPropertyChange(Constants.OPEN_CHEST_INVENTORY, Boolean.class, this::onChestOpen);
        }
        if (skillTreeViewModel != null) {
            skillTreeViewModel.onPropertyChange(Constants.OPEN_SKILLS, Boolean.class, this::onSkillTreeOpen);
        }
        if (settingsViewModel != null) {
            settingsViewModel.onPropertyChange(Constants.OPEN_SETTINGS, Boolean.class, this::onSettingsOpen);
        }
        if (eventBus != null) {
            eventBus.subscribe(PauseEvent.class, this::onPauseEvent);
        }
    }

    public void dispose() {
        if (eventBus != null) {
            eventBus.unsubscribe(PauseEvent.class, this::onPauseEvent);
        }
    }

    private void onInventoryOpen(Boolean open) {
        if (Boolean.TRUE.equals(open)) {
            closeAllExcept(UiView.INVENTORY);
        }
    }

    private void onChestOpen(Boolean open) {
        if (Boolean.TRUE.equals(open)) {
            closeAllExcept(UiView.CHEST);
        }
    }

    private void onSkillTreeOpen(Boolean open) {
        if (Boolean.TRUE.equals(open)) {
            closeAllExcept(UiView.SKILLS);
        }
    }

    private void onSettingsOpen(Boolean open) {
        if (Boolean.TRUE.equals(open)) {
            closeAllExcept(UiView.SETTINGS);
        }
    }

    private void onPauseEvent(PauseEvent event) {
        if (event == null) return;
        switch (event.action()) {
            case PAUSE -> {
                paused = true;
                closeAllExcept(UiView.PAUSE);
            }
            case RESUME -> paused = false;
            case TOGGLE -> {
                paused = !paused;
                if (paused) {
                    closeAllExcept(UiView.PAUSE);
                }
            }
        }
    }

    private void closeAllExcept(UiView view) {
        if (inventoryViewModel != null && view != UiView.INVENTORY) {
            inventoryViewModel.close();
        }
        if (chestViewModel != null && view != UiView.CHEST) {
            chestViewModel.close();
        }
        if (skillTreeViewModel != null && view != UiView.SKILLS) {
            skillTreeViewModel.setOpen(false);
        }
        if (settingsViewModel != null && view != UiView.SETTINGS && settingsViewModel.isOpen()) {
            settingsViewModel.close();
        }
        if (view != UiView.PAUSE && view != UiView.SETTINGS && paused) {
            eventBus.fire(new PauseEvent(PauseEvent.Action.RESUME));
        }
    }

    private enum UiView {
        INVENTORY,
        CHEST,
        SKILLS,
        PAUSE,
        SETTINGS
    }
}
