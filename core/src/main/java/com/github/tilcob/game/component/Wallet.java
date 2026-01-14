package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Wallet implements Component {
    public static final ComponentMapper<Wallet> MAPPER = ComponentMapper.getFor(Wallet.class);

    private int money;

    public int getMoney() {
        return money;
    }

    public void earn(int amount) {
        money += amount;
    }

    public void spend(int amount) {
        money -= Math.max(0, money - amount);
    }
}
