package com.github.tilcob.game.ui.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.EntityAddItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.input.Command;

import java.util.Map;

public class GameViewModel extends ViewModel {
    private final AudioManager audioManager;
    private int lifePoints;
    private int maxLife;
    private Map.Entry<Vector2, Integer> playerDamage;
    private final Array<ItemModel> playerItems = new Array<>();
    private boolean open = false;
    private final Vector2 tmpVec2;

    public GameViewModel(GdxGame game) {
        super(game, game.getEventBus());
        this.audioManager = game.getAudioManager();
        this.lifePoints = 0;
        this.maxLife = 0;
        this.playerDamage = null;
        this.tmpVec2 = new Vector2();

        game.getEventBus().subscribe(UiEvent.class, this::onUiEvent);
        game.getEventBus().subscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
    }

    private void onEntityAddItemEvent(EntityAddItemEvent event) {
        Inventory inventory = Inventory.MAPPER.get(event.getEntity());
        if (inventory == null) return;
        onAddItem(inventory.getItems());
        this.propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS, null, playerItems);
    }

    public void onAddItem(Array<Entity> items) {
        for (Entity itemEntity : items) {
            Item item = Item.MAPPER.get(itemEntity);
            ItemModel model = new ItemModel(
                1,
                item.getItemType().getCategory(),
                item.getItemType().getAtlasKey(),
                item.getSlotIndex(),
                item.isEquipped()
            );
            playerItems.add(model);
        }
    }

    private void onUiEvent(UiEvent event) {
        if (!(event.getCommand() == Command.INVENTORY)) return;

        boolean old = open;
        open = !open;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_INVENTORY, old, open);
    }

    public void playerDamage(int amount, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.playerDamage = Map.entry(position, amount);
        this.propertyChangeSupport.firePropertyChange(Constants.PLAYER_DAMAGE_PC, null, this.playerDamage);
    }

    public Vector2 toScreenCoords(Vector2 position) {
        tmpVec2.set(position);
        game.getViewport().project(tmpVec2);
        return tmpVec2;
    }

    public void updateLifeInfo(float maxLife, float life) {
        setMaxLife((int) maxLife);
        setLifePoints((int) life);
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void setLifePoints(int lifePoints) {
        if (this.lifePoints != lifePoints) {
            this.propertyChangeSupport.firePropertyChange(Constants.LIFE_POINTS_PC, this.lifePoints, lifePoints);
            if (this.lifePoints != 0 && this.lifePoints < lifePoints) {
                audioManager.playSound(SoundAsset.LIFE_REG);
            }
        }
        this.lifePoints = lifePoints;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        if (this.maxLife != maxLife) {
            this.propertyChangeSupport.firePropertyChange(Constants.MAX_LIFE_PC, this.maxLife, maxLife);
        }
        this.maxLife = maxLife;
    }

    public Array<ItemModel> getPlayerItems() {
        return playerItems;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void dispose() {
        gameEventBus.unsubscribe(UiEvent.class, this::onUiEvent);
        gameEventBus.unsubscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
    }
}
