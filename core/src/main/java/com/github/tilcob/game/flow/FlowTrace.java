package com.github.tilcob.game.flow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class FlowTrace {
    private final Deque<FlowAction> ring;
    private final int capacity;

    public FlowTrace(int capacity) {
        this.capacity = Math.max(10, capacity);
        this.ring = new ArrayDeque<>(this.capacity);
    }

    public void record(FlowAction action) {
        if (ring.size() >= capacity) ring.removeFirst();
        ring.addLast(action);
    }

    public List<FlowAction> snapshot() {
        return new ArrayList<>(ring);
    }

    public void clear() {
        ring.clear();
    }
}
