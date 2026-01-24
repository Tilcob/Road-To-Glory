package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.GameOverEvent;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class GameOverSystem extends EntitySystem implements Disposable {
    private final ScreenNavigator screenNavigator;
    private final GameEventBus eventBus;

    public GameOverSystem(ScreenNavigator screenNavigator, GameEventBus eventBus) {
        this.screenNavigator = screenNavigator;
        this.eventBus = eventBus;

        eventBus.subscribe(GameOverEvent.class, this::onGameOver);
    }

    private void onGameOver(GameOverEvent event) {
        screenNavigator.setScreen(MenuScreen.class);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(GameOverEvent.class, this::onGameOver);
    }
}
