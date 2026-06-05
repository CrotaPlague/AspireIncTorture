package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

public final class BlockDisguiseAppearance implements DisguiseAppearance {
    private final BlockData blockData;

    public BlockDisguiseAppearance(BlockData blockData) {
        this.blockData = Objects.requireNonNull(blockData, "blockData").clone();
    }

    public BlockData blockData() {
        return blockData.clone();
    }

    public String serializedBlockData() {
        return blockData.getAsString();
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("disguises", "block");
    }
}
