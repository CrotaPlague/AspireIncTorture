package com.crotaplague.torture.Files.ServerStorage.items;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class MainItem extends ItemClass implements ConfigurationSerializable, Cloneable{
    protected MainItem(){}
    public MainItem(int dexNum){
        super(dexNum);
        ItemClass item = TItemDex.getItem(dexNum);
        if(item instanceof MainItem){
            MainItem main = (MainItem) item;
            this.displayName = main.displayName;
            this.amount = 1;
            this.displayItem = new ItemStack(main.displayItem);
            this.type = main.type;
        }
    }
    public MainItem(MainItem main){
        this(main.itemDexNum);
    }

    @Override
    public MainItem clone(){
        return new MainItem(this);
    }

}
