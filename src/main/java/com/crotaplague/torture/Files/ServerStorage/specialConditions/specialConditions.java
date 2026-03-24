package com.crotaplague.torture.Files.ServerStorage.specialConditions;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class specialConditions implements ConfigurationSerializable {

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("turn", turn);
        result.put("condition", condition.condNum);
        return result;
    }
    @SuppressWarnings("unchecked")
    public static specialConditions deserialize(Map<String, Object> result){
        final int turn = (int) result.get("turn");
        final specialCondition condition = specialCondition.fromInt((int)result.get("condition"));
        return new specialConditions(turn, condition);
    }

    public enum specialCondition{
        BURN(((1/18)), 100, effect.DAMAGE, 25, 0, 1.1, TextColor.color(230, 61, 0)),
        POISON((1/18), 100, effect.DAMAGE, 25, 1, 1.1, TextColor.color(158, 0, 181)),
        FROZEN(0f, 60, effect.STUN, 25, 2, 1.5, TextColor.color(20, 162, 196)),;
        final float damage;
        final int percent;
        final effect thisEffect;
        final float disappearChance;
        final int condNum;
        final double catchChange;
        final int maxLength;
        final TextColor color;
        specialCondition(final float damage, final int percent, final effect thisEffect, float disappearChance, final int condNum, final double catchChange, TextColor color){
            this.damage = damage; this.percent = percent; this.thisEffect = thisEffect; this.disappearChance = disappearChance; this.condNum = condNum; this.catchChange = catchChange; this.maxLength = Integer.MAX_VALUE; this.color = color;
        }
        specialCondition(final float damage, final int percent, final effect thisEffect, float disappearChance, final int condNum, final double catchChange, int maxLength, TextColor color){
            this.damage = damage; this.percent = percent; this.thisEffect = thisEffect; this.disappearChance = disappearChance; this.condNum = condNum; this.catchChange = catchChange; this.maxLength = maxLength; this.color = color;
        }
        public static specialCondition fromInt(int x){
            switch(x){
                case 0:
                    return BURN;
                case 1:
                    return POISON;
                case 2:
                    return FROZEN;
            }
            return null;
        }
        public int getPercent(){return this.percent;}
        public effect getEffect(){return this.thisEffect;}
        public float getDamage(){return this.damage;}
        public double getCatchChange(){return this.catchChange;}
        public int getMaxTurns(){return this.maxLength;}
        public TextColor getColor(){return this.color;}
        public String getName(){
            switch (this){
                case BURN: return "burned";
                case POISON: return "poisoned";
                case FROZEN: return "frozen";
                default: return "";
            }
        }
    }
    public enum effect{
        STUN(0), DAMAGE(1);
        final int value;
        effect(int value){this.value = value;}
    }
    int turn; // how many turns the effect has been on for
    final specialCondition condition;
    public specialConditions(specialCondition condition){
        turn = 1;
        this.condition = condition;
    }
    public specialConditions(int turn, specialCondition condition){
        this.turn = turn;
        this.condition = condition;
    }
    public specialConditions(specialConditions cond){
        this.turn = cond.turn;
        this.condition = cond.getCondition();
    }
    public int getTurn(){return this.turn;}
    public void setTurn(int turn){this.turn = turn;}
    public specialCondition getCondition(){return this.condition;}


}
