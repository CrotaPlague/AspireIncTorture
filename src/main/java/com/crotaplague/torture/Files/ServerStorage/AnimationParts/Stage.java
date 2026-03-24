package com.crotaplague.torture.Files.ServerStorage.AnimationParts;

import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.items.BagClass;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

import static com.crotaplague.torture.Torture.plugin;
/**
 * Represents a single stage within a scripted animation sequence.
 * <p>
 * A {@code Stage} defines a segment of an animation that can include one or more
 * {@link AnimationManager} instances, an optional start and end location, and an assigned viewer.
 * Each stage can be serialized and deserialized for storage in plugin configuration files.
 * </p>
 *
 * <p>
 * During playback, the stage manages execution of its animations in sequence or simultaneously,
 * using an internal grouping system. Stages can also teleport the camera (via an {@link ArmorStand})
 * to simulate cinematic camera movement for a player viewing the animation.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * Stage stage = new Stage("Intro");
 * stage.setStartLocation(startLoc);
 * stage.setEndLocation(endLoc);
 * stage.setAnimations(animationList);
 * stage.setViewer(player);
 * stage.run(); // Executes the stage animation sequence
 * }</pre>
 *
 * <p>
 * Instances of this class are typically created and managed by the {@link Torture} plugin.
 * They may be serialized using Bukkit’s {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
 * system for persistent storage.
 * </p>
 *
 * @see AnimationManager
 * @see Torture
 * @see org.bukkit.configuration.serialization.ConfigurationSerializable
 */
public class Stage implements ConfigurationSerializable, Runnable {
    private final Plugin plugin;
    private List<AnimationManager> anms = new ArrayList<>();
    private String name;
    private Player viewer = null;
    private Location endLocation = null;
    private ArmorStand camera;
    private Location startLocation = null;

    /**
     * Creates a new {@link Stage} with the specified name.
     * <p>
     * This constructor initializes the stage using the main plugin instance ({@link Torture#plugin})
     * and assigns the provided name. It should be used for normal runtime instantiation.
     * </p>
     *
     * @param name the name of the stage
     */
    public Stage(String name) {
        this.plugin = Torture.plugin;
        this.name = name;
    }

    /**
     * Private no-argument constructor used internally for deserialization.
     * <p>
     * This constructor initializes the {@code plugin} reference to {@link Torture#plugin}
     * and sets the stage name to an empty string. The name and other properties
     * must be populated after construction (e.g., via {@link #deserialize(org.bukkit.configuration.MemorySection)}).
     * </p>
     */
    private Stage() {
        this.plugin = Torture.plugin;
        this.name = "";
    }


    /**
     * Sets the list of animations associated with this object.
     *
     * @param animations the list of {@link AnimationManager} instances to assign
     */
    public void setAnimations(@Nullable List<AnimationManager> animations) {
        this.anms = animations;
    }

    /***
     * Sets the name of this animation group or object.
     * @param name the new name to assign
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of this animation group or object.
     *
     * @return the name, or {@code null} if none has been set
     */
    public @Nullable String getName() {
        return this.name;
    }
    /**
     * Retrieves the ending location for the animation.
     *
     * @return the {@link Location} the animation ends at, or {@code null} if not defined
     */
    public @Nullable Location getEndLocation() {
        return this.endLocation;
    }
    /**
     * Sets the ending location for the animation.
     *
     * @param endLocation the {@link Location} to set as the animation’s end point
     */
    public void setEndLocation(@Nullable Location endLocation) {
        this.endLocation = endLocation;
    }
    /**
     * Sets the starting location for the animation.
     *
     * @param startLocation the {@link Location} to set as the starting point
     */
    public void setStartLocation(@Nullable Location startLocation) {
        this.startLocation = startLocation;
    }
    /***
     * Sets the player who will view the animation.
     * @param player {@link Player} who will view the animation.
     */
    public void setViewer(Player player){viewer = player;}
    /**
     * Returns the list of animations managed by this instance.
     * <p>
     * If no animations have been initialized, an empty list is returned instead of {@code null}.
     *
     * @return a list of {@link AnimationManager} objects, or an empty list if none exist
     */
    public List<AnimationManager> getAnimations() {
        return anms != null ? this.anms : new ArrayList<>();
    }

    /**
     * Serializes this object into a map of key-value pairs for configuration storage.
     * <p>
     * This method is used by Bukkit’s {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
     * system to save this object’s state into configuration files (e.g., YAML).
     * It includes information about animations, the name, and optional start/end locations.
     * </p>
     *
     * @return a non-null {@link Map} containing this object's serialized state
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("animations", anms != null ? anms : new ArrayList<>());
        result.put("name", name);
        if (endLocation != null) {
            result.put("endLocation", endLocation.serialize());
        }
        if (startLocation != null) {
            result.put("startLocation", startLocation.serialize());
        }
        return result;
    }


    /**
     * Deserializes a {@link Stage} object from the provided {@link MemorySection}.
     * <p>
     * This method reconstructs a {@code Stage} instance from configuration data
     * previously saved via {@link #serialize()}. It restores the stage’s name,
     * animations, and optional start/end locations.
     * </p>
     *
     * <p>
     * If the animations list fails to load (e.g., due to corruption or missing data),
     * an empty list will be used, and an error will be logged to the console.
     * </p>
     *
     * @param memorySection the {@link MemorySection} containing serialized stage data
     * @return a reconstructed {@link Stage} object
     */
    @SuppressWarnings("unchecked")
    public static Stage deserialize(MemorySection memorySection) {
        Stage stage = new Stage();
        stage.setName(memorySection.getString("name", ""));
        Map<String, Object> objs = memorySection.getValues(false);
        if (objs.containsKey("endLocation")) {
            stage.setEndLocation(Location.deserialize(((MemorySection) objs.get("endLocation")).getValues(false)));
        }
        if (objs.containsKey("startLocation")) {
            stage.setStartLocation(Location.deserialize(((MemorySection) objs.get("startLocation")).getValues(false)));
        }
        List<AnimationManager> ans = (List<AnimationManager>) memorySection.getList("animations");
        if (ans == null) {
            ans = new ArrayList<>();
            Torture.plugin.getLogger().log(Level.SEVERE, "Failed to load animations for stage: " + stage.getName());
        }
        stage.setAnimations(ans);
        return stage;
    }


    @Override
    public void run() {
        if (anms == null || anms.isEmpty() || viewer == null || !viewer.isOnline()) return;
        World w = viewer.getWorld();
        NamespacedKey key = new NamespacedKey(plugin, "bindCamera");
        camera = (ArmorStand) w.spawnEntity(startLocation, EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM, ent -> {
            ent.setGravity(false); ent.setInvulnerable(true); ent.setVisibleByDefault(true); ent.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 2);});
        viewer.setGameMode(GameMode.SPECTATOR);
        viewer.setSpectatorTarget(camera);
        // 1. Group sequential and simultaneous animations
        List<List<AnimationManager>> groups = new ArrayList<>();
        List<AnimationManager> current = new ArrayList<>();
        for (int i = 0; i < anms.size(); i++) {
            current.add(anms.get(i));
            if (i + 1 < anms.size() && anms.get(i + 1).simultaneous()) {
                continue;
            }
            groups.add(new ArrayList<>(current));
            current.clear();
        }

        // 2. Start the chained execution
        runGroup(0, groups);
    }

    private void runGroup(int index, List<List<AnimationManager>> groups) {
        if (index >= groups.size()){
            NamespacedKey key = new NamespacedKey(plugin, "bindCamera");
            if(viewer.isOnline() && endLocation != null){
                viewer.teleport(endLocation);
            }
            camera.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
            viewer.setGameMode(GameMode.ADVENTURE);
            camera.remove();
            return;
        }
        if(!viewer.isOnline()){
            for(List<AnimationManager> group : groups){
                for(AnimationManager anm : group){
                    if(anm.subject != null && !(anm.subject instanceof Player) && anm.subject.isValid()){
                        anm.subject.remove();
                    }
                }
            }
            return;
        }
        List<AnimationManager> group = groups.get(index);
        // Start all animations in this group
        for (AnimationManager anim : group) {
            if(anim.getTie() != null){
                UUID tie = anim.getTie();
                for(AnimationManager a : anms){
                    if(tie.equals(a.getId())){
                        anim.setActor(a.subject);
                        break;
                    }
                }
            }else{
                if(anim.disguise == null && anim.goal != null){
                    anim.setActor(viewer);
                }
            }
            anim.run();
        }

        // Poll once per tick until all are at their goal
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean allArrived = true;
                for (AnimationManager a : group) {
                    if(randomScripts.getRandomNumber(0, 15) == 2){
                        viewer.sendMessage("nah " + a.subject.getLocation().distance(a.goal));
                    }
                    if (a.subject.getLocation().distance(a.goal) > 0.8) {
                        a.run();
                        allArrived = false;
                        break;
                    }
                    if (a.subject instanceof Mob) {
                        ((Mob) a.subject).lookAt(a.getEndFace());
                    }
                }

                if (allArrived) {
                    this.cancel();
                    runGroup(index + 1, groups);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
