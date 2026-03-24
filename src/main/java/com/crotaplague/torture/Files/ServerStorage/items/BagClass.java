package com.crotaplague.torture.Files.ServerStorage.items;




import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BagClass implements ConfigurationSerializable {

    List<MainItem> MainItems = new ArrayList<>();
    List<HealItem> HealingItems = new ArrayList<>();
    List<ShulkerItem> ShulkerShells = new ArrayList<>();
    List<HoldingItems> HoldingItems = new ArrayList<>();
    List<ItemClass> MoveItems = new ArrayList<>();
    List<ItemClass> GeneralItems = new ArrayList<>();

    public BagClass(){
        MainItems = new ArrayList<>();
        HealingItems = new ArrayList<>();
        ShulkerShells = new ArrayList<>();
        HoldingItems = new ArrayList<>();
        MoveItems = new ArrayList<>();
        GeneralItems = new ArrayList<>();
    }

    public void setMainItems(List<MainItem> list){this.MainItems = list;}
    public void setHoldingItems(List<HoldingItems> list){this.HoldingItems = list;}
    public void setHealingItems(List<HealItem> list){this.HealingItems = list;}
    public void setShulkerShells(List<ShulkerItem> list){this.ShulkerShells = list;}
    public List<ShulkerItem> getShulkerShells(){return this.ShulkerShells;}
    public void setMoveItems(List<ItemClass> list){this.MoveItems = list;}
    public void addMainItem(MainItem item){
        if(item.getType() == ItemClass.TItemType.MAIN_ITEMS){
            if(MainItems == null){
                MainItems = new ArrayList<>();
            }
            List<MainItem> list = MainItems.stream().filter(itemClass -> {if(item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(list.size() > 0){
                MainItem ite = list.get(0);
                MainItems.remove(ite);
                ite.setAmount(ite.getAmount() + item.getAmount());
                this.MainItems.add(ite);
            }else{
                this.MainItems.add(item);
            }

        }else{
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Non-main item attempted to be added to main items.");
        }
    }
    public void addHealingItem(HealItem item){
        if(item.getType() == ItemClass.TItemType.HEALING){
            if(HealingItems == null){
                HealingItems = new ArrayList<>();
            }
            List<HealItem> list = HealingItems.stream().filter(itemClass -> {if(item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(list.size() > 0){
                HealItem ite = list.get(0);
                HealingItems.remove(ite);
                ite.setAmount(ite.getAmount() + item.getAmount());
                this.HealingItems.add(ite);
            }else{
                this.HealingItems.add(item);
            }
        }else{
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Non-healing item attempted to be added to healing items.");
        }
    }
    public void addShulkerItem(ShulkerItem item){
        if(item.getType() == ItemClass.TItemType.SHULKERSHELLS){
            if(ShulkerShells == null){
                ShulkerShells = new ArrayList<>();
            }
            List<ShulkerItem> list = ShulkerShells.stream().filter(itemClass -> {if(item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(list.size() > 0){
                ShulkerItem ite = list.get(0);
                ShulkerShells.remove(ite);
                Bukkit.getPlayer("CrotaPlague").sendMessage(ite.getAmount() + " " + item.getAmount());
                ite.setAmount(ite.getAmount() + item.getAmount());
                Bukkit.getPlayer("CrotaPlague").sendMessage(item.getAmount() + "");

                this.ShulkerShells.add(ite);
            }else{
                this.ShulkerShells.add(item);
            }
        }else{
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Non-shulker item attempted to be added to shulker shells.");
        }
    }
    public void addHoldingItem(HoldingItems item){
        if(item.getType() == ItemClass.TItemType.HOLDING_ITEMS){
            if(HoldingItems == null){
                HoldingItems = new ArrayList<>();
            }
            HoldingItems.removeIf(Objects::isNull);
            List<HoldingItems> list = HoldingItems.stream().filter(itemClass -> {if(itemClass != null && item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(!list.isEmpty()){
                HoldingItems ite = list.get(0);
                HoldingItems.remove(ite);
                ite.setAmount(ite.getAmount() + item.getAmount());
                this.HoldingItems.add(ite);
            }else{
                this.HoldingItems.add(item);
            }
        }else{
            Torture.plugin.getComponentLogger().debug(Marker.ANY_NON_NULL_MARKER, Component.text("Non-holding item attempted to be added to holding items.", NamedTextColor.RED));
        }
    }
    public void addMoveItem(ItemClass item){
        if(item.getType() == ItemClass.TItemType.MOVE_ITEMS){
            List<ItemClass> list = MoveItems.stream().filter(itemClass -> {if(item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(list.size() > 0){
                ItemClass ite = list.get(0);
                MoveItems.remove(ite);
                ite.setAmount(ite.getAmount() + item.getAmount());
                this.MoveItems.add(ite);
            }else{
                this.MoveItems.add(item);
            }
        }else{
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Non-move item attempted to be added to move items.");
        }
    }
    public void addGeneralItem(ItemClass item){
        if(item.getType() == ItemClass.TItemType.GENERAL_ITEMS){
            List<ItemClass> list = GeneralItems.stream().filter(itemClass -> {if(item.getItemDexNum() == itemClass.getItemDexNum()){return true;} return false;}).collect(Collectors.toList());
            if(list.size() > 0){
                ItemClass ite = list.get(0);
                GeneralItems.remove(ite);
                ite.setAmount(ite.getAmount() + item.getAmount());
                this.GeneralItems.add(ite);
            }else{
                this.GeneralItems.add(item);
            }
        }else{
            Bukkit.getLogger().log(Level.WARNING, "Non-general item attempted to be added to general items. Real type: " + item.getType().toString());
        }
    }
    public void addItem(ItemClass item){ //Default unidentified items ready for sorting
        switch(item.getType()){
            case GENERAL_ITEMS:
                addGeneralItem(item);
                break;
            case HEALING:
                addHealingItem((HealItem) item);
                break;
            case MAIN_ITEMS:
                addMainItem((MainItem) item);
                break;
            case SHULKERSHELLS:
                addShulkerItem((ShulkerItem) item);
                break;
            case HOLDING_ITEMS:
                addHoldingItem((HoldingItems) item);
                break;
            case MOVE_ITEMS:
                addMoveItem(item);
                break;
            default:
                Bukkit.getLogger().log(Level.SEVERE, "Item not given type and attempted to be added to player bag!!! " + item.getType() + item.getDisplayName());
                break;
        }
    }
    public List<ItemClass> getNumList(int num){
        switch(num){
            case 0:
                return new ArrayList<>(HealingItems);
            case 1:
                return new ArrayList<>(MainItems);
            case 2:
                return new ArrayList<>(ShulkerShells);
            case 3:
                return new ArrayList<>(HoldingItems);
            case 4:
                return MoveItems;
            case 5:
                return GeneralItems;
        }
        return new ArrayList<>();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("MainItems", MainItems);
        result.put("HealingItems", HealingItems);
        result.put("ShulkerShells", ShulkerShells);
        result.put("HoldingItems", HoldingItems);
        result.put("MoveItems", MoveItems);
        return result;
    }
    @SuppressWarnings("unchecked")
    public static BagClass deserialize(MemorySection memorySection){
        BagClass bag = new BagClass();
        Set<String> set = memorySection.getKeys(false);
        for(String path : set){
            String rawPath = path;
            path = memorySection.getCurrentPath() + "." + path;

            if(rawPath.equals("HealingItems")){
                List<HealItem> itemList = new ArrayList<>();
                itemList = (List<HealItem>) Torture.data.getConfig().getList(path);
                bag.setHealingItems(itemList);
            }
            if(rawPath.equals("MainItems")){
                List<MainItem> itemList = new ArrayList<>();
                itemList = (List<MainItem>) Torture.data.getConfig().getList(path);
                bag.setMainItems(itemList);
            }
            if(rawPath.equals("ShulkerShells")){
                List<ShulkerItem> itemList = new ArrayList<>();
                itemList = (List<ShulkerItem>) Torture.data.getConfig().getList(path);
                bag.setShulkerShells(itemList);
            }
            if(rawPath.equals("HoldingItems")){
                List<HoldingItems> itemList = new ArrayList<>();
                itemList = (List<HoldingItems>) Torture.data.getConfig().getList(path);
                bag.setHoldingItems(itemList);
            }
            if(rawPath.equals("MoveItems")){
                List<ItemClass> itemList = new ArrayList<>();
                itemList = (List<ItemClass>) Torture.data.getConfig().getList(path);
                bag.setMoveItems(itemList);
            }
        }


        return bag;
    }

}
