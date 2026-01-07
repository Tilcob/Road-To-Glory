package com.github.tilcob.game.save.states;

import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemType;

import java.util.HashMap;
import java.util.Map;

public class PlayerState {
    private float posX;
    private float posY;
    private float life;
    private Map<ItemType, Integer> items = new HashMap<>();

    public PlayerState() { }

    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }

    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getLife() { return life; }
    public void setLife(float life) { this.life = life; }

    public Map<ItemType, Integer> getItems() { return items; }
    public void setItems(Map<ItemType, Integer> items) { this.items = items; }

    @JsonIgnore
    public Vector2 getPositionAsVector() {
        return new Vector2(posX, posY);
    }
}
