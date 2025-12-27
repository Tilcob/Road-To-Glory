package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.tilcob.game.input.UiEvent;
import com.github.tilcob.game.ui.model.ViewModel;

public abstract class View<T extends ViewModel> extends Table implements EventListener {
    protected final Stage stage;
    protected final Skin skin;
    protected final T viewModel;

    public View(Skin skin, Stage stage, T viewModel) {
        super(skin);
        this.skin = skin;
        this.stage = stage;
        this.viewModel = viewModel;
        this.stage.addListener(this);
        setupUI();
    }

    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
            viewModel.clearPropertyChanges();
        } else {
            setupPropertyChanges();
        }
    }

    protected abstract void setupUI();

    protected void setupPropertyChanges() {
    }

    public void onLeft() {

    }

    public void onRight() {

    }

    public  void onUp() {

    }

    public void onDown() {

    }

    public void onSelect() {

    }

    public static void onClick(Actor actor, OnEventConsumer consumer) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                consumer.onEvent();
            }
        });
    }

    public static <T extends Actor> void onEnter(T actor, OnActorEvent<T> consumer) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                consumer.onEvent(actor);
            }
        });
    }

    public static <T extends Actor> void onChange(T actor, OnActorEvent<T> consumer) {
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor eventActor) {
                consumer.onEvent(actor);
            }
        });
    }

    @Override
    public boolean handle(Event event) {
        if (event instanceof UiEvent uiEvent) {
            switch (uiEvent.getCommand()) {
                case LEFT ->  onLeft();
                case RIGHT ->  onRight();
                case UP -> onUp();
                case DOWN -> onDown();
                case SELECT -> onSelect();
            }
            return true;
        }

        return false;
    }

    @FunctionalInterface
    public interface OnEventConsumer {
        void onEvent();
    }

    @FunctionalInterface
    public interface OnActorEvent<T extends Actor> {
        void onEvent(T actor);
    }
}
