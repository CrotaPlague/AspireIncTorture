package com.crotaplague.torture.Files.ServerStorage;

import com.crotaplague.torture.Files.ServerStorage.items.HoldingItems;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import org.jspecify.annotations.NonNull;

public class NextMove implements Comparable<NextMove>{

    private static final int SWAP_PRIORITY = 9_999_999;
    private static final int ITEM_PRIORITY = 999_999;
    private static final int FIRST_MOVE_PRIORITY = 99_999;

    public enum ActionType {
        MOVE,
        ITEM,
        SWAP,
        NONE
    }

    private int speed;
    private moveClass move;
    private ItemClass item;
    private mobEnums swapTarget;
    private ActionType type = ActionType.NONE;

    public NextMove(){}

    protected void setMove(moveClass move){
        this.type = ActionType.MOVE;
        this.move = move;
    }

    public static NextMove fromMove(moveClass move, mobEnums user){
        NextMove nextMove = new NextMove();
        nextMove.initMove(move, user);
        return nextMove;
    }

    public static NextMove fromItem(ItemClass item, mobEnums user){
        NextMove nextMove = new NextMove();
        nextMove.initItem(item, user);
        return nextMove;
    }

    public static NextMove fromSwap(mobEnums swapTarget){
        NextMove nextMove = new NextMove();
        nextMove.initSwap(swapTarget);
        return nextMove;
    }

    public static NextMove fromSelection(Object selection, mobEnums user){
        if(selection == null){
            return null;
        }
        if(selection instanceof NextMove nextMove){
            return nextMove;
        }
        if(selection instanceof moveClass move){
            return fromMove(move, user);
        }
        if(selection instanceof ItemClass item){
            return fromItem(item, user);
        }
        if(selection instanceof mobEnums mob){
            return fromSwap(mob);
        }
        throw new IllegalArgumentException("Unsupported next move selection type: " + selection.getClass().getName());
    }

    private void initMove(moveClass move, mobEnums mob){
        this.type = ActionType.MOVE;
        this.move = move;
        this.item = null;
        this.swapTarget = null;
        this.speed = getBaseSpeed(mob) + (move.isFirst() ? FIRST_MOVE_PRIORITY : 0);
        applyHeldItemPriority(mob);
    }

    private void initItem(ItemClass item, mobEnums mob){
        this.type = ActionType.ITEM;
        this.item = item;
        this.move = null;
        this.swapTarget = null;
        this.speed = getBaseSpeed(mob) + ITEM_PRIORITY;
        applyHeldItemPriority(mob);
    }

    private void initSwap(mobEnums swapTarget){
        this.type = ActionType.SWAP;
        this.swapTarget = swapTarget;
        this.item = null;
        this.move = null;
        this.speed = SWAP_PRIORITY;
    }

    private int getBaseSpeed(mobEnums mob){
        return mob.getStats()[moveClass.Stat.SPEED.getValue()];
    }

    private void applyHeldItemPriority(mobEnums mob){
        HoldingItems held = mob.getHeldItem();
        if(held == null){
            return;
        }
        double healthPercent = mob.getCurrentHp() / (double) mob.getMaxHp();
        if(healthPercent < held.getActivatePercent()){
            // Reserved for held-item speed interactions.
        }
    }

    public int getSpeed(){return speed;}
    public ActionType getType(){return type;}
    public moveClass getMove(){return move;}
    public ItemClass getItem(){return item;}
    public mobEnums getSwapTarget(){return swapTarget;}

    public boolean isMove(){return type == ActionType.MOVE && move != null;}
    public boolean isItem(){return type == ActionType.ITEM && item != null;}
    public boolean isSwap(){return type == ActionType.SWAP && swapTarget != null;}

    public void setUser(mobEnums user){
        if(isMove()){
            initMove(move, user);
            return;
        }
        if(isItem()){
            initItem(item, user);
        }
    }

    @Override
    public int compareTo(@NonNull NextMove o) {
        return Integer.compare(o.speed, this.speed);
    }
}
