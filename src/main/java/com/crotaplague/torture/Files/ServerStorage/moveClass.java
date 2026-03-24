package com.crotaplague.torture.Files.ServerStorage;



import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.specialConditions.specialConditions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

public class moveClass implements ConfigurationSerializable {

    private int moveNum;   // move number
    private int damage;    // damage amount
    private String name;     // move name
    private final ItemStack icon;   // move icon
    private int accuracy;     // the move accuracy
    private int allowedUses;    // currently remaining usages
    private String moveType;    // the type
    private moveEffectTarget target = moveEffectTarget.ENEMY;    // who the damaging element hits
    private int effectChance;    // the percent chance that the effect change lands
    private mobEnums mobTarget;    // the selected target
    private int effectAmount;    // how much will the stat get effected?
    private Stat effectStat;    // which stat is changing?
    private int totalAllowedUses;   //  the beginning usages
    private boolean goesFirst = false;   // if it is going to go first
    private UUID targetID;
    private List<specialConditions> posConditions = new ArrayList<specialConditions>();
    private moveEffectTarget effectTarget;    //  who's stat gets changed by the effect?

    public enum moveEffectTarget{
        SELF (1),
        ENEMY(2);

        public static moveEffectTarget fromInt(int x){
            switch(x){
                case 1:
                    return SELF;
                case 2:
                    return ENEMY;
            }
            return null;
        }

        private final int value;
        moveEffectTarget(int value){
            this.value = value;
        }
        public int getValue(){return this.value;}
    }

    public enum Stat {
        ATTACK(0, "attack"),
        RANGED_ATTACK(1, "ranged attack"),
        DEFENCE(2, "defence"),
        RANGED_DEFENCE(3, "ranged defence"),
        SPEED(4, "speed"),
        HIT_POINTS(5, "hit points"),
        ACCURACY(6, "accuracy"),
        EVASIVENESS(7, "evasiveness"),;

        public static Stat fromInt(int x){
            return switch (x) {
                case 0 -> ATTACK;
                case 1 -> RANGED_ATTACK;
                case 2 -> DEFENCE;
                case 3 -> RANGED_DEFENCE;
                case 4 -> SPEED;
                case 5 -> HIT_POINTS;
                case 6 -> ACCURACY;
                case 7 -> EVASIVENESS;
                default -> null;
            };

        }
        public int getValue(){return this.value;}
        public String getName(){return this.val2;}

        private final int value;
        private final String val2;
        Stat(int value, String str){this.value = value; this.val2 = str;}
        @Override
        public String toString() {
            return val2;
        }

    }

    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.moveType = moveType;
        this.target = null;
        this.effectChance = 0;
        this.mobTarget = null;
        this.effectAmount = 0;
        this.effectStat = null;
        this.totalAllowedUses = allowedUses;
        this.goesFirst = false;
    }
    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.moveType = moveType;
        this.target = null;
        this.effectChance = 0;
        this.mobTarget = null;
        this.effectAmount = 0;
        this.effectStat = null;
        this.totalAllowedUses = allowedUses;
        this.goesFirst = false;
        this.target = target;
    }
    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, Stat effectStat) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.target = null;
        this.effectChance = 0;
        this.mobTarget = null;
        this.effectAmount = 0;
        this.moveType = moveType;
        this.totalAllowedUses = allowedUses;
        this.goesFirst = false;
        this.effectStat = effectStat;
    }
    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target, int effectChance, int effectAmount, Stat effectStat) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.moveType = moveType;
        this.target = target;
        this.effectChance = effectChance;
        this.mobTarget = null;
        this.effectAmount = effectAmount;
        this.effectStat = effectStat;
        this.goesFirst = false;
    }
    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target, int effectChance, int effectAmount, Stat effectStat, moveEffectTarget effectTarget) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.moveType = moveType;
        this.target = target;
        this.effectChance = effectChance;
        this.mobTarget = null;
        this.effectAmount = effectAmount;
        this.effectStat = effectStat;
        this.goesFirst = false;
        this.effectTarget = effectTarget;
    }
    public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target, int effectChance, int effectAmount, Stat effectStat, final mobEnums mobTarget) {
        this.damage = damage;
        this.name = name;
        this.moveNum = moveNum;
        this.icon = icon;
        ItemMeta itemMeta = icon.getItemMeta();
        if(itemMeta!= null){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.icon.setItemMeta(itemMeta);
        }
        this.accuracy = accuracy;
        this.allowedUses = allowedUses;
        this.moveType = moveType;
        this.target = target;
        this.effectChance = effectChance;
        this.mobTarget = mobTarget;
        this.effectAmount = effectAmount;
        this.effectStat = effectStat;
        this.goesFirst = false;
    }

    public void setEffectTarget(moveEffectTarget effectTarget) {this.effectTarget = effectTarget;}
    public moveEffectTarget getEffectTarget(){return this.effectTarget;}
    public void setDamage(int num){this.damage = num; }
    public int getDamage(){return this.damage; }
    public void setName(String name){this.name = name; }
    public String getMoveName(){return this.name; }
    public void setMoveNum(int moveNum){this.moveNum = moveNum; }
    public int getMoveNum(){return this.moveNum; }
    public ItemStack getIcon(){return this.icon; }
    public String getMoveType(){return this.moveType;}
    public void setTarget(moveEffectTarget target){this.target = target;}
    public moveEffectTarget getTarget(){return this.target;}
    public void setPercent(int chance){this.effectChance = chance;}
    public int getPercent(){return this.effectChance;}
    public void setEffectAmount(int amount){this.effectAmount = amount;}
    public int getEffectAmount(){return this.effectAmount;}
    public mobEnums getMobTarget(){return this.mobTarget;}
    public void setMobTarget(mobEnums mobTarget){this.mobTarget = mobTarget;}
    public Boolean hasEffect(){if(this.effectAmount == 0){return false;} return true;};
    public Stat getEffectStat(){return this.effectStat;}
    public void setGoesFirst(boolean goesFirst){this.goesFirst = goesFirst;}
    public boolean isFirst(){return this.goesFirst;}
    public int getTotalAllowedUses(){return this.totalAllowedUses;}
    public void setTotalAllows(int total){this.totalAllowedUses = total;}
    public UUID getTargetId(){return this.targetID;}
    /**
     * @param condition sets the new condition for the move.
     * @param character literally, what are you here for, kys rn fr fr ong.
     */
    public void setCondition(specialConditions condition, char character){this.posConditions.add(condition);}
    public List<specialConditions> getConditions(){return this.posConditions;}

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("moveNum", moveNum);
        result.put("damage", damage);
        result.put("name", name);
        result.put("icon", icon.serialize());
        result.put("accuracy", accuracy);
        result.put("allowedUses", allowedUses);
        result.put("moveType", moveType);
        if(this.target != null)
        result.put("target", target.getValue());
        result.put("chance", effectChance);
        result.put("amount", effectAmount);
        if(this.effectStat != null)
        result.put("effectStat", effectStat.getValue());
        result.put("totalAllowedUses", totalAllowedUses);
        result.put("goesFirst", this.goesFirst);
        return result;
    }


    @SuppressWarnings("unchecked")
    public static moveClass deserialize(final Map<String, Object> map) {
            final int moveNum = (int) map.get("moveNum");
            final int damage = (int) map.get("damage");
            final String name = (String) map.get("name");
            Map<String, Object> IhateYOU = new HashMap<>();
            if(map.get("icon") instanceof MemorySection) IhateYOU = ((MemorySection) map.get("icon")).getValues(false);
            if(map.get("icon") instanceof LinkedHashMap) IhateYOU = (Map<String, Object>) map.get("icon");
            if(IhateYOU.isEmpty()) Bukkit.getLogger().log(Level.SEVERE, "FUCK!!!!!!!!!!!!!");
            final ItemStack icon = ItemStack.deserialize(((IhateYOU)));
            final int accuracy = (int) map.get("accuracy");
            final int allowedUses = (int) map.get("allowedUses");
            final String moveType = (String) map.get("moveType");
            final moveEffectTarget target;
            if(map.get("target") != null){
                target = moveEffectTarget.fromInt((Integer) map.get("target"));
            }else{
                target = null;
            }

            mobEnums mobTarget = (mobEnums) map.get("mobTarget");
            final int chance = (int) map.get("chance");
            final int amount = (int) map.get("amount");
            final Stat effectStat;
            if(map.get("effectStat") != null){
                effectStat = Stat.fromInt((Integer) map.get("effectStat"));
            }else{
                effectStat = null;
            }

            final int totalAllowed = (int) map.get("totalAllowedUses");
            moveClass move = new moveClass(moveNum, name, icon, damage, accuracy, allowedUses, moveType, target, chance, amount, effectStat, mobTarget);
            move.setTotalAllows(totalAllowed);
            move.setGoesFirst((boolean) map.get("goesFirst"));
            return move;
    }

    @Override
    public String toString(){
        return "name: " + this.name + " damage: " + damage + " target: " + this.mobTarget;
    }

}
