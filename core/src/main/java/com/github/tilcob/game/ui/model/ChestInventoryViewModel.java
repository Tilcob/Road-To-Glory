package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.PauseEvent;
import com.github.tilcob.game.event.OpenChestEvent;

public class ChestInventoryViewModel extends ViewModel implements Disposable {
    private final Array<ItemModel> items = new Array<>();
    private boolean open = false;
    private boolean paused = false;

    public ChestInventoryViewModel(GameServices services) {
        super(services);

        getEventBus().subscribe(OpenChestEvent.class, this::onUiEvent);
        getEventBus().subscribe(PauseEvent.class, this::onPauseEvent);
    }

    private void onUiEvent(OpenChestEvent event) {
        open = !open;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_CHEST_INVENTORY, null, open);
    }

    private void onPauseEvent(PauseEvent event) {
        if (event == null) return;
        switch (event.action()) {
            case PAUSE -> {
                paused = true;
                closeInventory();
            }
            case RESUME -> paused = false;
            case TOGGLE -> {
                paused = !paused;
                if (paused) closeInventory();
            }
        }
    }

    private void closeInventory() {
        if (!open) return;
        boolean old = true;
        open = false;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_CHEST_INVENTORY, old, false);
    }

    @Override
    public void dispose() {

    }
}
