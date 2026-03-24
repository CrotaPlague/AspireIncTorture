package com.crotaplague.torture.Files.ServerStorage.items;

public class TItemDex {
    public static ItemClass getItem(int dexNum){

        switch(dexNum){
            case 0: return TItemManager.potion;
            case 1: return TItemManager.defaultCatchDefault;
            case 2: return TItemManager.smoothStone;
            default: return null;
        }
    }
}
