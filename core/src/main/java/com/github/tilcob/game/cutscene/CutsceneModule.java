package com.github.tilcob.game.cutscene;

import com.github.tilcob.game.config.ContentPaths;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;
import com.github.tilcob.game.yarn.YarnRuntime;

public final class CutsceneModule {
    private CutsceneModule() {
    }

    public static CutsceneServices create(YarnRuntime runtime, FlowBootstrap flowBootstrap) {
        CutsceneRepository cutsceneRepository = new CutsceneRepository(true, ContentPaths.CUTSCENES_DIR);
        CutsceneYarnRuntime cutsceneYarnRuntime = new CutsceneYarnRuntime(
            runtime,
            flowBootstrap.commands(),
            flowBootstrap.executor());
        return new CutsceneServices(cutsceneRepository, cutsceneYarnRuntime);
    }

    public record CutsceneServices(CutsceneRepository cutsceneRepository, CutsceneYarnRuntime cutsceneYarnRuntime) {
    }
}
