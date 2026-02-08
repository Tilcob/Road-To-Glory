package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.physics.box2d.World;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.screen.ScreenNavigator;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.ui.model.GameViewModel;

public class CombatSystemsInstaller implements SystemInstaller {
        private final AudioManager audioManager;
        private final GameEventBus eventBus;
        private final GameViewModel gameViewModel;
        private final QuestManager questManager;
        private final ScreenNavigator screenNavigator;

        public CombatSystemsInstaller(
                AudioManager audioManager,
                GameEventBus eventBus,
                GameViewModel gameViewModel,
                QuestManager questManager,
                ScreenNavigator screenNavigator) {
            this.audioManager = audioManager;
            this.eventBus = eventBus;
            this.gameViewModel = gameViewModel;
            this.questManager = questManager;
            this.screenNavigator = screenNavigator;
        }

        @Override
        public void install(Engine engine) {
            engine.addSystem(withPriority(
                new AttackSystem(audioManager, eventBus),
                SystemOrder.COMBAT));
            engine.addSystem(withPriority(new AttackHitSystem(eventBus), SystemOrder.COMBAT));
            engine.addSystem(withPriority(
                new DamageSystem(questManager, eventBus),
                SystemOrder.COMBAT));
            engine.addSystem(withPriority(new LifeSystem(gameViewModel), SystemOrder.COMBAT));
            engine.addSystem(withPriority(new GameOverSystem(screenNavigator, eventBus), SystemOrder.COMBAT));
            engine.addSystem(withPriority(
                new TriggerSystem(audioManager, eventBus),
                SystemOrder.COMBAT));
        }
}
