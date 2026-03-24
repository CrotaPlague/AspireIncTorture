package com.crotaplague.torture.Files.ServerStorage.items;


import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShulkerItem extends Sellable implements Cloneable {

    private int catchPower = 0;
    private mobEnums possibleCatch;

    public ShulkerItem(int amount, int dexNum) {
        super(TItemType.SHULKERSHELLS, amount, dexNum);
        catchPower = ((ShulkerItem) TItemDex.getItem(dexNum)).getCatchPower();
    }

    public ShulkerItem(int amount, int dexNum, int catchPower){
        super(TItemType.SHULKERSHELLS, amount, dexNum);
        this.catchPower = catchPower;
        itemDexNum =dexNum;
    }

    public ShulkerItem(int amount, int dexNum, int catchPower, ItemStack displayItem, String name){
        super(TItemType.SHULKERSHELLS, amount, dexNum, displayItem, name);
        this.catchPower = catchPower;
        itemDexNum =dexNum;
    }

    public ShulkerItem(){
        super(TItemType.SHULKERSHELLS, 0, 0);
        this.catchPower = 0;
        this.setAmount(0);
    }

    public ShulkerItem(ItemClass item) {
        super(TItemType.SHULKERSHELLS, item.getAmount(), item.getItemDexNum());
    }
    public ShulkerItem(ItemClass item, int catchPower, mobEnums possibleCatch) {
        super(TItemType.SHULKERSHELLS, item.getAmount(), item.getItemDexNum());
        this.catchPower = catchPower;
        this.possibleCatch = new mobEnums(possibleCatch);
    }

    public boolean catchMob(mobEnums mob) {
        boolean caught = true;
        if (this.catchPower != 0) {
            int onePart = ((3*mob.getStats()[moveClass.Stat.HIT_POINTS.getValue()]) - (2 * mob.getCurrentHp()));
            onePart = onePart * mob.getCatchResilience();
            onePart *= catchPower;
            onePart /= (3*mob.getStats()[moveClass.Stat.HIT_POINTS.getValue()]);
            if(mob.getCondition() != null) onePart *= mob.getCondition().getCondition().getCatchChange();

        }
        return caught;
    }
    public void setPossibleCatch(mobEnums pos){
        this.possibleCatch = pos;
    }
    public mobEnums getPossibleCatch(){return this.possibleCatch;}
    public void setCatchPower(int catchPower){this.catchPower = catchPower;}
    public int getCatchPower(){return this.catchPower;}
    public void setItemDexNum(int itemNum){itemDexNum = itemNum;}

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> result = new HashMap<>();
        result.put("amount", getAmount());
        result.put("item_dex", itemDexNum);
        return result;
    }

    public static ShulkerItem deserialize(Map<String, Object> map){
        ShulkerItem item = new ShulkerItem(TItemDex.getItem((Integer) map.get("item_dex")));
        item.setAmount((Integer) map.get("amount"));
        return item;
    }

    @Override
    public ShulkerItem clone(){
        return new ShulkerItem();
    }

}
