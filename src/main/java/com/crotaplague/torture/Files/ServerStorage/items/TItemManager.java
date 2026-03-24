package com.crotaplague.torture.Files.ServerStorage.items;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TItemManager {
    public static HealItem potion;
    public static ShulkerItem defaultCatchDefault;
    public static ItemClass smoothStone;
    public static ItemClass testShulk;
    public static List<ItemClass> allItems = new ArrayList<>();
    public TItemManager() {
        potion = createPotion();
        defaultCatchDefault = createBasicShulker();
        smoothStone = createSmoothStone();
        testShulk = createBasicShulk();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(field.getType() == ItemClass.class){
                try{
                    if(java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                        allItems.add((ItemClass) field.get(this));
                    }
                }catch(IllegalAccessException exception){
                    exception.printStackTrace();
                }

            }
            Bukkit.getLogger().log(Level.FINE, "running");
        }
    }
    private HealItem createPotion(){
        HealItem item;
        item = new HealItem(1, 20, 0, new ItemStack(Material.BLAZE_POWDER), "Default Healer");
        return item;
    }
    private ShulkerItem createBasicShulker(){
        ShulkerItem item;
        item = new ShulkerItem(1, 1, 10, new ItemStack(Material.SHULKER_BOX), "Basic Shulker"); // 10 catch power
        return item;
    }
    private ItemClass createSmoothStone(){
        HoldingItems item;
        item = new HoldingItems(ItemClass.TItemType.HOLDING_ITEMS, 1, 2, new ItemStack(Material.SMOOTH_STONE), "Smooth Stone");
        return item;
    }
    public static List<ItemClass> getAllItems(){
        List<ItemClass> list = new ArrayList<>(allItems);
        for(int i = 0; i < list.size(); i++){
            ItemClass ite = (ItemClass) list.get(i);
            ItemClass ite2 = ite.clone();;
            list.remove(ite);
            list.add(ite2);
        }
        list = list.stream().sorted(Comparator.comparingInt(ItemClass::getItemDexNum)).collect(Collectors.toList());
        return list;
    }
    private ItemClass createBasicShulk(){
        ShulkerItem item;
        item = new ShulkerItem(1, 15, 30, new ItemStack(Material.BLUE_SHULKER_BOX), "Example Shulker"); // catch power 30
        return item;
    }
}













