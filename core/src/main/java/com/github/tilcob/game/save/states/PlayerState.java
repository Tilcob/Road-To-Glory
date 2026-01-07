package com.github.tilcob.game.save.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemType;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    private float posX;
    private float posY;
    private float life;
    @JsonIgnore
    private List<ItemType> items = new ArrayList<>();
    private List<String> itemsByName = new ArrayList<>();

    public PlayerState() { }

    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }

    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getLife() { return life; }
    public void setLife(float life) { this.life = life; }

    public List<String> getItemsByName() {
        return itemsByName;
    }

    public void setItemsByName(List<String> itemsByName) {
        this.itemsByName = itemsByName;
    }

    @JsonIgnore
    public List<ItemType> getItems() {
        return items;
    }
    @JsonIgnore
    public void setItems(List<ItemType> items) {
        this.items = items;
    }

    @JsonIgnore
    public void rebuildItemsByName() {
        items.clear();
        for (String name : itemsByName) {
            try {
                items.add(ItemType.valueOf(name));
            } catch (IllegalArgumentException e) {
                Gdx.app.error("PlayerState", e.getMessage());
            }
        }
    }

    @JsonIgnore
    public Vector2 getPositionAsVector() {
        return new Vector2(posX, posY);
    }
}
