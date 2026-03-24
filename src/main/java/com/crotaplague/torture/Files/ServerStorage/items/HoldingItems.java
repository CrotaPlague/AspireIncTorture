package com.crotaplague.torture.Files.ServerStorage.items;

import org.bukkit.inventory.ItemStack;

public class HoldingItems extends Sellable implements Cloneable{
    public HoldingItems(TItemType type, int amount, int dexNum, ItemStack displayItem, String name) {
        super(type, amount, dexNum, displayItem, name);
    }
    public HoldingItems(HoldingItems hold) {
        super(hold);
    }

    @Override
    public HoldingItems clone() {
        return new HoldingItems(this);
    }
}
