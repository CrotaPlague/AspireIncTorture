package com.crotaplague.torture.Files.ServerStorage.AnimationParts;

import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Torture;
import com.destroystokyo.paper.entity.Pathfinder;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * Handles the execution and data management of a single animation action.
 * <p>
 * Each {@code AnimationManager} instance represents one discrete animation
 * component, such as moving an entity to a target location, applying a disguise,
 * or teleporting the player's camera. Animations can include additional metadata
 * like comments, linked IDs, or simultaneous execution flags, and can be serialized
 * for persistent storage.
 * <p>
 * This class extends {@link BukkitRunnable} to allow animations to be scheduled and
 * run asynchronously in the Bukkit scheduler, and implements
 * {@link ConfigurationSerializable} for configuration-based saving and loading.
 */

public class AnimationManager extends BukkitRunnable implements ConfigurationSerializable {
    public Location goal = null;
    public Entity subject;
    private String text;
    private Location endFace = null;
    private boolean runSimul = false;
    String disguise = null;
    private Location output;
    String comments = null;
    private UUID id; // The ID of the animation mob was spawned in
    private UUID tie;
    private boolean cameraTp = false;

    @Override
    public void run() {
        if (goal != null && subject instanceof Mob && disguise == null) {
            Mob m = (Mob) subject;
            NamespacedKey key = new NamespacedKey(Torture.plugin, "AnimationMob");
            m.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, new int[]{goal.getBlockX(), goal.getBlockZ()});
            Runnable navigate = new Runnable() {
                @Override
                public void run() {
                    Pathfinder pf = m.getPathfinder();
                    boolean started = pf.moveTo(goal, 0.6);
                    if (!started) {

                        Bukkit.getScheduler().scheduleSyncDelayedTask(Torture.plugin, this, 1L);
                    }
                }
            };
            // Schedule at next tick for proper Pathfinder initialization
            Bukkit.getScheduler().scheduleSyncDelayedTask(Torture.plugin, navigate, 1L);
        }


        if(goal != null && disguise != null){
            World w = goal.getWorld();
            subject = w.spawnEntity(goal, EntityType.VILLAGER);
            Mob m = (Mob) subject;
            NamespacedKey key = new NamespacedKey(Torture.plugin, "AnimationMob");
            m.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, new int[]{goal.getBlockX(), goal.getBlockZ()});
            try {
                DisguiseAPI.addCustomDisguise("temp", disguise);
            }catch(Exception ignored){}
            Disguise d = DisguiseAPI.getCustomDisguise("temp");
            DisguiseAPI.disguiseEntity(subject, d);
        }
        if(goal != null && subject instanceof Player){
            subject.teleport(goal);
        }
    }

    /**
     * constructor based on ID
     * @param id id of the animation
     */
    public AnimationManager(UUID id){this.id = id;}

    /**
     * constructs animation given entity
     * ID is randomly generated
     * @param ent Entity to make animation for
     */
    public AnimationManager(@Nullable Entity ent){
        this.subject = ent; id = UUID.randomUUID();
    }

    /**
     * Constructs animation around a goal based on camera teleporting
     * @param loc location to teleport the camera
     */
    public AnimationManager(Location loc){
        this.goal = loc;
        this.cameraTp = true;
    }

    /**
     * Constructs animation given the subject and goal
     * @param ent subject of animation
     * @param loc the goal location for entity ot go to
     */
    public AnimationManager(Entity ent, Location loc){
        this.subject = ent; this.goal = loc; id = UUID.randomUUID();
    }

    /**
     * Constructs animation given entity and id
     * @param ent the subject of the animation
     * @param id provides ID for animation
     */
    public AnimationManager(Entity ent, UUID id){
        this.subject = ent;
        this.id = id;
    }

    /**
     *
     * @return Location the mob will look at after it moves
     */
    public Location getEndFace(){return endFace;}

    /**
     *
     * @param g Location to set as goal
     */
    public void setGoal(Location g){this.goal = g;}

    /**
     *
     * @param disguise New disguise
     */
    public void setDisguise(String disguise){this.disguise = disguise;}

    /**
     *
     * @return  whether it is simultaneous
     */
    public boolean simultaneous(){
        return this.runSimul;
    }

    /**
     *
     * @param endFace the new location to look at
     */
    public void setEndFace(Location endFace){this.endFace = endFace;}

    /**
     *
     * @param sim sets animation to run simultaneously based on input
     */
    public void setSimultaneous(boolean sim){this.runSimul = sim;}

    /**
     * sets the animation to run simulatenously
     */
    public void setSimultaneous(){this.runSimul = true;}

    /**
     *
     * @return  the comments on the animation
     */
    public String getComments(){return this.comments;}

    /**
     *
     * @param comments new comments about animation
     */
    public void setComments(String comments){this.comments = comments;}

    /**
     * checks if animation has disguise
     * @return true if has disguise
     */
    public boolean hasDisguise(){return disguise != null;}
    /**
    *
    * @return true if has goal
    */
    public boolean hasGoal(){return goal != null;}
    /**
    *
    * @return true if has comments
    */
    public boolean hasComments(){return comments != null;}
    /**
    *
    * @return true if has text
    */
    public boolean hasText(){return text != null;}
    /**
    *
    * @return the ID of the animation
    */
    public UUID getId(){return this.id;}
    /**
    *
    * @return the entity in the animation
    */
    public Entity getActor(){return this.subject;}
    /**
    *
    * @return the ID of the spawn animation
    */
    public UUID getTie(){return this.tie;};
    /**
    *
    * @return ties animation to ID of spawn animation
    */
    public void setTie(UUID tie){this.tie = tie;}
    /**
    *
    * @return the goal of the entity
    */
    public Location getGoal(){return this.goal;}
    /**
    *
    * @return the disguise of the entity
    */
    public String getDisguise(){return this.disguise;}

    /**
     *
     * @param ent sets the entity in the animation
     */
    public void setActor(Entity ent){this.subject = ent;}

    /**
     * No-arg constructor for animation manager
     * Avoid over-using
     */
    public AnimationManager(){
        this.subject = null;
        this.text = null;
        this.disguise = null;
    }

    /**
     * Returns whether this animation teleports the viewer's camera.
     *
     * @return {@code true} if this animation involves camera teleportation
     */
    public boolean isCameraTp() { return cameraTp; }

    /**
     * Serializes this animation instance into a {@link Map} for configuration storage.
     * <p>
     * The returned map includes data such as the goal location, facing direction,
     * disguise, comments, and identifiers necessary to reconstruct the animation
     * through {@link #deserialize(Map)}.
     *
     * @return a map containing this animation's serialized data
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        if(goal != null) {
            result.put("goal", goal.serialize());
        }
        if(endFace != null) {
            result.put("endFace", endFace.serialize());
        }
        if(text != null) {
            result.put("text", text);
        }
        if(runSimul) result.put("simultaneous", true);
        if(disguise != null) result.put("disguise", disguise);
        if(comments != null) result.put("comments", comments);
        result.put("id", (id != null ? id.toString() : UUID.randomUUID().toString()));
        if(tie != null){result.put("tie", tie.toString());}
        if (cameraTp) result.put("cameraTp", true);
        return result;
    }
    /**
     * Deserializes a stored animation configuration into an {@link AnimationManager} instance.
     * <p>
     * This method reconstructs the animation's key properties—such as goal, disguise,
     * facing direction, comments, and ties—from the provided map. If minimal data is
     * present, the animation is assumed to represent a camera teleport.
     *
     * @param map the serialized animation data
     * @return a reconstructed {@code AnimationManager} instance
     */
    @SuppressWarnings("unchecked")
    public static AnimationManager deserialize(Map<String, Object> map){
        AnimationManager a = new AnimationManager(UUID.fromString((String) map.get("id")));
        if(map.containsKey("goal")) a.setGoal(Location.deserialize((Map<String, Object>) map.get("goal")));
        if(map.containsKey("disguise")) a.setDisguise((String) map.get("disguise"));
        if(map.containsKey("endFace")) a.setEndFace(Location.deserialize((Map<String, Object>) map.get("endFace")));
        if(map.containsKey("simultaneous")){ a.setSimultaneous();
        }
        if(map.containsKey("comments")){ a.setComments(String.valueOf(map.get("comments")));
        }
        if(map.containsKey("tie")) a.setTie(UUID.fromString(map.get("tie").toString()));
        if (map.containsKey("cameraTp")) {
            a.cameraTp = true;
        }
        if(a.goal != null){
            a.goal.setWorld(Torture.world);
        }
        return a;
    }
}

