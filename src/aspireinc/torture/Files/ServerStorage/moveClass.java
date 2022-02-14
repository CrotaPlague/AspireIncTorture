package aspireinc.torture.Files.ServerStorage;

import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import aspireinc.torture.Files.ServerStorage.specialConditions.specialConditions;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class moveClass implements ConfigurationSerializable {

        private int moveNum;
        private int damage;
        private String name;
        private final ItemStack icon;
        private int accuracy;
        private int allowedUses;
        private String moveType;
        private moveEffectTarget target;
        private int effectChance;
        private mobEnums mobTarget;
        private int effectAmount;
        private stat effectStat;
        private int totalAllowedUses;
        private List<specialConditions> posConditions;

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

        public enum stat{
            ATTACK(0),
            RANGED_ATTACK(1),
            DEFENCE(2),
            RANGED_DEFENCE(3),
            SPEED(4),
            HIT_POINTS(5);

            public static stat fromInt(int x){
                switch(x){
                    case 0:
                        return ATTACK;
                    case 1:
                        return RANGED_ATTACK;
                    case 2:
                        return DEFENCE;
                    case 3:
                        return RANGED_DEFENCE;
                    case 4:
                        return SPEED;
                    case 5:
                        return HIT_POINTS;
                }

                return null;
            }
            public int getValue(){return this.value;}


            private final int value;
            stat(int value){this.value = value;}
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
        }
        public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target, int effectChance, int effectAmount, stat effectStat) {
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
        }
        public moveClass(int moveNum, String name, ItemStack icon, int damage, int accuracy, int allowedUses, String moveType, moveEffectTarget target, int effectChance, int effectAmount, stat effectStat, final mobEnums mobTarget) {
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
        }



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
        public Boolean hasEffect(){if(this.effectAmount == 0) return false; else return true;};
        public stat getEffectStat(){return this.effectStat;}
    public int getTotalAllowedUses(){return this.totalAllowedUses;}
    public void setTotalAllows(int total){this.totalAllowedUses = total;}
    public void setCondition(specialConditions condition){this.posConditions.add(condition);}
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

        result.put("target", target.value);
        result.put("chance", effectChance);
        result.put("amount", effectAmount);
        result.put("effectStat", effectStat.value);
        result.put("totalAllowedUses", totalAllowedUses);
        if(mobTarget != null)
        result.put("mobTarget", mobTarget.serialize());
        else result.put("mobTarget", null);
        return result;
    }


    @SuppressWarnings("unchecked")
    public static moveClass deserialize(final Map<String, Object> map) {
            final int moveNum = (int) map.get("moveNum");
            final int damage = (int) map.get("damage");
            final String name = (String) map.get("name");
            MemorySection section = (MemorySection) map.get("icon");
            final ItemStack icon = ItemStack.deserialize(section.getValues(false));
            final int accuracy = (int) map.get("accuracy");
            final int allowedUses = (int) map.get("allowedUses");
            final String moveType = (String) map.get("moveType");
            final moveEffectTarget target = moveEffectTarget.fromInt((Integer) map.get("target"));
            mobEnums mobTarget = (mobEnums) map.get("mobTarget");
            final int chance = (int) map.get("chance");
            final int amount = (int) map.get("amount");
            final stat effectStat = stat.fromInt((Integer) map.get("effectStat"));
            final int totalAllowed = (int) map.get("totalAllowedUses");
            moveClass move = new moveClass(moveNum, name, icon, damage, accuracy, allowedUses, moveType, target, chance, amount, effectStat, mobTarget);
            move.setTotalAllows(totalAllowed);
            return move;
    }
}
