package com.crotaplague.torture.Files.ServerStorage.boxs;



import com.crotaplague.torture.Torture;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class boxDex {
    public static ArenaBox returnBox(int boxNum){
        List<Location> list = new ArrayList<Location>();
        ArenaBox box = null;
        if(boxNum == 1){
            List<Location> playerLocs = Arrays.asList(new Location[]{new Location(Torture.world,-208,90,603.5), new Location(Torture.world,-201,90,609)});
            List<Location> mobLocs = Arrays.asList(new Location[]{new Location(Torture.world, -207.5, 90, 606.5), new Location(Torture.world, -203.5, 90, 606.5)});  // sort by, side, so Trainer, Trainer, Opponent, Opponent
            box = new ArenaBox(mobLocs,playerLocs);
            List<Location> displayChoicesA = List.of(new Location[]{location(-207.5, 91, 604.8), location(-207.5, 90, 604.8), location(-208.5, 91, 604.8), location(-208.5, 90, 604.8)});  // fight, bag, mobs, run
            List<Location> displayChoicesB = List.of(new Location[]{location(-201, 91, 607), location(-201, 90, 607), location(-200, 91, 607), location(-200, 90, 607)});
            box.setDisplayChoices(displayChoicesA, displayChoicesB);
        }
        if(boxNum == 2){
            List<Location> playerLocs = Arrays.asList(new Location[]{location(-210, 105, 628.5), location(-202, 105, 637)});
            List<Location> mobsLocs = Arrays.asList(location(0, 0 ,0));
        }
        return box;
    }
    public static Location location(int a, int b, int c){
        return new Location(Torture.world, a, b, c);
    }
    public static Location location(float a, float b, float c){
        return new Location(Torture.world, a, b, c);
    }
    public static Location location(double a, double b, double c){
        return new Location(Torture.world, a, b, c);
    }
    public static Location location(int a, int b, double c){
        return location(a, (double)b, c);
    }
    public static Location location(Number a, Number b, Number c){
        return location(a.doubleValue(), b.doubleValue(), c.doubleValue());
    }
}
