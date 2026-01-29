package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.flow.*;

import java.util.*;

public class DialogYarnRuntime extends BaseYarnRuntime {

    public DialogYarnRuntime(YarnRuntime runtime,
                             CommandRegistry commandRegistry,
                             FlowExecutor flowExecutor,
                             FunctionRegistry functionRegistry) {
        super(runtime, commandRegistry, flowExecutor, functionRegistry);
    }

    public boolean evaluateCondition(Entity player, String condition, CommandCall.SourcePos source) {
        return super.evaluateCondition(player, condition, source);
    }
}
