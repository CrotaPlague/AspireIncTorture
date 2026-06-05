package com.crotaplague.torture.Files.ServerStorage.items;

import com.crotaplague.torture.Files.ServerStorage.specialConditions.SpecialConditions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealItem extends Sellable implements ConfigurationSerializable, Cloneable {

    private int healAmount = 0;
    private List<SpecialConditions> healConditions = new ArrayList<>();
    private boolean reviveItem;

    public HealItem(){
        super();
    }

    public HealItem(int amount, int healAmount, int itemDexNum) {
        super(TItemType.HEALING, amount, itemDexNum);
        this.healAmount = healAmount;
        this.reviveItem = false;
    }
    public HealItem(int amount, int healAmount, int itemDexNum, List<SpecialConditions> conditions, boolean reviveItem) {
        super(TItemType.HEALING, amount, itemDexNum);
        this.healAmount = healAmount;
        this.reviveItem = reviveItem;
        this.healConditions = new ArrayList<>();
        this.healConditions.addAll(conditions);
    }
    public HealItem(int amount, int healAmount, int itemDexNum, ItemStack displayItem, String name) {
        super(TItemType.HEALING, amount, itemDexNum, displayItem, name);
        this.healAmount = healAmount;
        this.reviveItem = false;
    }
    public HealItem(Sellable i){
        super(TItemType.HEALING, i.getAmount(), i.getItemDexNum(), i.getDisplayItem(), i.getDisplayName(), i.buyPrice, i.sellPrice);
        if(i instanceof HealItem){
            HealItem aHealItem = (HealItem) i;
            this.reviveItem = aHealItem.reviveItem;
            this.healConditions = new ArrayList<>();
            for(SpecialConditions cond : aHealItem.healConditions){
                healConditions.add(new SpecialConditions(cond.getTurn(), cond.getCondition()));
            }
            this.healAmount = aHealItem.healAmount;
        }

    }
    public HealItem(ItemClass item) {
        super(TItemType.HEALING, item.getAmount(), item.getItemDexNum());
    }

    public void addCondition(SpecialConditions con){healConditions.add(con);}
    public List<SpecialConditions> getConditions(){return this.healConditions;}
    public int getHealAmount() {
        return this.healAmount;
    }
    public boolean isReviveItem(){return this.reviveItem;}
    public void setReviveItem(boolean a){this.reviveItem = a;}
    public void setHealAmount(int amount){this.healAmount = amount;}
    public void setHealConditions(List<SpecialConditions> cond){
        this.healConditions = new ArrayList<>();
        for(SpecialConditions co : cond){
            healConditions.add(new SpecialConditions(co));
        }
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> result = new HashMap<>();
        result.put("amount", getAmount());
        result.put("item_dex", itemDexNum);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static HealItem deserialize(Map<String, Object> map){
        HealItem item = new HealItem(TItemDex.getItem((Integer) map.get("item_dex")));
        item.setAmount((Integer) map.get("amount"));
        return item;
    }

    @Override
    public HealItem clone(){
        return new HealItem(amount, healAmount, itemDexNum, healConditions, reviveItem);
    }

}
