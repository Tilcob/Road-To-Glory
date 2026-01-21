package com.github.tilcob.game.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.DialogSession;

public class DialogNavigator {
    private final DialogData dialogData;
    private final DialogSession session;
    private final boolean repeatableChoices;
    private Array<String> lines;
    private Array<DialogChoice> choices;

    public DialogNavigator(DialogData dialogData, DialogSession session, DialogSelection selection, boolean repeatableChoices) {
        this.dialogData = dialogData;
        this.session = session;
        this.repeatableChoices = repeatableChoices;
        this.lines = selection == null || selection.lines() == null ? new Array<>() : selection.lines();
        this.choices = selection == null || selection.choices() == null ? new Array<>() : selection.choices();
    }

    public DialogLine toDialogLine() {
        return new DialogLine(currentLine(), session.getLineIndex() + 1, getTotal());
    }

    public String currentLine() {
        if (lines.isEmpty()) {
            return "";
        }
        return lines.get(session.getLineIndex());
    }

    public int getTotal() {
        return lines.size;
    }

    public boolean hasLines() {
        return !lines.isEmpty();
    }

    public boolean advance() {
        session.setLineIndex(session.getLineIndex() + 1);
        return session.getLineIndex() < lines.size;
    }

    public boolean hasChoices() {
        return choices != null && choices.size > 0;
    }

    public boolean hasRemainingChoices() {
        return !session.isChoiceConsumed() && hasChoices();
    }

    public void beginChoice() {
        if (!hasChoices() || session.isChoiceConsumed()) {
            return;
        }
        session.setAwaitingChoice(true);
        session.setChoiceIndex(0);
    }

    public void moveChoice(int delta) {
        if (!session.isAwaitingChoice() || choices.isEmpty()) {
            return;
        }
        session.setChoiceIndex(MathUtils.clamp(session.getChoiceIndex() + delta, 0, choices.size - 1));
    }

    public DialogChoice selectChoice() {
        if (!session.isAwaitingChoice() || choices.isEmpty()) {
            return null;
        }
        DialogChoice choice = choices.get(session.getChoiceIndex());
        session.setAwaitingChoice(false);
        session.setChoiceConsumed(true);
        return choice;
    }

    public Array<DialogChoice> getChoices() {
        return choices;
    }

    public void applyChoice(DialogChoice choice) {
        if (choice == null) {
            return;
        }
        if (switchToNode(choice.next())) {
            hasLines();
            return;
        }
        setLines(choice.lines());
        hasLines();
    }

    private boolean switchToNode(String nodeId) {
        if (nodeId == null) {
            return false;
        }
        ObjectMap<String, DialogNode> nodes = dialogData.nodesById();
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        DialogNode node = nodes.get(nodeId);
        if (node == null) {
            if (Gdx.app != null) {
                Gdx.app.error("DialogNavigator", "Dialog node not found: " + nodeId);
            }
            return false;
        }
        resetForLines(node.lines(), node.choices(), nodeId, false);
        return true;
    }

    private void setLines(Array<String> lines) {
        Array<DialogChoice> nextChoices = repeatableChoices ? choices : new Array<>();
        boolean nextChoiceConsumed = !repeatableChoices;
        resetForLines(lines, nextChoices, null, nextChoiceConsumed);
    }

    private void resetForLines(Array<String> lines, Array<DialogChoice> choices, String nodeId, boolean choiceConsumed) {
        this.lines = lines == null ? new Array<>() : lines;
        this.choices = choices == null ? new Array<>() : choices;
        session.setCurrentNodeId(nodeId);
        session.setLineIndex(0);
        session.setChoiceIndex(0);
        session.setAwaitingChoice(false);
        session.setChoiceConsumed(choiceConsumed);
    }
}
