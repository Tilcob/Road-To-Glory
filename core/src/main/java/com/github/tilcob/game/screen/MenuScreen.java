package com.github.tilcob.game.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.MusicAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.input.IdleControllerState;
import com.github.tilcob.game.input.InputManager;
import com.github.tilcob.game.input.UiControllerState;
import com.github.tilcob.game.ui.model.MenuViewModel;
import com.github.tilcob.game.ui.view.MenuView;

public class MenuScreen extends ScreenAdapter {
    private final GameServices services;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;
    private final InputManager inputManager;
    private final ScreenNavigator screenNavigator;
    private final IdleControllerState idleControllerState;
    private final UiControllerState uiControllerState;

    public MenuScreen(
        GameServices services,
        com.badlogic.gdx.graphics.g2d.Batch batch,
        InputManager inputManager,
        Viewport uiViewport,
        ScreenNavigator screenNavigator
    ) {
        this.services = services;
        this.uiViewport = uiViewport;
        this.stage = new Stage(uiViewport, batch);
        this.skin = services.getAssetManager().get(SkinAsset.DEFAULT);
        this.inputManager = inputManager;
        this.screenNavigator = screenNavigator;
        this.idleControllerState = new IdleControllerState();
        this.uiControllerState = new UiControllerState(services.getEventBus());
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        inputManager.setInputProcessors(stage);
        inputManager.configureStates(UiControllerState.class, idleControllerState, uiControllerState);
        stage.addActor(new MenuView(skin, stage, new MenuViewModel(services, screenNavigator)));
        services.getAudioManager().playMusic(MusicAsset.MENU);
    }

    @Override
    public void hide() {
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
