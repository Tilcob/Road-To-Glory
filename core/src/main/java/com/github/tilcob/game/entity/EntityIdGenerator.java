package com.github.tilcob.game.entity;

import java.util.concurrent.atomic.AtomicLong;

public class EntityIdGenerator{
    private static final AtomicLong NEXT = new AtomicLong();

    private EntityIdGenerator() {}

    public static long next() {
        return NEXT.getAndIncrement();
    }
}
