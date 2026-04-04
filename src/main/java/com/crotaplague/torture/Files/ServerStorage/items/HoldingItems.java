package com.crotaplague.torture.Files.ServerStorage.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HoldingItems extends Sellable implements Cloneable{
    private double activatePercent;
    private boolean onAttack;
    public HoldingItems(TItemType type, int amount, int dexNum, ItemStack displayItem, String name) {
        super(type, amount, dexNum, displayItem, name);
    }
    public HoldingItems(HoldingItems hold) {
        super(hold);
    }

    public HoldingItems(Sellable s){
        super(s);
        if(s instanceof HoldingItems h){
            this.activatePercent = h.activatePercent; this.onAttack = h.onAttack;
        }
    }

    private void setActivatePercent(double percent){
        this.activatePercent = percent;
    }
    private double getActivatePercent(){return this.activatePercent;}
    private boolean isOnAttack(){return this.onAttack;}
    private void setOnAttack(boolean b){this.onAttack = b;}
    @Override
    public HoldingItems clone() {
        return new HoldingItems(this);
    }

    public static HoldingItems deserialize(Map<String, Object> entry){
        Sellable item = Sellable.deserialize(entry);
        return new HoldingItems(item);
    }

}
