package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.dialog.DialogLine;
import com.github.tilcob.game.ui.model.DialogChoiceDisplay;
import com.github.tilcob.game.ui.model.DialogDisplay;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.RewardDisplay;
import com.github.tilcob.game.yarn.script.ScriptEvent;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.Map;

public class GameView extends View<GameViewModel> {
    private final HorizontalGroup lifeGroup;
    private final Table dialogContainer;
    private final TypingLabel dialogText;
    private final TextraLabel speakerLabel;
    private final TextraLabel dialogProgressLabel;
    private VerticalGroup dialogChoices;
    private TextraLabel dialogHintLabel;
    private final Table rewardContainer;
    private final TextraLabel rewardTitleLabel;
    private final TextraLabel rewardGoldLabel;
    private final VerticalGroup rewardItems;
    private final TextraLabel rewardHintLabel;

    public GameView(Skin skin, Stage stage, GameViewModel viewModel) {
        super(skin, stage, viewModel);
        this.lifeGroup = findActor("lifeGroup");
        this.dialogContainer = findActor("dialogContainer");
        this.dialogText = findActor("dialogText");
        this.speakerLabel = findActor("dialogSpeaker");
        this.dialogProgressLabel = findActor("dialogProgress");
        this.dialogChoices = findActor("dialogChoices");
        this.dialogHintLabel = findActor("dialogHint");
        this.rewardContainer = findActor("rewardContainer");
        this.rewardTitleLabel = findActor("rewardTitle");
        this.rewardGoldLabel = findActor("rewardGold");
        this.rewardItems = findActor("rewardItems");
        this.rewardHintLabel = findActor("rewardHint");

        updateLife(viewModel.getLifePoints());
    }

    @Override
    protected void setupUI() {
        align(Align.topLeft);
        setFillParent(true);

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.setName("lifeGroup");
        horizontalGroup.align(Align.topLeft);
        horizontalGroup.padLeft(5.0f);
        horizontalGroup.padTop(5.0f);
        horizontalGroup.space(5.0f);
        add(horizontalGroup).left().top().expandX().padLeft(5.0f).padTop(5.0f);

        Table dialogBox = new Table(skin);
        dialogBox.setName("dialogContainer");
        dialogBox.setBackground(skin.getDrawable("Other_panel_brown"));
        dialogBox.pad(8f, 12f, 8f, 12f);
        dialogBox.setVisible(false);

        Table dialogHeader = new Table(skin);
        dialogHeader.align(Align.left);
        TextraLabel dialogSpeaker = new TextraLabel("", skin, "text_12");
        dialogSpeaker.setName("dialogSpeaker");
        dialogSpeaker.setColor(skin.getColor("BLACK"));
        TextraLabel dialogProgress = new TextraLabel("", skin, "text_10");
        dialogProgress.setName("dialogProgress");
        dialogProgress.setColor(skin.getColor("BLACK"));
        dialogHeader.add(dialogSpeaker).left().expandX();
        dialogHeader.add(dialogProgress).right();

        TypingLabel dialogLabel = new TypingLabel("", skin, "text_10");
        dialogLabel.setName("dialogText");
        dialogLabel.setColor(skin.getColor("BLACK"));
        dialogLabel.setWrap(true);

        VerticalGroup dialogChoiceGroup = new VerticalGroup();
        dialogChoiceGroup.setName("dialogChoices");
        dialogChoiceGroup.align(Align.left);
        dialogChoiceGroup.space(2f);
        dialogChoiceGroup.setVisible(false);
        dialogChoices = dialogChoiceGroup;

        TextraLabel continueLabel = new TextraLabel("Continue with E", skin, "text_08");
        continueLabel.setName("dialogHint");
        continueLabel.setColor(skin.getColor("BLACK"));
        dialogHintLabel = continueLabel;

        dialogBox.add(dialogHeader).expandX().fillX().row();
        dialogBox.add(dialogLabel).expandX().fillX().padTop(4f).row();
        dialogBox.add(dialogChoiceGroup).expandX().fillX().left().padTop(6f).row();
        dialogBox.add(continueLabel).right().padTop(6f);

        Table rewardBox = new Table(skin);
        rewardBox.setName("rewardContainer");
        rewardBox.setBackground(skin.getDrawable("Other_panel_brown"));
        rewardBox.pad(8f, 12f, 8f, 12f);
        rewardBox.setVisible(false);

        TextraLabel rewardTitle = new TextraLabel("Quest Reward", skin, "text_12");
        rewardTitle.setName("rewardTitle");
        rewardTitle.setColor(skin.getColor("BLACK"));

        TextraLabel rewardGold = new TextraLabel("", skin, "text_10");
        rewardGold.setName("rewardGold");
        rewardGold.setColor(skin.getColor("BLACK"));

        VerticalGroup rewardItemsGroup = new VerticalGroup();
        rewardItemsGroup.setName("rewardItems");
        rewardItemsGroup.align(Align.left);
        rewardItemsGroup.space(2f);

        TextraLabel rewardHint = new TextraLabel("Continue with E", skin, "text_08");
        rewardHint.setName("rewardHint");
        rewardHint.setColor(skin.getColor("BLACK"));

        rewardBox.add(rewardTitle).center().row();
        rewardBox.add(rewardGold).center().padTop(4f).row();
        rewardBox.add(rewardItemsGroup).expand().center().padTop(4f).row();
        rewardBox.add(rewardHint).right().padTop(6f).row();

        add().expandY().row();

        add(rewardBox).expand().center().padLeft(10f).padRight(10f).padBottom(8f).row();
        add(dialogBox).expandX().fillX().bottom().padLeft(10f).padRight(10f).padBottom(8f);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.LIFE_POINTS_PC, Integer.class, this::updateLife);
        viewModel.onPropertyChange(Constants.PLAYER_DAMAGE_PC, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(Constants.SHOW_DIALOG, DialogDisplay.class, this::showDialog);
        viewModel.onPropertyChange(Constants.HIDE_DIALOG, Boolean.class, value -> hideDialog());
        viewModel.onPropertyChange(Constants.SHOW_DIALOG_CHOICES, DialogChoiceDisplay.class, this::showChoices);
        viewModel.onPropertyChange(Constants.HIDE_DIALOG_CHOICES, Boolean.class, value -> hideChoices());
        viewModel.onPropertyChange(Constants.SHOW_REWARD_DIALOG, RewardDisplay.class, this::showRewardDialog);
        viewModel.onPropertyChange(Constants.HIDE_REWARD_DIALOG, Boolean.class, value -> hideRewardDialog());
    }

    private void showDialog(DialogDisplay display) {
        dialogContainer.setVisible(true);
        speakerLabel.setText(display.speaker());
        dialogProgressLabel.setText(display.line().index() + "/" + display.line().total());
        setText(display.line(), dialogText);
        if (dialogHintLabel != null) {
            dialogHintLabel.setText("Continue with E");
        }
        dialogText.restart();
    }

    private void setText(DialogLine line, TypingLabel label) {
        String text;
        if (line.text() instanceof ScriptEvent.Text t) {
            text = t.text();
        } else {
            text = String.valueOf(line.text());
        }
        dialogText.setText(text);
    }

    private void hideDialog() {
        dialogContainer.setVisible(false);
        dialogText.setText("");
        dialogProgressLabel.setText("");
        speakerLabel.setText("");
        hideChoices();
    }

    private void showRewardDialog(RewardDisplay display) {
        rewardContainer.setVisible(true);
        rewardTitleLabel.setText(display.title());
        rewardGoldLabel.setText(display.money() > 0 ? "Money: " + display.money() : "");
        rewardItems.clearChildren();
        if (display.items() != null) {
            for (String item : display.items()) {
                TextraLabel itemLabel = new TextraLabel("Item: " + item, skin, "text_10");
                itemLabel.setColor(skin.getColor("BLACK"));
                rewardItems.addActor(itemLabel);
            }
        }
        rewardHintLabel.setText("Continue with E");
    }

    private void hideRewardDialog() {
        rewardContainer.setVisible(false);
        rewardTitleLabel.setText("");
        rewardGoldLabel.setText("");
        rewardItems.clearChildren();
    }

    private void showChoices(DialogChoiceDisplay display) {
        if (dialogChoices == null) {
            return;
        }
        dialogChoices.clearChildren();
        Array<String> choices = display.choices();
        for (int i = 0; i < choices.size; i++) {
            String prefix = i == display.selectedIndex() ? "> " : "";
            TextraLabel choiceLabel = new TextraLabel(prefix + choices.get(i), skin, "text_10");
            choiceLabel.setColor(skin.getColor("BLACK"));
            dialogChoices.addActor(choiceLabel);
        }
        dialogChoices.setVisible(true);
        if (dialogHintLabel != null) {
            dialogHintLabel.setText("Choose mit W/S, accept mit E");
        }
    }

    private void hideChoices() {
        dialogChoices.setVisible(false);
        dialogChoices.clearChildren();
    }

    private void updateLife(int lifePoints) {
        lifeGroup.clear();

        int maxLife = viewModel.getMaxLife();
        while (maxLife > 0) {
            int imgIdx = MathUtils.clamp(lifePoints, 0, 4);
            Image image = new Image(skin, "life_0" + imgIdx);
            lifeGroup.addActor(image);

            maxLife -= 4;
            lifePoints -= 4;
        }
    }

    private void showDamage(Map.Entry<Vector2, Integer> damageAndPosition) {
        new DamageIndicator(damageAndPosition.getKey(), damageAndPosition.getValue()).show();
    }

    private Vector2 toStageCoords(Vector2 gamePosition) {
        Vector2 resultPos = viewModel.toScreenCoords(gamePosition);
        stage.getViewport().unproject(resultPos);
        resultPos.y = stage.getViewport().getWorldHeight() - resultPos.y;
        return resultPos;
    }

    private class DamageIndicator {
        private final Vector2 position;
        private final TextraLabel label;

        DamageIndicator(Vector2 position, int damage) {
            this.position = position;
            this.label = new TypingLabel("[%50]{JUMP=2.0;0.5;0.9}{RED}" + damage, skin);
        }

        void show() {
            stage.addActor(label);
            label.addAction(
                    Actions.parallel(
                            Actions.sequence(Actions.delay(1.25f), Actions.removeActor()),
                            Actions.forever(Actions.run(this::updatePosition))));
        }

        private void updatePosition() {
            Vector2 stageCoords = toStageCoords(position);
            label.setPosition(stageCoords.x, stageCoords.y);
        }
    }
}
