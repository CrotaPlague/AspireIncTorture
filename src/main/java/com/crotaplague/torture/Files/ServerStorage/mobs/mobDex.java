package com.crotaplague.torture.Files.ServerStorage.mobs;


import com.crotaplague.torture.Files.ServerStorage.Pair;
import com.crotaplague.torture.Files.ServerStorage.items.ShulkerItem;
import com.crotaplague.torture.Files.ServerStorage.items.TItemDex;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import org.bukkit.inventory.ItemStack;


import java.util.*;

//Attack, rangedAttack, defence, rangedDefence, speed, hitPoints

public class mobDex {

    public enum ExpAmountType{
        SMALL(1.5f),
        MEDIUM(4f);
        private float multiplier;
        ExpAmountType(float multiplier){this.multiplier = multiplier;}
        float value(){return this.multiplier;}
    }


    public static mobEnums mobDex1(int dexNum, int level){

        mobEnums mob = null;
        if(dexNum == 1){
            mob = createMob(new int[]{7, 6, 7, 5, 5, 25}, "Zombie", 1, new ItemStack(Material.ZOMBIE_SPAWN_EGG), EntityType.ZOMBIE, new moveClass[]{moveList("Scratch"), moveList("bite")}, level);
            mob.setExpEarnType(mobEnums.ExpEarnType.MEDIUM);
            mob.setBaseExpDrop(63);
        }else if(dexNum == 2){
            mob = createMob(new int[]{5, 7, 6, 8, 4, 29}, "Cow", 2, new ItemStack(Material.COW_SPAWN_EGG), EntityType.COW, new moveClass[]{moveList("Trample"), moveList("bite")}, level);
        }else if(dexNum == 3) {
            mob = createMob(new int[]{7, 5, 6, 5, 9, 22}, "Skeleton", 3, new ItemStack(Material.SKELETON_SPAWN_EGG), EntityType.SKELETON, new moveClass[]{moveList("Growl"), moveList("bite")}, level);
        }

        mob.setMobBall((ShulkerItem) TItemDex.getItem(1));
        return mob;
    }



    public static mobEnums createMob(int[] beginStats, String name, int dexNum, ItemStack icon, EntityType entityType, moveClass[] moves, Integer level){
        if(level == null){
            level = 1;
        }
        List<moveClass> newMoves = new ArrayList<>(Arrays.asList(moves));


        return new mobEnums(beginStats, name, mobEnums.Ability.SHARP_SHOOTER, dexNum, icon, entityType, newMoves, level, 0);
    }

    public static moveClass moveList(String move) {

        moveClass newMove = null;

        ArrayList moveData = new ArrayList();
        if (move.equalsIgnoreCase("Empty")) {
            newMove = new moveClass(0, "Empty", new ItemStack(Material.AIR), 0, 0, 0, "special");
            return newMove;
        }
        if(move.equalsIgnoreCase("Scratch")){
            newMove = new moveClass(1, "Scratch", new ItemStack(Material.WOODEN_SWORD), 8, 100, 25, "physical", moveClass.moveEffectTarget.ENEMY);
            return newMove;

        }
        if(move.equalsIgnoreCase("Trample")){
            newMove = new moveClass(2, "Trample", new ItemStack(Material.LEATHER_BOOTS), 12, 85, 15, "physical", moveClass.moveEffectTarget.ENEMY);
            return newMove;
        }
        if (move.equalsIgnoreCase("Bite")) {
            newMove = new moveClass(3, "Bite", new ItemStack(Material.SPIDER_EYE), 9, 90, 20, "physical", moveClass.moveEffectTarget.ENEMY);
            return newMove;
        }
        if (move.equalsIgnoreCase("Growl")) {
            newMove = new moveClass(4, "Growl", new ItemStack(Material.CARVED_PUMPKIN), 0, 100, 25, "targetStat", moveClass.moveEffectTarget.ENEMY, 100, -1, moveClass.Stat.ATTACK, moveClass.moveEffectTarget.ENEMY);
            newMove.setPercent(100);
            newMove.setTarget(moveClass.moveEffectTarget.ENEMY);
            return newMove;
        }
        if(move.equalsIgnoreCase("Protect")){
            newMove = new moveClass(5, "Protect", new ItemStack(Material.SHIELD), 0, 100, 10, "effect", moveClass.moveEffectTarget.SELF);
            return newMove;
        }
        if(move.equalsIgnoreCase("Flamethrower")){
            newMove = new moveClass(6, "Flamethrower", new ItemStack(Material.LAVA_BUCKET), 20, 90, 15, "special", moveClass.moveEffectTarget.ENEMY);
            return newMove;
        }

        if (Bukkit.getPlayer("CrotaPlague") != null)
        Bukkit.getPlayer("CrotaPlague").sendMessage("yep iz null");
        return newMove;
    }
    public static moveClass setMoveDefaults(moveClass move){
        if(move.getMoveNum() == 6){

        }
        return move;
    }

    public static HashMap LearnedMovesLiteral(String mobName){
        HashMap<Integer, String> hashMap = new HashMap<>();
        if(mobName.equalsIgnoreCase("Zombie")){hashMap.put(1, "Scratch");}
        return hashMap;
    }

    public static mobEnums.Ability[] allPosAbilities(int dex){
        return switch(dex){
            case 1 -> new mobEnums.Ability[]{mobEnums.Ability.INTIMIDATE};
            case 2 -> new mobEnums.Ability[]{mobEnums.Ability.SHARP_SHOOTER};
            case 3 -> new mobEnums.Ability[]{mobEnums.Ability.THIN};
            default -> new mobEnums.Ability[]{};
        };
    }

    public static mobEnums.Ability[] allPosAbilities(mobEnums mob){
        return allPosAbilities(mob.getDexNum());
    }

    public static Pair<mobEnums.MHTypes, mobEnums.MHTypes> getType(int dexNum){
        return switch(dexNum){
            case 1 -> new Pair<>(mobEnums.MHTypes.UNDEAD, null);
            case 2 -> new Pair<>(mobEnums.MHTypes.NORMAL, null);

            default -> new Pair<>(null, null);
        };
    }

}
