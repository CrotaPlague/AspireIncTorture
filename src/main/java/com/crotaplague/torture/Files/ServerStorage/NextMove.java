package com.crotaplague.torture.Files.ServerStorage;

import com.crotaplague.torture.Files.ServerStorage.items.HoldingItems;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import org.jspecify.annotations.NonNull;

public class NextMove implements Comparable<NextMove>{

    protected int speed;
    moveClass move;
    public NextMove(moveClass move, mobEnums mob){
        this.speed = mob.getStats()[moveClass.Stat.SPEED.getValue()];
        this.speed += move.isFirst() ? 1000 : 0;
        this.move = move;
        HoldingItems item = mob.getHeldItem();
        if(item != null){
            item.
        }
    }

    @Override
    public int compareTo(@NonNull NextMove o) {
        return 0;
    }
}
