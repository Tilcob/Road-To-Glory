package com.github.tilcob.game.yarn;

import com.badlogic.gdx.utils.Array;

public final class IfStack {
    private final Array<Frame> stack = new Array<>();

    public boolean isExecuting() {
        return stack.size == 0 || stack.peek().executing;
    }

    public void clear() {
        stack.clear();
    }

    public void onIfStart(boolean conditionResult) {
        boolean parentExec = stack.size == 0 || stack.peek().executing;
        boolean ifTaken = parentExec && conditionResult;
        stack.add(new Frame(parentExec, ifTaken, ifTaken, false));
    }

    /** Called when encountering <<else>> */
    public void onElse() {
        if (stack.size == 0) return;

        Frame f = stack.pop();
        if (f.elseUsed) {
            stack.add(f);
            return;
        }

        boolean elseExec = f.parentExecuting && !f.branchTaken;
        boolean newBranchTaken = f.branchTaken || elseExec;

        stack.add(new Frame(f.parentExecuting, newBranchTaken, elseExec, true));
    }

    /** Called when encountering <<endif>> */
    public void onEndIf() {
        if (stack.size > 0) stack.pop();
    }

    @Deprecated
    public void push(boolean condResult) {
        onIfStart(condResult);
    }

    @Deprecated
    public void elseBlock() {
        onElse();
    }

    @Deprecated
    public void pop() {
        onEndIf();
    }

    private record Frame(boolean parentExecuting,
                         boolean branchTaken,
                         boolean executing,
                         boolean elseUsed) {
    }
}

