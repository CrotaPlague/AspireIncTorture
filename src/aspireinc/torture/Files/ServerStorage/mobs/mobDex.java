package aspireinc.torture.Files.ServerStorage.mobs;

import aspireinc.torture.Files.ServerStorage.moveClass;
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
            mob = createMob(new int[]{7, 6, 7, 5, 5, 25}, "Zombie", 1, new String[]{"intimidate", "moon"}, new ItemStack(Material.ZOMBIE_SPAWN_EGG), EntityType.ZOMBIE, new moveClass[]{moveList("Scratch"), moveList("bite")}, level);
        }else if(dexNum == 2){
            mob = createMob(new int[]{5, 7, 6, 8, 4, 29}, "Cow", 2, new String[]{"run-around", "grass-eater"}, new ItemStack(Material.COW_SPAWN_EGG), EntityType.COW, new moveClass[]{moveList("Trample"), moveList("bite")}, level);
        }else if(dexNum == 3){
            mob = createMob(new int[]{7, 5, 6, 5, 9, 22}, "Skeleton", 3, new String[]{"replenish", "thin"}, new ItemStack(Material.SKELETON_SPAWN_EGG), EntityType.SKELETON,new moveClass[]{moveList("Growl"), moveList("bite")}, level);
        }



        return mob;
    }



    public static mobEnums createMob(int[] beginStats, String name, int dexNum, String[] abilities, ItemStack icon, EntityType entityType, moveClass[] moves, Integer level){
        if(level == null){
            level = 1;
        }
        List<Integer> newStats = new ArrayList<>();

        for(int i : beginStats){
            newStats.add(i);
        }
        List<String> newAbilities = new ArrayList<>(Arrays.asList(abilities));
        List<moveClass> newMoves = new ArrayList<>(Arrays.asList(moves));


        return new mobEnums(newStats, name, "placeholder", newAbilities, dexNum, icon, entityType, newMoves, level, 0);
    }

    public static moveClass moveList(String move) {

        moveClass newMove = null;

        ArrayList moveData = new ArrayList();
        if (move.equalsIgnoreCase("Empty")) {
            newMove = new moveClass(0, "Empty", new ItemStack(Material.AIR), 0, 0, 0, "special");
        }
        if(move.equalsIgnoreCase("Scratch")){
            newMove = new moveClass(1, "Scratch", new ItemStack(Material.WOODEN_SWORD), 8, 100, 25, "physical");
        }
        if(move.equalsIgnoreCase("Trample")){
            newMove = new moveClass(2, "Trample", new ItemStack(Material.LEATHER_BOOTS), 12, 85, 15, "physical");
        }
        if (move.equalsIgnoreCase("Bite")) {
            newMove = new moveClass(3, "Bite", new ItemStack(Material.SPIDER_EYE), 9, 90, 20, "physical");
        }
        if (move.equalsIgnoreCase("Growl")) {
            newMove = new moveClass(4, "Growl", new ItemStack(Material.CARVED_PUMPKIN), 2, 100, 25, "targetStat");
        }
        if(move.equalsIgnoreCase("Protect")){
            newMove = new moveClass(5, "Protect", new ItemStack(Material.CARVED_PUMPKIN), 0, 100, 10, "effect");
        }
        if(move.equalsIgnoreCase("Flamethrow")){
            newMove = new moveClass(6, "Flamethrow", new ItemStack(Material.LAVA_BUCKET), 20, 90, 15, "special");
        }

        if(newMove == null){
            Bukkit.getPlayer("CrotaPlague").sendMessage("yep iz null");
        }
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


}
