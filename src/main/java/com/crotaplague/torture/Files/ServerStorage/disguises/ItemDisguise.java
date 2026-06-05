package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("TortureItemDisguise")
public final class ItemDisguise extends Disguise {

    private final ItemStack itemStack;

    public ItemDisguise(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack").clone();
    }

    public ItemStack itemStack() {
        return itemStack.clone();
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("torture", "item");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", itemStack.clone());
        return map;
    }

    public static ItemDisguise deserialize(Map<String, Object> map) {
        Object raw = map.get("item");
        if (!(raw instanceof ItemStack item)) {
            throw new IllegalArgumentException("Missing item");
        }
        return new ItemDisguise(item);
    }
}