package com.github.tilcob.game.event;

import com.github.tilcob.game.ui.model.ItemModel;

public record DragAndDropEvent(int fromIdx, int toIdx) {
}
