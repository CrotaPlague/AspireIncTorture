package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class ItemDisguiseAppearanceSerializer implements DisguiseAppearanceSerializer<ItemDisguiseAppearance> {
    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("disguises", "item");
    }

    @Override
    public Map<String, Object> serialize(ItemDisguiseAppearance appearance) {
        Map<String, Object> data = new HashMap<>();
        data.put("item", appearance.itemStack());
        return data;
    }

    @Override
    public ItemDisguiseAppearance deserialize(Map<String, Object> data) {
        ItemStack item = (ItemStack) data.get("item");
        if (item == null) {
            throw new IllegalArgumentException("Missing item");
        }
        return new ItemDisguiseAppearance(item);
    }
}
