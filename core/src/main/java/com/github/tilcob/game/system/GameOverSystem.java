package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class GameOverSystem extends IteratingSystem {
    private final ScreenNavigator screenNavigator;

    public GameOverSystem(ScreenNavigator screenNavigator) {
        super(Family.all(Life.class, Player.class).get());
        this.screenNavigator = screenNavigator;
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Life life = Life.MAPPER.get(player);
        if (life.getLife() <= .01f) {
            screenNavigator.setScreen(MenuScreen.class);
        }
    }
}
