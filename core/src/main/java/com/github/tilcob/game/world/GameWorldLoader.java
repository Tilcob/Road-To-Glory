package com.github.tilcob.game.world;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.MapChangeEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.player.PlayerFactory;
import com.github.tilcob.game.player.PlayerStateApplier;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestLoader;
import com.github.tilcob.game.system.CameraSystem;
import com.github.tilcob.game.system.RenderSystem;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;

import java.util.function.Consumer;

public class GameWorldLoader {
    private final Dependencies dependencies;
    private Entity player;

    public GameWorldLoader(Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    public void initializeWorld() {
        configureTiledCallbacks();
        createPlayer();
    }

    public void loadState() {
        if (player == null) {
            createPlayer();
        }
        applyPlayerState();
        loadQuest();
    }

    public Entity getPlayer() {
        return player;
    }

    private void configureTiledCallbacks() {
        Consumer<TiledMap> renderConsumer = dependencies.engine().getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = dependencies.engine().getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = dependencies.audioManager()::setMap;

        dependencies.tiledManager().setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        dependencies.tiledManager().setLoadObjectConsumer(dependencies.tiledAshleyConfigurator()::onLoadObject);
        dependencies.tiledManager().setLoadTileConsumer(dependencies.tiledAshleyConfigurator()::onLoadTile);
        dependencies.tiledManager().setLoadTriggerConsumer(dependencies.tiledAshleyConfigurator()::onLoadTrigger);
    }

    private void loadQuest() {
        QuestLoader loader = new QuestLoader(new QuestFactory(dependencies.services().getQuestYarnRegistry()));
        QuestLog questLog = QuestLog.MAPPER.get(player);
        dependencies.services().getStateManager().loadQuests(questLog, loader);
    }

    private void createPlayer() {
        player = PlayerFactory.create(dependencies.engine(), dependencies.services().getAssetManager(), dependencies.physicWorld());
        dependencies.activeEntityReference().set(player);
        loadMap();
    }

    private void applyPlayerState() {
        if (dependencies.services().getStateManager().getGameState().getPlayerState() != null) {
            PlayerStateApplier.apply(dependencies.services().getStateManager().getGameState().getPlayerState(), player);
        } else {
            Transform.MAPPER.get(player).getPosition().set(dependencies.tiledManager().getSpawnPoint());
            Physic.MAPPER.get(player).getBody().setTransform(dependencies.tiledManager().getSpawnPoint(), 0);
        }
        dependencies.services().getStateManager().loadDialogFlags(DialogFlags.MAPPER.get(player));
        dependencies.services().getStateManager().loadCounters(Counters.MAPPER.get(player));
        dependencies.services().getStateManager().setPlayerState(player);
        dependencies.services().getEventBus().fire(new UpdateInventoryEvent(player));
    }

    private void loadMap() {
        MapAsset mapToLoad = dependencies.services().getStateManager().getGameState().getCurrentMap();
        if (mapToLoad == null) mapToLoad = MapAsset.MAIN;
        dependencies.tiledManager().setMap(dependencies.tiledManager().loadMap(mapToLoad));
        dependencies.services().getEventBus().fire(
            new MapChangeEvent(dependencies.tiledManager().getCurrentMapAsset().name().toLowerCase())
        );
    }

    public record Dependencies(
        GameServices services,
        Engine engine,
        TiledManager tiledManager,
        TiledAshleyConfigurator tiledAshleyConfigurator,
        com.github.tilcob.game.audio.AudioManager audioManager,
        World physicWorld,
        Viewport viewport,
        ActiveEntityReference activeEntityReference
    ) {}
}
