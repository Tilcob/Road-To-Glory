package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.model.PauseViewModel;

public class PauseView extends View<PauseViewModel> {
    private final Image selectionImage;
    private Group selectedItem;

    public PauseView(Skin skin, Stage stage, PauseViewModel viewModel) {
        super(skin, stage, viewModel);
        this.selectionImage = new Image(skin, "selection");
        this.selectionImage.setTouchable(Touchable.disabled);
        this.selectedItem = findActor(PauseOption.RESUME.name());
        selectMenuItem(selectedItem);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        Table contentTable = new Table();
        contentTable.setBackground(skin.getDrawable("frame"));
        contentTable.pad(30f);

        Label title = new Label("Paused", skin, "text_12");
        title.setColor(skin.getColor("sand"));
        contentTable.add(title).padBottom(15f).row();

        Table buttonTable = new Table();
        contentTable.add(buttonTable).row();

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.setName(PauseOption.RESUME.name());
        buttonTable.add(resumeButton).width(180f).row();
        onClick(resumeButton, viewModel::resumeGame);
        onEnter(resumeButton, this::selectMenuItem);

        TextButton quitButton = new TextButton("Quit to Menu", skin);
        quitButton.setName(PauseOption.QUIT.name());
        buttonTable.add(quitButton).width(180f).padTop(10f).row();
        onClick(quitButton, viewModel::quitToMenu);
        onEnter(quitButton, this::selectMenuItem);

        add(contentTable).expand().center();
        align(Align.center);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ON_DOWN, Boolean.class, this::onDown);
        viewModel.onPropertyChange(Constants.ON_UP, Boolean.class, this::onUp);
        viewModel.onPropertyChange(Constants.ON_SELECT, Boolean.class, this::onSelect);
    }

    private void selectMenuItem(Group menuItem) {
        if (selectionImage.getParent() != null) {
            selectionImage.getParent().removeActor(selectionImage);
        }

        float extraSize = 6f;
        float halfExtraSize = extraSize * .5f;
        float resizeTime = .2f;

        selectedItem = menuItem;
        menuItem.addActor(selectionImage);
        selectionImage.setPosition(-halfExtraSize, -halfExtraSize);
        selectionImage.setSize(menuItem.getWidth() + extraSize, menuItem.getHeight() + extraSize);
        selectionImage.clearActions();
        selectionImage.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtraSize, -halfExtraSize, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtraSize, halfExtraSize, resizeTime, Interpolation.linear)
            )
        )));
    }

    private void onDown(Object ignored) {
        Group menuContentTable = selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = (currentIdx + 1) % numOptions;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    private void onUp(Object ignored) {
        Group menuContentTable = selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = currentIdx == 0 ? numOptions - 1 : currentIdx - 1;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    private void onSelect(Object ignored) {
        PauseOption option = PauseOption.valueOf(selectedItem.getName());
        switch (option) {
            case RESUME -> viewModel.resumeGame();
            case QUIT -> viewModel.quitToMenu();
        }
    }

    private enum PauseOption {
        RESUME,
        QUIT
    }
}
