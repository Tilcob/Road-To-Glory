package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.screen.ScreenNavigator;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.ui.model.GameViewModel;

public class CombatSystemsInstaller implements SystemInstaller {
        private final AudioManager audioManager;
        private final GameEventBus eventBus;
        private final GameViewModel gameViewModel;
        private final ScreenNavigator screenNavigator;

        public CombatSystemsInstaller(
                AudioManager audioManager,
                GameEventBus eventBus,
                GameViewModel gameViewModel,
                ScreenNavigator screenNavigator) {
            this.audioManager = audioManager;
            this.eventBus = eventBus;
            this.gameViewModel = gameViewModel;
            this.screenNavigator = screenNavigator;
        }

        @Override
        public void install(Engine engine) {
            // Combat order: Attack -> Hit -> Damage -> Life -> Death -> GameOver
            engine.addSystem(withPriority(
                new AttackSystem(audioManager, eventBus),
                SystemOrder.COMBAT_ATTACK));
            engine.addSystem(withPriority(new AttackHitSystem(eventBus), SystemOrder.COMBAT_HIT));
            engine.addSystem(withPriority(new DamageApplySystem(eventBus), SystemOrder.COMBAT_DAMAGE));
            engine.addSystem(withPriority(new LifeSystem(gameViewModel), SystemOrder.COMBAT_LIFE));
            engine.addSystem(withPriority(new DeathSystem(eventBus), SystemOrder.COMBAT_DEATH));
            engine.addSystem(withPriority(new GameOverSystem(screenNavigator, eventBus),
                SystemOrder.COMBAT_GAME_OVER));
        }
}
