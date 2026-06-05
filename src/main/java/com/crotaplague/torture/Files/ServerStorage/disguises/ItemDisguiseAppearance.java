package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class ItemDisguiseAppearance implements DisguiseAppearance {
    private final ItemStack itemStack;

    public ItemDisguiseAppearance(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack").clone();
    }

    public ItemStack itemStack() {
        return itemStack.clone();
    }

    @Override
    public NamespacedKey typeKey() {
        return new NamespacedKey("disguises", "item");
    }
}