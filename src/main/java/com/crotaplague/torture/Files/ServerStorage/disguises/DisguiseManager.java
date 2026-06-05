package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * Main entry point for disguise management.
 */
public interface DisguiseManager {

    /**
     * Applies a disguise appearance to the given entity.
     *
     * <p>If the entity already has an active disguise, the existing disguise is replaced.</p>
     *
     * @param subject the real entity being disguised
     * @param appearance the visual appearance to show to viewers
     * @return the active disguise session
     */
    DisguiseSession disguise(Entity subject, DisguiseAppearance appearance);

    /**
     * Removes any active disguise from the given entity.
     *
     * @param subject the real entity whose disguise should be removed
     * @return true if a disguise was removed, false if none existed
     */
    boolean undisguise(Entity subject);

    /**
     * Gets the active disguise for an entity.
     *
     * @param subject the real entity to look up
     * @return the active disguise session, or empty if none exists
     */
    Optional<DisguiseSession> get(Entity subject);

    /**
     * Gets all currently active disguise sessions.
     *
     * @return all active disguise sessions
     */
    Collection<DisguiseSession> getAll();

    /**
     * Re-sends all active disguises to a viewer.
     *
     * <p>This is useful for join handling, world changes, respawns, or any custom resync moment.</p>
     *
     * @param viewer the player to synchronize
     */
    void sync(Player viewer);

    /**
     * Re-sends one disguise to all viewers that should currently see it.
     *
     * @param subject the real entity whose disguise should be synchronized
     */
    void sync(Entity subject);

    /**
     * Registers a serializer for a custom disguise appearance type.
     *
     * <p>This is what lets the API stay generic while new disguise kinds are added later.</p>
     *
     * @param serializer the serializer to register
     * @param <T> the appearance type handled by this serializer
     */
    <T extends DisguiseAppearance> void registerSerializer(DisguiseAppearanceSerializer<T> serializer);

    /**
     * Returns the visibility policy used by this manager.
     *
     * @return the current visibility policy
     */
    DisguiseVisibilityPolicy visibilityPolicy();

    /**
     * Sets the visibility policy used by this manager.
     *
     * @param policy the new visibility policy
     */
    void visibilityPolicy(DisguiseVisibilityPolicy policy);
}
