package com.crotaplague.torture.Files.ServerStorage.AnimationParts;

import net.kyori.adventure.key.Key;

public enum CustomSound {
    TEST("aspireinc:test"), OTHER("aspireinc:other");

    private final String key;
    private final Key adventureKey;

    CustomSound(String key) {
        this.key = key;
        this.adventureKey = Key.key(key);
    }

    public String getKey() {
        return key;
    }

    public Key getAdventureKey() {
        return adventureKey;
    }

    public static CustomSound fromString(String input) {
        for (CustomSound sound : values()) {
            if (sound.key.equalsIgnoreCase(input)) {
                return sound;
            }
        }
        return null;
    }
}