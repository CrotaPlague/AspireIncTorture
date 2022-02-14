package aspireinc.torture.Files.ServerStorage.mobs;


import aspireinc.torture.Files.ServerStorage.moveClass;
import aspireinc.torture.Files.ServerStorage.specialConditions.specialConditions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aspireinc.torture.Files.ServerStorage.mobs.mobDex.moveList;

public class mobEnums implements ConfigurationSerializable {


    private List<Integer> stats;
    private String name;
    private String ability;
    private List<String> allPosAbilities;
    private int dexNum;
    private ItemStack icon;
    private EntityType mojangMobType;
    private List<moveClass> moves;
    private int level;
    private int exp;
    private int currentHp;
    private List<Integer> tempStats;
    private specialConditions condition;

    public enum ExpEarnType{
        SLOW(0.75f),
        MEDIUM(1);
        private float value;
        ExpEarnType(float num){this.value = num;};
    }

    public mobEnums(List<Integer> stats, String name, String ability, List<String> allPosAbilities, int dexNum, ItemStack icon, EntityType mojangMobType, List<moveClass> moves, int level, int exp) {
        this.stats = stats;
        this.name = name;
        this.ability = ability;
        this.allPosAbilities = allPosAbilities;
        this.dexNum = dexNum;
        this.icon = icon;
        this.mojangMobType = mojangMobType;
        this.moves = moves;
        this.level = level;
        this.exp = exp;
        this.currentHp = stats.get(moveClass.stat.HIT_POINTS.getValue());
        while(moves.size() != 4){
            moves.add(moveList("Empty"));
        }
        this.tempStats = stats;
        condition = null;
    }
    public mobEnums(List<Integer> stats, List<Integer> tempStats, String name, String ability, List<String> allPosAbilities, int dexNum, ItemStack icon, EntityType mojangMobType, List<moveClass> moves, int level, int exp) {
        this.stats = stats;
        this.name = name;
        this.ability = ability;
        this.allPosAbilities = allPosAbilities;
        this.dexNum = dexNum;
        this.icon = icon;
        this.mojangMobType = mojangMobType;
        this.moves = moves;
        this.level = level;
        this.exp = exp;

        while(moves.size() != 4){
            moves.add(moveList("Empty"));
        }
        this.tempStats = tempStats;
        this.currentHp = tempStats.get(moveClass.stat.HIT_POINTS.getValue());
        condition = null;
    }



    public void setStats(List mobStatsToSet){ this.stats = (List<Integer>) mobStatsToSet;}

    public void setName(String name1){ this.name = name1;}

    public String getName(){return this.name; }

    public void setAbility(String ability1){this.ability = ability1;}

    public void setAllPosAbilities(List<String> allPosAbilities1){this.allPosAbilities = allPosAbilities1;}

    public List<Integer> getStats(){
        return this.stats;
    }

    public String getAbilities(){return this.ability;}

    public List<String> getAllPosAbilities(){return this.allPosAbilities;}

    public void setDexNum(int dexNum){this.dexNum = dexNum;}

    public int getDexNum(){return this.dexNum; }

    public void setIcon(ItemStack newIcon){this.icon = newIcon; }

    public ItemStack getIcon(){return this.icon; }

    public EntityType getType(){return this.mojangMobType; }

    public void setType(EntityType newMojangMobType){this.mojangMobType = newMojangMobType; }

    public void setMove(moveClass move, int num){this.moves.set(num, move); }

    public void setAllMoves(List<moveClass> moveNames){int i = 0; for(moveClass move : moveNames){this.moves.set(i, move); i++;}}

    public moveClass getMove(int num){return this.moves.get(num); }

    public List<moveClass> getMoves(){return this.moves; }

    public void setLevel(int level){this.level = level; }

    public int getLevel(){return this.level; }

    public int getCurrentHp(){return this.currentHp;}
    public void setCurrentHp(int hp){this.currentHp = hp;}

    public int getExp(){return this.exp;}

    public specialConditions getCondition(){return this.condition;}
    public void setCondition(specialConditions condition){this.condition = condition;}

    @Override
    public Map<String, Object> serialize() {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("stats", stats);
        result.put("tempStats", tempStats);
        result.put("name", name);
        result.put("ability", ability);
        result.put("allPosAbilities", allPosAbilities);
        result.put("dexNum", dexNum);
        result.put("icon", icon.serialize());
        result.put("MobType", mojangMobType.toString());
        int i = 0;
        while(i<moves.size()){
            if(i>=moves.size()) break;
            result.put("move " + i, moves.get(i).serialize());

            i++;
        }
        result.put("level", level);
        result.put("exp", this.exp);
        result.put("currentHp", this.currentHp);
        result.put("condition", condition.serialize());


        return result;
    }
    @SuppressWarnings("unchecked")
    public static mobEnums deserialize(Map<?, ?> map) {
        final List<Integer> stats = (List) map.get("stats");
        final List<Integer> tempStats = (List) map.get("tempStats");
        final String name = (String) map.get("name");
        final String ability = (String) map.get("ability");
        final List<String> allPosAbilities = (List<String>) map.get("allPosAbilities");
        final int dexNum = (int) map.get("dexNum");
        MemorySection section = (MemorySection) map.get("icon");
        final ItemStack icon = ItemStack.deserialize(section.getValues(false));

        final EntityType mojangMobType = EntityType.valueOf((String) map.get("MobType"));
        List<moveClass> moves = new ArrayList<>();
        for(int i = 0; i<map.size(); i++){
            if(map.get("move " + i)!= null){
                section = (MemorySection) map.get("move " + i);
                moves.add(moveClass.deserialize(section.getValues(false)));
            }
        }
        final int level = (int) map.get("level");
        final int exp = (int) map.get("exp");

        mobEnums mob = new mobEnums(stats, tempStats, name, ability, allPosAbilities, dexNum, icon, mojangMobType, moves, level, exp);

        if(map.get("condition") != null) mob.setCondition((specialConditions) map.get("condition"));

        return mob;
    }
}