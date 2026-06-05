package com.crotaplague.torture.Files.ServerStorage.disguises;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.NamespacedKey;

import java.util.Objects;

public final class PlayerDisguiseAppearance implements DisguiseAppearance {
    private final PlayerProfile profile;

    public PlayerDisguiseAppearance(PlayerProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile").clone();
    }

    public PlayerProfile profile() {
        return profile.clone();
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("disguises", "player");
    }
}

