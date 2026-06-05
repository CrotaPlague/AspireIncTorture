package com.crotaplague.torture.Files.ServerStorage.disguises;

import java.util.Objects;

/**
 * Simple service locator for the disguise API.
 * Set this once in onEnable().
 */
public final class DisguiseApiHolder {
    private static volatile DisguiseApi api;

    private DisguiseApiHolder() {
    }

    public static void set(DisguiseApi disguiseApi) {
        api = Objects.requireNonNull(disguiseApi, "disguiseApi");
    }

    public static DisguiseApi get() {
        return api;
    }

    public static DisguiseApi require() {
        DisguiseApi current = api;
        if (current == null) {
            throw new IllegalStateException("DisguiseApi has not been set yet");
        }
        return current;
    }
}
