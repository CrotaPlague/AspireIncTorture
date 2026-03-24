package com.crotaplague.torture.Files.ServerStorage.items;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Sellable extends ItemClass implements ConfigurationSerializable{

    protected int sellPrice = 0;
    protected int buyPrice = 0;

    public Sellable(){
        super();
        this.sellPrice = -1; this.buyPrice = -1;
    }

    public Sellable(TItemType type, int amount, int dexNum, ItemStack displayItem, String name) {
        super(type, amount, dexNum, displayItem, name);
    }
    public Sellable(TItemType type, int amount, int dexNum, ItemStack displayItem, String name, int buyPrice, int sellPrice) {
        super(type, amount, dexNum, displayItem, name);
        this.buyPrice = buyPrice; this.sellPrice = sellPrice;
    }
    public Sellable(TItemType type, int amount, int dexNum){
        super(type, amount, dexNum);
    }
    public Sellable(Sellable s){
        super(s);
        this.buyPrice = s.buyPrice; this.sellPrice = s.sellPrice;
    }

    public void setSellPrice(int sellPrice){this.sellPrice = sellPrice;}
    public void setBuyPrice(int buyPrice){this.buyPrice = buyPrice;}
    public int getBuyPrice(){return this.buyPrice;}
    public int getSellPrice(){return this.sellPrice;}

    @SuppressWarnings("unchecked")
    public static Sellable deserialize(Map<String, Object> map){
        ItemClass item = ItemClass.deserialize(map);
        if(item == null) return null;
        ItemClass tItem = TItemDex.getItem(item.itemDexNum);
        Sellable sell = new Sellable();
        sell.setDisplayName(tItem.displayName);
        sell.setDisplayItem(new ItemStack(tItem.displayItem));
        sell.setSellPrice(((Sellable) tItem).getSellPrice());
        sell.setBuyPrice(((Sellable) tItem).getBuyPrice());
        return sell;
    }
}
