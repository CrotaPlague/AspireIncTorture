package com.crotaplague.torture.Files.ServerStorage.humans;

import com.crotaplague.torture.Files.ServerStorage.SaveFile;
import com.crotaplague.torture.Files.ServerStorage.battleClass;
import com.crotaplague.torture.Files.ServerStorage.battleTypes;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.crotaplague.torture.Files.ServerScriptService.randomScripts.getPlayerSaveFile;
import static com.crotaplague.torture.Torture.plugin;

/**
 * Represents a generic non-player character (NPC) that can be interacted with in the game.
 * NPCs typically have a name, an associated entity, and one or more combat "mobs".
 */
interface NPC{
    /** @return the NPC's display name */
    String getName();
    /** @return the underlying {@link LivingEntity} for this NPC */
    LivingEntity getMob();
    /** @return the list of {@link mobEnums} associated with this NPC */
    List<mobEnums> getMobs();
    /** @return the {@link Location} of this NPC in the world */
    Location getLocation();
}
/**
 * Represents a human NPC in the game world. A {@code humanClass} instance can serve
 * as a town NPC, trainer, or AI-controlled opponent.
 * <p>
 * Human NPCs can hold references to {@link mobEnums} that represent their team, an associated
 * {@link LivingEntity} in the world, and contextual data such as dialogue phrases,
 * rewards, and type classification.
 */
public class humanClass implements ConfigurationSerializable, NPC{

    protected String name;
    protected List<mobEnums> mobs;
    private String phrase;
    private Integer boxNum = -1;
    protected LivingEntity thisMob;
    private boolean raycast = false;
    private int NPCType = 1; // 1: Town Npc 2: Trainer,
    private battleTypes battleType;
    private int cashReward = 0;
    protected char displaySlot = 'a';
    protected int dexNum;
    /**
     * Copy constructor.
     * <p>
     * Creates a new {@code humanClass} instance by copying the properties
     * of an existing one. This performs a shallow copy — object references
     * (like {@code mobs} and {@code thisMob}) are shared, not cloned.
     *
     * @param h the existing {@code humanClass} instance to copy from
     */
    public humanClass(humanClass h){
        this.name = h.name;
        this.mobs = h.mobs;
        this.phrase = h.phrase;
        this.boxNum = h.boxNum;
        this.thisMob = h.thisMob;
        this.NPCType = h.NPCType;
        this.battleType = h.battleType;
        this.cashReward = h.cashReward;
    }
    /**
     * Constructs a new {@code humanClass} with the given name.
     * <p>
     * The NPC will have an empty mob list until one is added later.
     *
     * @param name the display name or identifier for this NPC
     */
    public humanClass(String name){
        this.name = name;
        this.mobs = new ArrayList<>();
    }
    /**
     * Constructs a new {@code humanClass} with detailed initialization.
     * <p>
     * This version allows specifying the NPC’s name, roster, entity, dialogue, and storage box number.
     *
     * @param name   the NPC’s display name
     * @param mobs   the list of mobs associated with the NPC
     * @param entity the in-world {@link LivingEntity} representing the NPC
     * @param phrase the dialogue phrase or text the NPC will say
     * @param boxNum the storage box or roster index for this NPC
     */
    public humanClass(String name, List<mobEnums> mobs, LivingEntity entity, String phrase, Integer boxNum){
        this.name = name;
        this.mobs = mobs;
        this.phrase = phrase;
        this.boxNum = boxNum;
        this.thisMob = entity;
    }
    /**
     * Constructs a new {@code humanClass} using an existing {@link LivingEntity},
     * a list of mobs, and a display name.
     * <p>
     * This is commonly used when generating NPCs dynamically based on an existing
     * in-world entity and pre-defined mob team.
     *
     * @param entity   the in-world {@link LivingEntity} representing the NPC
     * @param entMobs  the list of {@link mobEnums} associated with this NPC
     * @param name     the NPC’s display name
     */
    public humanClass(LivingEntity entity, List<mobEnums> entMobs, String name) {
        this.thisMob = entity;
        this.mobs = entMobs;
        this.name = name;
    }
    /**
     * Constructs a new {@code humanClass} with full battle and reward data.
     * <p>
     * This version is typically used for trainer NPCs or battle-enabled entities
     * where cash rewards and roster positions are relevant.
     *
     * @param entity      the in-world {@link LivingEntity} representing the NPC
     * @param entMobs     the list of {@link mobEnums} associated with this NPC
     * @param name        the NPC’s display name
     * @param cashReward  the amount of cash players receive for defeating this NPC
     * @param boxNum      the NPC’s storage box or internal roster number
     */
    public humanClass(LivingEntity entity, List<mobEnums> entMobs, String name, int cashReward, int boxNum) {
        this.thisMob = entity;
        this.mobs = entMobs;
        this.name = name;
        this.cashReward = cashReward;
        this.boxNum = boxNum;
    }
    /**
     * Gets the internal reference number for this NPC's roster entry.
     * <p>
     * This value identifies the NPC's position or reference within its trainer’s
     * or system’s internal roster, but is not a Pokédex or external identifier.
     *
     * @return the internal roster reference number
     */
    public int getDexNum() { return this.dexNum; }

    /**
     * Sets the internal reference number for this NPC's roster entry.
     * <p>
     * When this is called, all mobs associated with the NPC are also
     * linked to a new {@link Trainer} instance, ensuring they correctly
     * reference their owning trainer.
     *
     * @param a the internal roster reference number to assign
     */
    public void setDexNum(int a) {
        this.dexNum = a;
        new Trainer(this).claimMobs();
    }

    /**
     * Gets the current {@link Location} of this NPC’s in-world entity.
     *
     * @return the NPC’s location
     */
    public Location getLocation() { return this.thisMob.getLocation(); }

    /**
     * Enables or disables raycasting behavior for this NPC.
     *
     * @param bool {@code true} to enable raycasting; {@code false} to disable
     */
    public void setRaycasting(Boolean bool) { this.raycast = bool; }

    /**
     * Gets the list of mobs associated with this NPC.
     *
     * @return a list of {@link mobEnums} belonging to this NPC
     */
    public List<mobEnums> getMobs() { return this.mobs; }

    /**
     * Checks whether raycasting is enabled for this NPC.
     *
     * @return {@code true} if raycasting is enabled; otherwise {@code false}
     */
    public Boolean getRaycast() { return this.raycast; }

    /**
     * Sets the display name of this NPC.
     *
     * @param name the new name to assign
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the display name of this NPC.
     *
     * @return the NPC’s name
     */
    public String getName() { return this.name; }

    /**
     * Gets the dialogue phrase or line associated with this NPC, if any.
     *
     * @return the NPC’s phrase, or {@code null} if none is set
     */
    @Nullable
    public String getPhrase() { return this.phrase; }

    /**
     * Sets the dialogue phrase or line that this NPC will say.
     *
     * @param newPhrase the new phrase to assign
     */
    public void setPhrase(String newPhrase) { this.phrase = newPhrase; }

    /**
     * Gets the {@link LivingEntity} representing this NPC in the game world.
     *
     * @return the NPC’s {@link LivingEntity}
     */
    public LivingEntity getMob() { return this.thisMob; }

    /**
     * Sets the type of this NPC.
     * <p>
     * Example values:
     * <ul>
     *     <li>1 — Town NPC</li>
     *     <li>2 — Trainer NPC</li>
     * </ul>
     *
     * @param NPCType the NPC type identifier
     */
    public void setType(int NPCType) { this.NPCType = NPCType; }

    /**
     * Gets the type of this NPC.
     *
     * @return the NPC type identifier (e.g. 1 = Town NPC, 2 = Trainer)
     */
    public int getType() { return this.NPCType; }

    /**
     * Gets the box number associated with this NPC.
     * <p>
     * This typically represents the NPC’s storage slot or related configuration ID.
     *
     * @return the box number, or {@code -1} if none is assigned
     */
    public Integer getBoxNum() { return this.boxNum; }

    /**
     * Sets the underlying {@link LivingEntity} instance for this NPC.
     *
     * @param entity the entity to associate
     */
    public void setThisMob(LivingEntity entity) { this.thisMob = entity; }

    /**
     * Replaces the list of mobs associated with this NPC.
     *
     * @param newMobs a list of {@link mobEnums} to assign
     */
    public void setMobs(List<mobEnums> newMobs) { this.mobs = newMobs; }

    /**
     * Sets the cash reward a player receives after defeating this NPC in battle.
     *
     * @param cash the cash reward amount
     */
    public void setCashReward(int cash) { this.cashReward = cash; }

    /**
     * Gets the cash reward a player receives after defeating this NPC.
     *
     * @return the cash reward amount
     */
    public int getCashReward() { return this.cashReward; }

    /**
     * Sets this NPC’s internal {@link LivingEntity} reference.
     * <p>This is functionally equivalent to {@link #setThisMob(LivingEntity)}.</p>
     *
     * @param entity the entity to assign
     */
    public void setSelf(LivingEntity entity) { this.thisMob = entity; }

    /**
     * Sets the display slot used for positioning or UI display purposes
     * (for example, in battle or selection menus).
     *
     * @param c the display slot character (e.g. 'a' or 'b')
     */
    public void setDisplaySlot(char c) { this.displaySlot = c; }

    /**
     * Gets the display slot character used for positioning or UI purposes.
     *
     * @return the display slot character (e.g. 'a' or 'b')
     */
    public char getDisplaySlot() { return this.displaySlot; }

    public void setMobEntity(LivingEntity liv){
        for(mobEnums mob : mobs){
            mob.getTrainer().setSelf(liv);
        }
    }
    /**
     * Converts this NPC into an AI-controlled {@link aiTrainer} version.
     *
     * @return an {@link aiTrainer} copy of this NPC
     */
    public aiTrainer toAi(){
        aiTrainer train = new aiTrainer(thisMob, mobs, name, cashReward, boxNum);
        train.setPhrase(this.phrase);
        return train;
    }
    /**
     * Sets or replaces a mob at a given team index.
     *
     * @param index  the team slot index
     * @param newMob the mob to place at that slot
     */
    public void setMob(final int index, final mobEnums newMob){
        if(this.mobs.size() <= index){
            for(int i = 0; i<=index; i++){
                mobs.add(null);
            }
        }
        this.mobs.set(index, newMob);
    }


    /**
     * Represents a human NPC acting as a battle trainer.
     * Trainers can hold teams of {@link mobEnums}, manage battle states,
     * and spawn in-world interactive UI elements (like option stands).
     */
    public static class Trainer extends humanClass implements NPC{
        private Boolean ready = false;
        private mobEnums currentMob;
        private List<mobEnums> mobsInPlay;
        private Entity currentMobEnt;
        private int AiTrainerNum;
        private Location falseLoc = null;
        private UUID thisIsStupidAF;
        private UUID thisIsAlsoStupidAF;
        private mobEnums currentSelecting;

        /**
         * Copy constructor for {@link Trainer}.
         * <p>
         * Creates a new {@code Trainer} by copying all properties from another
         * {@code Trainer} instance. This performs a shallow copy; object references
         * like {@code mobs} and {@code thisMob} are shared between the two instances.
         *
         * @param t the {@code Trainer} instance to copy
         */
        public Trainer(Trainer t){
            super(t);
            this.mobs = t.mobs;
            this.thisMob = t.thisMob;
            this.ready = t.ready;
            this.currentMob = t.currentMob;
            this.mobsInPlay = t.mobsInPlay;
            this.currentMobEnt = t.currentMobEnt;
            this.AiTrainerNum = t.AiTrainerNum;
            this.falseLoc = t.falseLoc;
            this.thisIsStupidAF = t.thisIsStupidAF;
            this.thisIsAlsoStupidAF = t.thisIsAlsoStupidAF;
            this.currentSelecting = t.currentSelecting;
        }
        /**
         * Constructs a new {@code Trainer} using an in-world entity, a list of mobs, and a name.
         * <p>
         * This is typically used when creating a trainer NPC dynamically in the world
         * with a predefined mob team.
         *
         * @param entity the {@link LivingEntity} representing this trainer in the world
         * @param mobs   the list of {@link mobEnums} that belong to this trainer
         * @param name   the display name of the trainer
         */
        public Trainer(LivingEntity entity, List<mobEnums> mobs, String name){
            super(entity, mobs, name);
            this.mobs = mobs;
            this.thisMob = entity;
        }
        /**
         * Constructs a new {@code Trainer} from an existing {@link humanClass} instance.
         * <p>
         * This constructor initializes a trainer based on the NPC data in {@code humanClass},
         * copying over the entity, mob list, name, cash reward, and roster/box number.
         * It also initializes the trainer-specific fields such as {@code mobsInPlay} and
         * {@code currentSelecting}.
         *
         * @param h the {@code humanClass} instance from which to create this trainer
         */
        public Trainer(humanClass h){
            super(h.thisMob, h.mobs, h.name, h.cashReward, h.boxNum);
            if(this.name == null || this.name.isEmpty() && thisMob != null){this.name = thisMob.getName();}
            mobsInPlay = new ArrayList<>();
            currentSelecting = null;
            this.dexNum = h.dexNum;
        }
        /** @return the first mob with HP above 0, or {@code null} if all are fainted */
        public mobEnums getFirstMobAlive(){
            for(mobEnums m : mobs){
                if(m.getCurrentHp() > 0){
                    return m;
                }
            }
            return null;
        }
        /**
         * Retrieves the second alive mob in the trainer's roster.
         * <p>
         * Iterates through the trainer's {@code mobs} list in order, counting
         * only those with {@code currentHp > 0}. Returns the second mob found
         * that is still alive. If fewer than two mobs are alive, this method
         * returns {@code null}.
         *
         * @return the second alive {@link mobEnums} in the roster, or {@code null} if none exists
         */
        public mobEnums getSecondMobAlive(){
            int count = 0;
            for(mobEnums m : mobs){
                if(m.getCurrentHp() > 0){
                    count++;
                    if(count == 2){
                        return m;
                    }
                }
            }
            return null;
        }
        /**
         * Assigns this trainer as the owner of all mobs in their roster.
         * <p>
         * Iterates through the trainer's {@code mobs} list and sets each
         * mob's trainer reference to {@code this}.
         */
        public void claimMobs() {
            for (mobEnums mob : mobs) {
                mob.setTrainer(this);
            }
        }
        /**
         * Returns the readiness status of this trainer.
         *
         * @return {@code true} if the trainer is ready, {@code false} otherwise
         */
        public Boolean getStatus() {
            return this.ready;
        }
        /**
         * Sets the readiness status of this trainer.
         *
         * @param ready the new readiness status to set
         */
        public void setStatus(Boolean ready) {
            this.ready = ready;
        }
        /**
         * Sets the current active mob entity controlled by this trainer.
         *
         * @param currentMobEnt the entity to set as the current mob
         */
        public void setCurrentMobEnt(Entity currentMobEnt) {
            this.currentMobEnt = currentMobEnt;
        }

        /**
         * Retrieves the current active mob entity controlled by this trainer.
         * <p>
         * This corresponds to the primary mob in play for the trainer.
         *
         * @return the {@link Entity} representing the trainer's current active mob
         */
        public Entity getCurrentMobEnt() {
            return this.getPrimaryMob().getSelf();
        }
        public mobEnums getPrimaryMob(){return mobsInPlay.get(0);}
        public Trainer setPrimaryMob(mobEnums mob){this.currentMob = mob; this.setCurrentMobEnt(mob.getSelf());return this;}
        public int getAiTrainerNum(){return this.AiTrainerNum;}
        public LivingEntity getSelf(){return this.thisMob;}
        public boolean hasFalseLoc(){if(falseLoc == null) return false; else return true;}
        public Location getFalseLoc(){return falseLoc;}
        public void setFalseLOc(Location falsel){this.falseLoc = falsel;}
        public void setStupid(UUID var){this.thisIsStupidAF = var;}
        public UUID getStupid(){return this.thisIsStupidAF;}
        public void setAlsoStupid(UUID var){this.thisIsAlsoStupidAF = var;}
        public UUID getAlsoStupid(){return this.thisIsAlsoStupidAF;}
        public List<mobEnums> getCurrentMobs(){return mobsInPlay;}
        public void replaceMobInPlay(mobEnums from, mobEnums to){mobsInPlay.replaceAll(f -> f.equals(from) ? to : from);}
        public boolean isInPlay(mobEnums mob){return mobsInPlay.contains(mob);}
        public void setMobsInPlay(List<mobEnums> ms){this.mobsInPlay = new ArrayList<>(ms);}
        public void addMobInPlay(mobEnums mob){
            if(mobsInPlay == null){mobsInPlay = new ArrayList<>();} if(!mobsInPlay.contains(mob)){this.mobsInPlay.add(mob);}}
        public mobEnums getCurrentSelecting(){return this.currentSelecting;}
        public void setCurrentSelecting(mobEnums currentSelecting){this.currentSelecting = currentSelecting; thisMob.sendMessage("Changing selection: " + currentSelecting);}
        public void clearMobMoves(){for(mobEnums m : mobs){if(m != null) m.setNextMove(null);}}
        public int nextMoveSwaps(){int i = 0; for(mobEnums m : mobs){if(m.getNextMove() instanceof mobEnums) i++;} return i;}
        public boolean isMobOf(Entity ent){
            for(mobEnums current : mobsInPlay){
                if(current.getSelf() != null && current.getSelf().getUniqueId().equals(ent.getUniqueId()) ){
                    return true;
                }
            }
            return false;
        }
        public boolean hasDeadActiveMob(){
            for(mobEnums m : mobsInPlay){
                if(m.getCurrentHp() < 1){return true;}
            }
            return false;
        }
        public Set<mobEnums> getDeadMobs(){Set<mobEnums> dead = new HashSet<>(); for(mobEnums m : mobsInPlay){if(m.getCurrentHp() < 1) dead.add(m);} return dead;}
        public int numDeadActive(){int i = 0; for(mobEnums m : mobsInPlay){if(m.getCurrentHp() < 1) i++;} return i;}
        public void displayOptions(battleClass battle) {
            NamespacedKey nameKey = new NamespacedKey(plugin, "BattleOptionName");
            NamespacedKey key = new NamespacedKey(plugin, "BattleOptions");
            World w = Torture.world;
            List<Location> dl = this.displaySlot == 'a' ? battle.getBox().getChoicesA() : battle.getBox().getChoicesB();
            String[] strs = new String[]{"Fight", "Bag", "Mobs", "Run"};
            Map<Integer, Integer> pairings = new HashMap<>();
            pairings.put(1, 2);
            pairings.put(2, 1);
            pairings.put(3, 4);
            pairings.put(4, 3);
            List<ArmorStand> stands = new ArrayList<>();
            List<MagmaCube> cubes = new ArrayList<>();
            SaveFile file = getPlayerSaveFile((Player) this.getSelf());
            int count = 1;
            for(Location l : dl){
                Location copy = l.clone();
                if(count == 1 || count == 3){
                    copy = copy.add(new Vector(0, 0, 0.2));
                }
                MagmaCube c = (MagmaCube) w.spawnEntity(copy, EntityType.MAGMA_CUBE, CreatureSpawnEvent.SpawnReason.CUSTOM, cube -> {cube.setInvulnerable(true); cube.setSilent(true); ((MagmaCube) cube).setSize(2); ((MagmaCube) cube).setAI(false);
                    cube.setGravity(false); cube.setVisibleByDefault(false);});
                c.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, pairings.get(count++));
            }
            AtomicInteger i2 = new AtomicInteger(0);
            for(Location l : dl){
                l.add(new Vector(0, -1.7, 0));
                ArmorStand stand = (ArmorStand) w.spawnEntity(l, EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM, s -> {s.setInvulnerable(true); s.setVisibleByDefault(false); s.setGravity(false); s.setCustomNameVisible(true);
                    s.customName(Component.text(strs[i2.get()])); s.setSilent(true); s.setInvisible(true);});
                ((Player) this.getSelf()).showEntity(plugin, stand);
                this.getSelf().sendMessage("The location: " + stand.getLocation());
                stand.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, strs[i2.getAndIncrement()]);
                file.addArmorStand(stand);
            }
        }

        public boolean isOutOfMobs(){

            for(mobEnums mob : mobs) if(mob.getCurrentHp() > 0) return false;

            return true;
        }
    }
    /**
     * Represents an AI-controlled variant of a {@link Trainer}, capable of
     * making autonomous combat decisions using randomized move selection.
     */
    public static class aiTrainer extends Trainer implements NPC{
        private String phrase;
        private mobEnums currentMob;
        public aiTrainer(LivingEntity entity, List<mobEnums> mobs, String name, int cashReward, int boxNum) {
            super(entity, mobs, name);
        }
        public moveClass getRandomMove(){
            return this.getPrimaryMob().getRandomMove();
        }
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("NPCName", this.name);
        int iteration = 0;
        for(mobEnums mob : this.mobs){
            if(mob == null){
                result.put("NPCMob " + iteration, null);
            }else{
                result.put("NPCMob " + iteration, mob.serialize());

            }
            iteration++;
        }
        result.put("phrase", this.phrase);
        result.put("boxNum", this.boxNum);
        result.put("raycasting", this.raycast);
        result.put("NPCType", this.NPCType);
        result.put("cashReward", this.cashReward);
        if(this.battleType != null){
            result.put("battleType", this.battleType);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public static humanClass deserialize(Map<String, Object> map){
        String name = (String) map.get("NPCName");

        humanClass human = new humanClass(name);
        if(map.get("boxNum") != null) human.boxNum = (Integer) map.get("boxNum");
        int iteration = 0;
        List<mobEnums> mobs = new ArrayList<>();
        human.setRaycasting((Boolean) map.get("raycasting"));
        human.setCashReward((int)map.get("cashReward"));
        if(map.get("NPCType") != null) human.setType((int)map.get("NPCType"));
        if(map.containsKey("phrase")) human.setPhrase((String) map.get("phrase"));
        while(map.containsKey("NPCMob " + iteration)){
            MemorySection memorySection = (MemorySection) map.get("NPCMob " + iteration);
            mobEnums m = mobEnums.deserialize(memorySection);
            m.setTrainer(new Trainer(human));
            mobs.add(m);
            iteration++;
        }
        human.setMobs(mobs);
        Trainer t = new Trainer(human);
        for(mobEnums m : mobs){
            m.setTrainer(t);
        }
        return human;
    }

}
