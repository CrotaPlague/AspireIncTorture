package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("TortureBlockDisguise")
public final class BlockDisguise extends Disguise {

    private final BlockData blockData;

    public BlockDisguise(BlockData blockData) {
        this.blockData = Objects.requireNonNull(blockData, "blockData").clone();
    }

    public BlockData blockData() {
        return blockData.clone();
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("torture", "block");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("blockdata", blockData.getAsString());
        return map;
    }

    public static BlockDisguise deserialize(Map<String, Object> map) {
        String data = (String) map.get("blockdata");
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Missing blockdata");
        }
        return new BlockDisguise(Bukkit.createBlockData(data));
    }
}

