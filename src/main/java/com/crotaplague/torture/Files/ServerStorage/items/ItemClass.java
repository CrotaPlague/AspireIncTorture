package com.crotaplague.torture.Files.ServerStorage.items;



import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public abstract class ItemClass implements ConfigurationSerializable, Cloneable{
    public enum TItemType{
        HEALING, MAIN_ITEMS, SHULKERSHELLS, HOLDING_ITEMS, MOVE_ITEMS, GENERAL_ITEMS; //Move Items grant a Mob a specific move / contain it
        public static String menuNames(int num){
            switch(num){
                case 0:
                    return "Healing Items";
                case 1:
                    return "Main Items";
                case 2:
                    return "Shulkers";
                case 3:
                    return "Holding Items";
                case 4:
                    return "Move Items";
                case 5:
                    return "General items";
            }
            return null;
        }
        public static TItemType fromInt(int num){
            switch(num){
                case 0:
                    return HEALING;
                case 1:
                    return MAIN_ITEMS;
                case 2:
                    return SHULKERSHELLS;
                case 3:
                    return HOLDING_ITEMS;
                case 4:
                    return MOVE_ITEMS;
                case 5:
                    return GENERAL_ITEMS;
            }
            return null;
        }
    }
    protected TItemType type = null;
    protected int amount = 1;
    protected ItemStack displayItem = null;
    protected String displayName = null;
    protected int itemDexNum = -1;
    protected mobEnums target = null;
    protected ItemClass(){this.type = null; this.amount = 0; this.displayItem = new ItemStack(Material.AIR); displayName = "unset";}
    protected ItemClass(int itemDexNum){this.itemDexNum = itemDexNum;}
    public ItemClass(TItemType type){
        this.type = type;
    }
    public ItemClass(TItemType type, int amount, int itemDexNum){this.type = type; this.itemDexNum = itemDexNum; this.amount = amount; if(TItemDex.getItem(itemDexNum) != null)setDisplayItem(TItemDex.getItem(itemDexNum).getDisplayItem()); else
        Bukkit.getLogger().log(Level.WARNING, "[Torture] " + itemDexNum + " has no item");}
    public ItemClass(TItemType type, int amount, int itemDexNum, ItemStack displayItem, String name){this.type = type; this.itemDexNum = itemDexNum; this.amount = amount; this.displayItem = displayItem; this.displayName = name;}
    public ItemClass(TItemType type, ItemStack displayItem){this.type = type; this.displayItem = displayItem;}
    public ItemClass(TItemType type, int amount){
        this.type = type;
        this.amount = amount;
    }
    public ItemClass(TItemType type, ItemStack displayItem, int amount){
        this.type = type;
        this.amount = amount;
        this.displayItem = displayItem;
    }

    /**
     * Constructs a new {@code ItemClass} as a copy of the specified item.
     * @param item the item to copy
     */
    public ItemClass(ItemClass item){
        this.type = item.getType();
        this.amount = item.getAmount();
        this.displayItem = item.displayItem;
        this.displayName = item.displayName;
        this.itemDexNum = item.getItemDexNum();
    }

    /**
     * Sets the quantity of this item.
     * @param newAmount the new quantity of this item
     * @return this item, after its quantity has been updated
     */
    public ItemClass setAmount(int newAmount){this.amount = newAmount; return this;}

    /***
     * Returns the display representation of this item.
     * @return the {@link ItemStack} representing this item
     */
    public ItemStack getDisplayItem(){return this.displayItem;}
    public void setDisplayItem(ItemStack item){this.displayItem = item;}
    public void setDisplayName(String name){this.displayName = name;}
    public String getDisplayName(){return this.displayName;}
    public void setItemDexNum(int num){this.itemDexNum = num;}
    public TItemType getType(){return this.type;}
    /**
     * Returns the dex number of this item.
     * @return the dex number of this item
     */
    public int getItemDexNum() {return itemDexNum;}
    /***
     * Returns the quantity of this item.
     * @return the quantity of this item.
     */
    public int getAmount(){return this.amount;}
    public mobEnums getTarget(){return this.target;}
    public void setTarget(mobEnums target){this.target = target;}
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if(this.itemDexNum != -1){
            map.put("itemDexNum", this.itemDexNum);
            map.put("amount", this.amount);
        }
        return map;
    }
    @SuppressWarnings("unchecked")
    public static ItemClass deserialize(Map<String, Object> map){
        ItemClass item1;
        if(map.containsKey("itemDexNum")){
            item1 = TItemDex.getItem((Integer) map.get("itemDexNum"));
            item1.setAmount((int) map.get("amount"));
            return item1;
        }
        return null;
    }
    @Override
    public ItemClass clone()
    {
        switch (this.type) {
            case HEALING: return ((HealItem) this).clone();
            case SHULKERSHELLS: return ((ShulkerItem) this).clone();
            case MAIN_ITEMS: return ((MainItem) this).clone();
            case HOLDING_ITEMS: return ((HoldingItems) this).clone();
        }
        return null;  /// shouldn't ever run
    }

}

