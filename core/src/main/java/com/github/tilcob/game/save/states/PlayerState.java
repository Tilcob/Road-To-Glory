package com.github.tilcob.game.save.states;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.item.ItemType;

public class PlayerState {
    private Vector2 position;
    private float life;
    private Array<String> items;

    public PlayerState(Vector2 position, float life, Array<Entity> items) {
        this.position = new Vector2(position);
        this.life = life;
        this.items = extractItemType(items);
    }

    public PlayerState() {
        this.position = new Vector2();
        this.life = 0;
        this.items = new Array<>();
    }

    private Array<String > extractItemType(Array<Entity> items) {
        Array<String> newItems = new Array<>();

        for (Entity itemEntity : items) {
            Item item = Item.MAPPER.get(itemEntity);
            newItems.add(item.getItemType().toString());
        }

        return newItems;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getLife() {
        return life;
    }

    public Array<ItemType> getItems() {
        Array<ItemType> newItems = new Array<>();
        for (String item : items) {
            newItems.add(ItemType.valueOf(item));
        }
        return newItems;
    }
}
