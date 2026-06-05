package com.crotaplague.torture.Files.ServerStorage.disguises;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.crotaplague.torture.Torture.plugin;

/**
 * Factory and registration helper.
 */
public final class Disguises {

    private Disguises() {
    }

    /**
     * Registers all disguise serializables.
     * Call this once during plugin startup.
     */
    public static void registerSerializables() {
        ConfigurationSerialization.registerClass(PlayerDisguise.class);
        ConfigurationSerialization.registerClass(BlockDisguise.class);
        ConfigurationSerialization.registerClass(ItemDisguise.class);
    }

    /**
     * Creates a player disguise from a username.
     *
     * @param username player name
     * @return player disguise
     */
    public static PlayerDisguise player(String username) {
        return new PlayerDisguise(username);
    }

    /**
     * Creates a player disguise from a UUID and name.
     *
     * @param uuid player UUID
     * @param username player name
     * @return player disguise
     */
    public static PlayerDisguise player(UUID uuid, String username) {
        return new PlayerDisguise(uuid, username);
    }

    /**
     * Creates a player disguise from an existing profile.
     *
     * @param profile player profile
     * @return player disguise
     */
    public static PlayerDisguise player(PlayerProfile profile) {
        return new PlayerDisguise(profile);
    }

    /**
     * Creates a block disguise from block data.
     *
     * @param blockData block data
     * @return block disguise
     */
    public static BlockDisguise block(BlockData blockData) {
        return new BlockDisguise(blockData);
    }

    /**
     * Creates a block disguise from a material.
     *
     * @param material material
     * @return block disguise
     */
    public static BlockDisguise block(org.bukkit.Material material) {
        return new BlockDisguise(Bukkit.createBlockData(material));
    }

    /**
     * Creates an item disguise from an ItemStack.
     *
     * @param itemStack item stack
     * @return item disguise
     */
    public static ItemDisguise item(ItemStack itemStack) {
        return new ItemDisguise(itemStack);
    }

    // Disguises.java
    public static PlayerDisguise player(String username, Entity target) {
        PlayerDisguise disguise = new PlayerDisguise(username);
        disguise.resolveAsync().thenAccept(resolved ->
                Bukkit.getScheduler().runTask(plugin, () ->
                        resolved.apply(target).showDisguiseOnly()
                )
        );
        return disguise;
    }

    public static CompletableFuture<PlayerDisguise> playerAsync(String username) {
        return new PlayerDisguise(username).resolveAsync();
    }

    public static CompletableFuture<DisguiseSession> playerWithSession(String username, Entity target) {
        return playerAsync(username).thenApplyAsync(disguise ->
                        disguise.apply(target),
                runnable -> Bukkit.getScheduler().runTask(plugin, runnable)
        );
    }

}
