package com.crotaplague.torture.Files.ServerStorage.mobs;

import com.crotaplague.torture.Files.ServerStorage.moveClass;

public enum Nature {
    ADAMANT("Adamant", moveClass.Stat.ATTACK.getValue(), moveClass.Stat.RANGED_ATTACK.getValue()),
    BASHFUL("Bashful", -1, -1),
    Bold("Bold", moveClass.Stat.DEFENCE.getValue(), moveClass.Stat.ATTACK.getValue()),
    BRAVE("Brave", moveClass.Stat.ATTACK.getValue(), moveClass.Stat.SPEED.getValue()),
    MODEST("Modest", moveClass.Stat.RANGED_ATTACK.getValue(), moveClass.Stat.ATTACK.getValue()),
    RASH("Rash", moveClass.Stat.ATTACK.getValue(), moveClass.Stat.RANGED_DEFENCE.getValue()),
    HARDY("Hardy", -1, -1); // neutral nature

    private final String name;
    private final int increaseStat;
    private final int decreaseStat;

    Nature(String name, int increaseStat, int decreaseStat) {
        this.name = name;
        this.increaseStat = increaseStat;
        this.decreaseStat = decreaseStat;
    }

    /***
     * Returns the name of this nature
     * @return the name of this nature
     */
    public String getName() {
        return name;
    }

    public double getModifier(int statIndex) {
        if (statIndex == increaseStat) {
            return 1.1;
        } else if (statIndex == decreaseStat) {
            return 0.9;
        } else {
            return 1.0;
        }
    }

    public double getModifier(moveClass.Stat s){
        return getModifier(s.ordinal());
    }

    public static Nature fromInt(int x) {
        Nature[] natures = Nature.values();
        if (x >= 0 && x < natures.length) {
            return natures[x];
        }
        return null;
    }

}

