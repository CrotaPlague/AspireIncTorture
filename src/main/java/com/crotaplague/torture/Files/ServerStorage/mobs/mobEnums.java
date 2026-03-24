package com.crotaplague.torture.Files.ServerStorage.mobs;

import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.Pair;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.items.ShulkerItem;
import com.crotaplague.torture.Files.ServerStorage.items.TItemManager;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import com.crotaplague.torture.Files.ServerStorage.specialConditions.specialConditions;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static com.crotaplague.torture.Files.ServerStorage.mobs.mobDex.moveList;


public class mobEnums implements ConfigurationSerializable, Cloneable {

    private int[] iv;
    private int [] ev;
    private int[] stats;
    private String name;
    private Ability ability;
    private int dexNum;
    private ItemStack icon;
    private EntityType mojangMobType;
    private List<moveClass> moves;
    private int level;
    private int exp;
    private int currentHp;
    private specialConditions condition = null;
    private ExpEarnType expEarnType;
    private humanClass.Trainer trainer;
    private int baseExpDrop = 0;
    private Entity thisEntity;
    private mobEnums opponentMob;
    private UUID ident = UUID.randomUUID();
    private UUID selfUUID;
    private UUID opponentMobId;
    private int opponentId;
    private int slotInMenu, slotInPc = 0, battleSpot;
    private int[] stages = {0,0,0,0,0,0,0};
    private int catchResiliance = 5;
    private ShulkerItem mobBall = TItemManager.defaultCatchDefault;
    private Object nextMove;
    private char gender;
    private String nickname;
    private Nature nature;

    public enum MHTypes {
        NORMAL, WATER, FIRE, EARTH, MAGIC, UNDEAD, SHADOW, METAL, FLYING, FROST;

        public static float getEffectiveness(MHTypes one, MHTypes two) {
            File file = new File(Torture.data.getFile().getParent() + "\\Types.csv");
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                String object = lines.get(one.ordinal() + 1); // +1 to skip header
                object = object.split(",")[two.ordinal() + 1]; // +1 to skip row name
                object = object.replace("x", "");
                return Float.valueOf(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
        public static MHTypes fromInt(int a){
            if(a > -1 && values().length > a){
                return values()[a];
            }
            throw new ArrayIndexOutOfBoundsException("MobType provided an integer out of bounds. mobEnums.java");
        }

        public String getName(){
            return switch(this){
                case NORMAL -> "Normal";
                case WATER -> "Water";
                case FIRE -> "Fire";
                case EARTH -> "Earth";
                case MAGIC -> "Magic";
                case UNDEAD -> "Undead";
                case SHADOW -> "Shadow";
                case METAL -> "Metal";
                case FLYING -> "Flying";
                case FROST -> "Frost";
            };
        }
    }


    public enum ExpEarnType{
        SLOW(0.75f),
        MEDIUM(1),
        FAST(1.25f);
        private float value;
        ExpEarnType(float num){this.value = num;};
    }

    /**
     * Copy constructor of the mob class
     * @param m The mob to copy
     */
    public mobEnums(mobEnums m){
        this();
        this.stats = Arrays.copyOf(m.stats, m.stats.length);
        this.name = m.name;
        this.ability = m.ability;
        this.dexNum = m.dexNum;
        this.icon = m.icon;
        this.mojangMobType = m.mojangMobType;
        this.moves = m.moves;
        this.level = m.level;
        this.exp = m.exp;
        this.currentHp = m.currentHp;
        this.stages = m.stages;
        this.condition = m.condition;
        this.expEarnType = m.expEarnType;
        this.trainer = m.trainer;
        this.baseExpDrop = m.baseExpDrop;
        this.thisEntity = m.thisEntity;
        this.opponentMob = m.opponentMob;
        this.ident = m.ident;
        this.selfUUID = m.selfUUID;
        this.opponentId = m.opponentId;
        this.battleSpot = m.battleSpot;
        this.nextMove = m.nextMove;
        this.gender = m.gender;
        this.iv = Arrays.copyOf(m.iv, m.iv.length);
        this.ev = Arrays.copyOf(m.ev, m.ev.length);
        this.nature = m.nature;
    }

    /**
     *
     * @param stats the stats of the mob
     * @param name  the name of the mob
     * @param ability the ability of the mob
     * @param dexNum the mobDex number of the mob
     * @param icon the item icon of the mob
     * @param mojangMobType the actual mob type of the mob
     * @param moves the moves the mob has
     * @param level the level of the mob
     * @param exp the amount of exp the mob has
*     */
    public mobEnums(int[] stats, String name, Ability ability, int dexNum, ItemStack icon, EntityType mojangMobType, List<moveClass> moves, int level, int exp) {
        this();
        this.stats = stats;
        this.name = name;
        this.ability = ability;
        this.dexNum = dexNum;
        this.icon = icon;
        this.mojangMobType = mojangMobType;
        this.moves = moves;
        this.level = level;
        this.exp = exp;
        this.currentHp = stats[moveClass.Stat.HIT_POINTS.getValue()];
        while(moves.size() != 4){
            moves.add(moveList("Empty"));
        }
        condition = null;
        loadGender();
    }

    /**
     *
     * @param stats real stats of the mob
     * @param tempStats battle stats of the mob
     * @param name name of the mob
     * @param ability the mob's ability
     * @param dexNum the mobdex number of the mob
     * @param icon the display icon of the mob
     * @param mojangMobType the actual mob type of it
     * @param moves the moves the mob has
     * @param level the level of the mob
     * @param exp the amount of experience the mob has
     */
    public mobEnums(int[] stats, List<Integer> tempStats, String name, Ability ability, int dexNum, ItemStack icon, EntityType mojangMobType, List<moveClass> moves, int level, int exp) {
        this();
        this.stats = stats;
        this.name = name;
        this.ability = ability;
        this.dexNum = dexNum;
        this.icon = icon;
        this.mojangMobType = mojangMobType;
        this.moves = moves;
        this.level = level;
        this.exp = exp;
        loadGender();
        while(moves.size() != 4){
            moves.add(moveList("Empty"));
        }
        this.currentHp = tempStats.get(moveClass.Stat.HIT_POINTS.getValue());
        condition = null;
    }

    /**
     * Empty constructor for a mob
     */
    public mobEnums(){
        this.stats = new int[Torture.statLength];
        this.ev = new int[Torture.statLength];
        genIVsRandom();
    }

    public void genIVsRandom() {
        if(iv == null){
            iv = new int[Torture.statLength];
        }
        for(int i = 0; i < iv.length; i++) {
            iv[i] = randomScripts.getRandomNumber(0, 31); // 0 to 31 inclusive
        }
    }

    public void genIVsBreeding(mobEnums parent1, mobEnums parent2) {
        Random rand = new Random(System.currentTimeMillis());
        ArrayList<Integer> inheritedIndices = new ArrayList<>();

        // Choose 3 random indices to inherit from parents
        while(inheritedIndices.size() < 3) {
            int statIndex = rand.nextInt(6);
            if(!inheritedIndices.contains(statIndex)) {
                // Randomly pick which parent for this stat
                iv[statIndex] = rand.nextBoolean() ? parent1.iv[statIndex] : parent2.iv[statIndex];
                inheritedIndices.add(statIndex);
            }
        }

        // Remaining stats are random
        for(int i = 0; i < 6; i++) {
            if(!inheritedIndices.contains(i)) {
                iv[i] = rand.nextInt(32);
            }
        }
    }


    public void setNextMove(Object o){this.nextMove = o;}
    public Object getNextMove(){return this.nextMove;}
    public char getGender(){return this.gender;}
    public char getGenderAsSymbol(){
        return switch (gender){
            case 'm', 'M' -> '♂';
            case 'f', 'F' -> '♀';
            default -> '?';
        };
    }
    public TextColor getGenderAsColor(){
        return switch (gender){
            case 'm', 'M' -> TextColor.color(14, 104, 173);
            case 'f', 'F' -> TextColor.color(235, 12, 220);
            default -> NamedTextColor.WHITE;
        };
    }
    public void setGender(char c){this.gender = c;}
    public void loadGender(){
        if(randomScripts.getRandomNumber(0, 1) == 0){
               gender = 'M';
        }else{
            gender = 'F';
        }
    }
    public boolean hasTrainer(){return trainer != null;}

    /**
     *
     * @param mobStatsToSet the new stats for the mob
     */
    public void setStats(int[] mobStatsToSet){ this.stats = mobStatsToSet;}

    /**
     *
     * @return returns the ID of the mob
     */
    public UUID getIdent(){return this.ident;}

    /**
     *
     * @param name1 sets the display name of the mob
     */
    public void setName(String name1){ this.name = name1;}

    /**
     *
     * @return the number slot of the item in the menu
     */
    public int getSlotInMenu(){return this.slotInMenu;}

    /**
     *
     * @return the integer slot of the item in the PC
     */
    public int getSlotInPc(){return this.slotInPc;}
    public void setSlotInMenu(int slot){this.slotInMenu = slot;} public mobEnums setSlotInPc(int slot){this.slotInPc = slot; return this;}

    /**
     *
     * @return the mob's name
     */
    public String getName(){return this.name; }
    public String getNickname(){return this.nickname != null ? this.nickname : name;}
    /**
     *
     * @param ca new catch resistance
     */
    public void setCatchResilience(int ca){this.catchResiliance = ca;}
    /**
     *
     * @return catch resistance of the mob
     */
    public int getCatchResilience(){return this.catchResiliance;}
    /**
     *
     * @param ability1 sets the mob's ability
     */
    public void setAbility(Ability ability1){this.ability = ability1;}
    /**
     *
     * @return returns the mob's stats
     */
    public int[] getStats(){
        return this.stats;
    }
    /**
     *
     * @return the mob's ability
     */
    public Ability getAbilities(){return this.ability;}
    /**
     *
     * @return all the mob's possible abilities
     */
    public Ability[] getAllPosAbilities(){return mobDex.allPosAbilities(this.dexNum);}
    /**
     *
     * @param dexNum new mob dex number
     */
    public void setDexNum(int dexNum){this.dexNum = dexNum;}
    /**
     *
     * @return the mob dex number
     */
    public int getDexNum(){return this.dexNum; }
    /**
     *
     * @return the ball the mob is caught in
     */
    public ItemClass getMobBall(){return this.mobBall;}
    /**
     *
     * @param mobBall the mob ball for the mob to be caught in
     */
    public void setMobBall(ShulkerItem mobBall){this.mobBall = mobBall;}
    /**
     *
     * @param newIcon the display icon for the mob to be shown as
     */
    public void setIcon(ItemStack newIcon){this.icon = newIcon; }
    /**
     *
     * @return the ID of the opponent mob
     */
    public int getOpponentId(){return this.opponentId;}
    /**
     *
     * @return the display icon of the mob
     */
    public ItemStack getIcon(){return this.icon; }
    /**
     *
     * @return the mojang entity type of the mob
     */
    public EntityType getMojangMobType(){return this.mojangMobType; }
    /**
     *
     * @return the mob's trainer
     */
    public humanClass.Trainer getTrainer(){return this.trainer;}
    /**
     * Trainers are used in battle
     * @param trainer sets the mob's trainer
     */
    public void setTrainer(humanClass.Trainer trainer){this.trainer = trainer;}
    /**
     *
     * @param mob an instance of the opponent's mob
     * @return an instance of this class
     */
    public mobEnums setOpponentMob(mobEnums mob){this.opponentMob = mob; this.opponentId = mob.getBattleSpot(); return this;}
    /**
     *
     * @return opponent mob
     */
    public mobEnums getOpponentMob(){return this.opponentMob;}
    /**
     *
     * @param newMojangMobType
     */
    public void setType(EntityType newMojangMobType){this.mojangMobType = newMojangMobType; }

    public void setMove(moveClass move, int num){this.moves.set(num, move); }

    public void setAllMoves(List<moveClass> moveNames){int i = 0; for(moveClass move : moveNames){this.moves.set(i, move); i++;}}
    public moveClass getRandomMove(){
        moveClass ranMove = this.moves.get(randomScripts.getRandomNumber(0, (this.moves.size()-1)));
        while(ranMove.getMoveName().equalsIgnoreCase("Empty")){
            ranMove = this.moves.get(randomScripts.getRandomNumber(0, (this.moves.size()-1)));
        }
        return ranMove;
    }

    /**
     *
     * @param num the move number to return
     * @return the move at the specified number
     */
    public moveClass getMove(int num){if(num < 4) return this.moves.get(num); return null;}
    /**
     *
     * @return the mob's moves
     */
    public List<moveClass> getMoves(){return this.moves; }
    /**
     *
     * @param level the mob's new level
     */
    public void setLevel(int level){this.level = level; }
    public Pair<MHTypes, MHTypes> getType(){
        return  mobDex.getType(dexNum);
    }

    /**
     *
     * @return the mob's current level
     */
    public int getLevel(){return this.level; }

    /**
     *
     * @return the mob's current hp
     */
    public int getCurrentHp(){return this.currentHp;}

    /**
     *
     * @param hp the mob's new HP
     */
    public void setCurrentHp(int hp){this.currentHp = hp;}

    /**
     *
     * @return the amount of exp the mob has
     */
    public int getExp(){return this.exp;}

    /**
     *
     * @param earnType the enum representing the speed the mob earns experience at
     */
    public void setExpEarnType(@Nonnull ExpEarnType earnType){this.expEarnType = earnType;}
    public ExpEarnType getExpEarnType(){return this.expEarnType;}

    /**
     *
     * @return the entity that is representing the mob in the world
     */
    @Nullable
    public Entity getSelf(){return thisEntity;}

    /**
     *
     * @param entity
     * @return the new instance of this class
     */
    public mobEnums setSelf(@Nonnull Entity entity){thisEntity = entity; this.selfUUID = entity.getUniqueId(); Bukkit.getPlayer("CrotaPlague").sendMessage("IMPORTANT, self change"); return this;}
    public UUID getSelfUUID(){return this.selfUUID;}
    public void setSelfUUID(UUID id){this.selfUUID = id;}
    public Nature getNature(){return this.nature;}
    public specialConditions getCondition(){return this.condition;}
    public void setCondition(specialConditions condition){this.condition = condition;}
    public void setBaseExpDrop(int exp){this.baseExpDrop = exp;}
    public int getBaseExpDrop(){return this.baseExpDrop;}
    public int getBattleSpot(){return this.battleSpot;}
    public void setBattleSpot(int newSpot){this.battleSpot = newSpot;}
    public int[] getStages(){return this.stages;}
    public void changeStage(moveClass.Stat stat, int amount){this.stages[stat.getValue()] = this.stages[stat.getValue()] + amount; Bukkit.getPlayer("CrotaPlague").sendMessage("The mob: " + this + " and change " + amount);}
    public void setEv(int[] ev){this.ev = Arrays.copyOf(ev, ev.length);}
    public void setIv(int[] iv){this.iv = Arrays.copyOf(iv, iv.length);}
    public void setNickname(String s){this.nickname = s;}
    @Override
    public Map<String, Object> serialize() {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("stats", stats);
        result.put("evs", ev);
        result.put("iv", iv);
        result.put("name", name);
        result.put("ability", ability.name());
        result.put("dexNum", Integer.valueOf(dexNum));
        result.put("icon", icon.serialize());
        result.put("MobType", mojangMobType.toString());
        int i = 0;
        while(i<moves.size()){
            if(i>=moves.size()) break;
            result.put("move " + i, moves.get(i).getMoveName());

            i++;
        }
        result.put("level", level);
        result.put("exp", this.exp);
        result.put("currentHp", this.currentHp);
        if(this.condition != null)
        result.put("condition", condition.serialize());
        result.put("slotInPc", this.slotInPc);
        result.put("mobBall", this.mobBall.serialize());
        result.put("gender", String.valueOf(this.gender));
        if(this.nickname != null){
            result.put("nickname", this.nickname);
        }

        return result;
    }
    @SuppressWarnings("unchecked")
    public static mobEnums deserialize(MemorySection memorySection) {
        return deserialize(memorySection.getValues(false));
    }

    @SuppressWarnings("unchecked")
    public static mobEnums deserialize(Map<String, Object> map) {
        // Convert List<Integer> back to int[] for stats
        List<Integer> list = (List<Integer>) map.get("stats");
        int[] stats = list.stream().mapToInt(Integer::intValue).toArray();
        final String name = (String) map.get("name");

        final Ability ability;
        if (map.get("ability") instanceof String) {
            ability = Ability.valueOf((String) map.get("ability"));
        } else {
            ability = (Ability) map.get("ability");
        }

        final int dexNum = (int) map.get("dexNum");

        final ItemStack icon;
        Object iconObj = map.get("icon");
        if (iconObj instanceof MemorySection) {
            MemorySection ms = (MemorySection) iconObj;
            icon = ItemStack.deserialize(ms.getValues(false));
        } else {
            icon = ItemStack.deserialize((Map<String, Object>) iconObj);
        }

        final EntityType mojangMobType = EntityType.valueOf((String) map.get("MobType"));

        List<moveClass> moves = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            if(map.containsKey("move " + i)){
                String moveName = map.get("move " + i).toString();
                moves.add(mobDex.moveList(moveName));
            }
        }

        final int level = (int) map.get("level");
        final int exp = (int) map.get("exp");

        mobEnums mob = new mobEnums(stats, name, ability, dexNum, icon, mojangMobType, moves, level, exp);
        if(map.containsKey("evs")){
            list = (List<Integer>) map.get("evs");
            int[] ev = list.stream().mapToInt(Integer::intValue).toArray();
            mob.setEv(ev);
        }else{
            mob.setEv(new int[Torture.statLength]);
        }
        if(map.containsKey("iv")){
            list = (List<Integer>) map.get("iv");
            int[] iv = list.stream().mapToInt(Integer::intValue).toArray();
            mob.setIv(iv);
        }else{
            mob.genIVsRandom();
        }
        if(map.containsKey("nickname")){
            String nickname = (String) map.get("nickname");
            mob.setNickname(nickname);
        }
        if (map.containsKey("mobBall")) {
            Object mobBallObj = map.get("mobBall");
            if (mobBallObj instanceof MemorySection) {
                MemorySection ms = (MemorySection) mobBallObj;
                mob.setMobBall(ShulkerItem.deserialize(ms.getValues(false)));
            } else {
                mob.setMobBall(ShulkerItem.deserialize((Map<String, Object>) mobBallObj));
            }
        } else {
            mob.setMobBall(TItemManager.defaultCatchDefault);
        }

        if (map.get("condition") != null)
            mob.setCondition((specialConditions) map.get("condition"));

        if (map.containsKey("slotInPc"))
            mob.setSlotInPc((int) map.get("slotInPc"));
        if(map.containsKey("gender")){
            Character gender = map.get("gender").toString().charAt(0);
            mob.setGender(gender);
        }else{
            mob.loadGender();
        }
        return mob;
    }

    public int getPhysicalAttackPower() {
        return calculateStat(moveClass.Stat.ATTACK);
    }

    public int getRangedAttackPower() {
        return calculateStat(moveClass.Stat.RANGED_ATTACK);
    }

    public int calculateStat(moveClass.Stat stat) {
        int index = stat.getValue();

        int base = stats[index];
        int indiv = iv[index];
        int effort = ev[index];

        int statValue = (((base * 2 + indiv + (effort / 4)) * level) / 100) + 5;

        // Apply nature modifier
        if(nature != null) {
            statValue = (int) Math.ceil(statValue * nature.getModifier(stat));
        }

        // Apply stage multiplier
        int stage = stages[index];
        double stageMultiplier = getStageMultiplier(stage, stat);

        statValue = (int) Math.ceil(statValue * stageMultiplier);

        return statValue;
    }




    public enum Ability{
        SHARP_SHOOTER, INTIMIDATE, THIN;
    }

    public int getMaxHp(){
        return stats[5];
    }


    /**
     * Creates a copy of this mobEnums using its copy constructor.
     * Does not use Object.clone().
     */
    @Override
    public mobEnums clone(){
        return new mobEnums(this);
    }

    @Override
    public int hashCode(){
        return ident.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof mobEnums)){return false;}
        mobEnums other = (mobEnums) o;
        if(!(this.ident.equals(other.ident))){return false;}
        if(this.dexNum != other.dexNum){return false;}
        if(this.level != other.level){return false;}
        if(this.exp != other.exp){return false;}
        if(!this.name.equals(other.name)){return false;}
        if(!Objects.equals(selfUUID, other.selfUUID)){return false;}

        return true;
    }

    @Override
    public String toString(){
        return "Lv. " + level + " " + this.name + " ";
    }

    private double getStageMultiplier(int stage, moveClass.Stat stat) {
        // Accuracy and Evasiveness have different calculations
        if(stat == moveClass.Stat.ACCURACY || stat == moveClass.Stat.EVASIVENESS) {
            if(stage >= 0) {
                return (3.0 + stage) / 3.0;
            } else {
                return 3.0 / (3.0 - stage);
            }
        } else {
            if(stage >= 0) {
                return (2.0 + stage) / 2.0;
            } else {
                return 2.0 / (2.0 - stage);
            }
        }
    }


}