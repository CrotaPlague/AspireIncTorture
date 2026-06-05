package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * High-level disguise object.
 * Subclasses are serializable and can be stored directly in config.
 */
public abstract class Disguise implements DisguiseAppearance, ConfigurationSerializable {

    /**
     * Applies this disguise to the given entity.
     *
     * @param entity entity to disguise
     * @return the active disguise session
     */
    public DisguiseSession apply(Entity entity) {
        return DisguiseApiHolder.require().manager().disguise(entity, this);
    }

    /**
     * Removes this disguise from the given entity.
     *
     * @param entity entity to undisguise
     * @return true if a disguise was removed
     */
    public boolean remove(Entity entity) {
        return DisguiseApiHolder.require().manager().undisguise(entity);
    }

    /**
     * Gets the current active session for the given entity, if one exists.
     *
     * @param entity entity to check
     * @return active session if present
     */
    public Optional<DisguiseSession> session(Entity entity) {
        return DisguiseApiHolder.require().manager().get(entity);
    }

    /**
     * Re-sends the disguise for the given entity.
     *
     * @param entity entity to refresh
     */
    public void refresh(Entity entity) {
        session(entity).ifPresent(DisguiseSession::refresh);
    }

    /**
     * Shows the disguise to one viewer.
     *
     * @param entity disguised entity
     * @param viewer viewer
     */
    public void showTo(Entity entity, Player viewer) {
        session(entity).ifPresent(s -> s.setDisguiseVisible(viewer, true));
    }

    /**
     * Hides the disguise from one viewer.
     *
     * @param entity disguised entity
     * @param viewer viewer
     */
    public void hideFrom(Entity entity, Player viewer) {
        session(entity).ifPresent(s -> s.setDisguiseVisible(viewer, false));
    }

    /**
     * Shows the real entity to one viewer.
     *
     * @param entity disguised entity
     * @param viewer viewer
     */
    public void showRealTo(Entity entity, Player viewer) {
        session(entity).ifPresent(s -> s.setRealEntityVisible(viewer, true));
    }

    /**
     * Hides the real entity from one viewer.
     *
     * @param entity disguised entity
     * @param viewer viewer
     */
    public void hideRealFrom(Entity entity, Player viewer) {
        session(entity).ifPresent(s -> s.setRealEntityVisible(viewer, false));
    }

    /**
     * Checks whether the disguise is currently visible to a viewer.
     *
     * @param entity disguised entity
     * @param viewer viewer
     * @return true if visible
     */
    public boolean isVisibleTo(Entity entity, Player viewer) {
        return session(entity).map(s -> s.canSeeDisguise(viewer)).orElse(false);
    }
}
