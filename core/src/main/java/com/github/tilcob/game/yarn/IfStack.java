package com.github.tilcob.game.yarn;

import com.badlogic.gdx.utils.Array;

public final class IfStack {
    private final Array<Frame> stack = new Array<>();

    public boolean isExecuting() {
        return stack.size == 0 || stack.peek().executing;
    }

    public void push(boolean condResult) {
        boolean parentExec = stack.size == 0 || stack.peek().executing;
        stack.add(new Frame(parentExec, condResult, parentExec && condResult, false));
    }

    public void elseBlock() {
        if (stack.size == 0) return;
        Frame f = stack.pop();
        if (f.elseUsed) {
            stack.add(f);
            return;
        }
        boolean exec = f.parentExecuting && !f.conditionResult;
        stack.add(new Frame(f.parentExecuting, f.conditionResult, exec, true));
    }

    public void pop() {
        if (stack.size > 0) stack.pop();
    }

    private record Frame(boolean parentExecuting, boolean conditionResult, boolean executing, boolean elseUsed) {}
}

