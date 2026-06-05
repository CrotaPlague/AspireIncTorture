package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A single active disguise attached to one real entity.
 */
public interface DisguiseSession {

    /**
     * Gets the UUID of the real entity being disguised.
     *
     * @return the subject UUID
     */
    UUID subjectId();

    /**
     * Gets the real entity being disguised.
     *
     * @return the subject entity
     */
    Entity subject();

    /**
     * Gets the current appearance being shown.
     *
     * @return the disguise appearance
     */
    DisguiseAppearance appearance();

    /**
     * Re-evaluates and re-sends this disguise to all applicable viewers.
     */
    void refresh();

    /**
     * Removes this disguise from the entity and clears any viewer state.
     */
    void destroy();

    /**
     * Checks whether the given viewer is currently allowed to see the disguise.
     *
     * @param viewer the observer
     * @return true if the disguise is visible to that viewer
     */
    boolean canSeeDisguise(Player viewer);

    /**
     * Checks whether the given viewer is currently allowed to see the real entity.
     *
     * @param viewer the observer
     * @return true if the real entity is visible to that viewer
     */
    boolean canSeeRealEntity(Player viewer);

    /**
     * Shows or hides the disguise for one viewer.
     *
     * <p>This does not have to affect the real entity's visibility.</p>
     *
     * @param viewer the observer
     * @param visible true to show the disguise, false to hide it
     */
    void setDisguiseVisible(Player viewer, boolean visible);

    /**
     * Shows or hides the real entity for one viewer.
     *
     * <p>This does not have to affect the disguise's visibility.</p>
     *
     * @param viewer the observer
     * @param visible true to show the real entity, false to hide it
     */
    void setRealEntityVisible(Player viewer, boolean visible);
    /**
     * Makes only the disguise visible by default.
     *
     * <p>The real entity will be hidden from viewers unless explicitly overridden.</p>
     */
    default void showDisguiseOnly() {
        defaultRealEntityVisible(false);
        defaultDisguiseVisible(true);
    }

    /**
     * Makes only the real entity visible by default.
     *
     * <p>The disguise will be hidden from viewers unless explicitly overridden.</p>
     */
    default void showRealEntityOnly() {
        defaultRealEntityVisible(true);
        defaultDisguiseVisible(false);
    }

    /**
     * Makes both the disguise and real entity visible by default.
     */
    default void showBoth() {
        defaultRealEntityVisible(true);
        defaultDisguiseVisible(true);
    }

    /**
     * Hides both the disguise and the real entity by default.
     */
    default void hideEverything() {
        defaultRealEntityVisible(false);
        defaultDisguiseVisible(false);
    }

    /**
     * Sets the default disguise visibility for viewers
     * without explicit overrides.
     *
     * @param visible true if visible by default
     */
    void defaultDisguiseVisible(boolean visible);

    /**
     * Sets the default real entity visibility for viewers
     * without explicit overrides.
     *
     * @param visible true if visible by default
     */
    void defaultRealEntityVisible(boolean visible);
}

