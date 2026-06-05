package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BlockDisguiseAppearanceSerializer implements DisguiseAppearanceSerializer<BlockDisguiseAppearance> {
    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("disguises", "block");
    }

    @Override
    public Map<String, Object> serialize(BlockDisguiseAppearance appearance) {
        Map<String, Object> data = new HashMap<>();
        data.put("blockdata", appearance.serializedBlockData());
        return data;
    }

    @Override
    public BlockDisguiseAppearance deserialize(Map<String, Object> data) {
        String serialized = Objects.toString(data.get("blockdata"), null);
        if (serialized == null) {
            throw new IllegalArgumentException("Missing blockdata");
        }
        return new BlockDisguiseAppearance(Bukkit.getServer().createBlockData(serialized));
    }
}
