package com.crotaplague.torture.Files.ServerStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.util.logging.Level;

public enum battleTypes{
    SOLOPVP, WILD, SOLOPVE,TAGBATTLE,DOUBLEPVE, DOUBLEPVP;
    @Nullable
    public battleTypes fromInt(int passed){
        battleTypes type = null;
        switch(passed) {
            case (0):
                type = com.crotaplague.torture.Files.ServerStorage.battleTypes.SOLOPVP;
                break;
            case(1):
                type= com.crotaplague.torture.Files.ServerStorage.battleTypes.WILD;
                break;
            case(2):
                type= com.crotaplague.torture.Files.ServerStorage.battleTypes.SOLOPVE;
                break;
            case(3):
                type = com.crotaplague.torture.Files.ServerStorage.battleTypes.TAGBATTLE;
                break;
            case(4):
                type= com.crotaplague.torture.Files.ServerStorage.battleTypes.DOUBLEPVE;
                break;
            case(5):
                type= com.crotaplague.torture.Files.ServerStorage.battleTypes.DOUBLEPVP;
                break;
            default:
                Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "[Crota] battle type not found.");
                break;
        }
        return type;
    }
    public int toInt(){
        return switch (this) {
            case SOLOPVP -> 0;
            case WILD -> 1;
            case SOLOPVE -> 2;
            case TAGBATTLE -> 3;
            case DOUBLEPVE -> 4;
            case DOUBLEPVP -> 5;
        };
    }
}

