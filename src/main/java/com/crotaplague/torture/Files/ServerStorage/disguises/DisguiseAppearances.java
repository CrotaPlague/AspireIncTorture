package com.crotaplague.torture.Files.ServerStorage.disguises;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Convenience factory methods for built-in appearances.
 */
public final class DisguiseAppearances {
    private DisguiseAppearances() {
    }

    public static BlockDisguiseAppearance block(BlockData blockData) {
        return new BlockDisguiseAppearance(blockData);
    }

    public static ItemDisguiseAppearance item(ItemStack itemStack) {
        return new ItemDisguiseAppearance(itemStack);
    }

    public static PlayerDisguiseAppearance player(PlayerProfile profile) {
        return new PlayerDisguiseAppearance(profile);
    }

    public static PlayerDisguiseAppearance player(String name) {
        return new PlayerDisguiseAppearance(Bukkit.getServer().createProfile(name));
    }

    public static PlayerDisguiseAppearance player(UUID uuid, String name) {
        return new PlayerDisguiseAppearance(Bukkit.getServer().createProfile(uuid, name));
    }

    public static CompletableFuture<PlayerDisguiseAppearance> playerResolved(String name) {
        PlayerProfile profile = Bukkit.getServer().createProfile(name);
        return profile.update().thenApply(PlayerDisguiseAppearance::new);
    }
}
