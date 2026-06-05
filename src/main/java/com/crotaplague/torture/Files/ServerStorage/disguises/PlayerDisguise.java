package com.crotaplague.torture.Files.ServerStorage.disguises;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SerializableAs("TorturePlayerDisguise")
public final class PlayerDisguise extends Disguise {

    private final PlayerProfile profile;

    public PlayerDisguise(String username) {
        this(Bukkit.createProfile(username));
        this.profile.update().thenAccept(resolved -> {
            resolved.getProperties().forEach(this.profile::setProperty);
        });
    }

    public PlayerDisguise(UUID uuid, String username) {
        this(Bukkit.createProfile(uuid, username));
    }

    public PlayerDisguise(PlayerProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile").clone();
    }

    /**
     * Gets a copy of the stored profile.
     *
     * @return player profile
     */
    public PlayerProfile profile() {
        return profile.clone();
    }

    /**
     * Convenience accessor for the username, if present.
     *
     * @return name or null
     */
    public String username() {
        return profile.getName();
    }

    /**
     * Resolves the profile asynchronously.
     *
     * @return future that completes with an updated disguise
     */
    public CompletableFuture<PlayerDisguise> resolveAsync() {
        return profile.update().thenApply(PlayerDisguise::new);
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("torture", "player");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("profile", profile.clone());
        return map;
    }

    public static PlayerDisguise deserialize(Map<String, Object> map) {
        Object raw = map.get("profile");
        if (raw instanceof PlayerProfile profile) {
            return new PlayerDisguise(profile);
        }

        String username = (String) map.get("username");
        if (username != null) {
            return new PlayerDisguise(username);
        }

        throw new IllegalArgumentException("Missing profile or username");
    }
}
