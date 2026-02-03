package com.github.tilcob.game.dialog;

import com.github.tilcob.game.config.ContentPaths;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.YarnRuntime;

import java.util.Map;

public final class DialogModule {
    private DialogModule() {
    }

    public static DialogServices create(YarnRuntime runtime, FlowBootstrap flowBootstrap) {
        DialogRepository dialogRepository = new DialogRepository(true, ContentPaths.DIALOGS_DIR,
            Map.of("Shopkeeper", "shopkeeper"));
        DialogYarnRuntime dialogYarnRuntime = new DialogYarnRuntime(
            runtime,
            flowBootstrap.commands(),
            flowBootstrap.executor(),
            flowBootstrap.functions());
        return new DialogServices(dialogRepository, dialogYarnRuntime);
    }

    public record DialogServices(DialogRepository dialogRepository, DialogYarnRuntime dialogYarnRuntime) {
    }
}
