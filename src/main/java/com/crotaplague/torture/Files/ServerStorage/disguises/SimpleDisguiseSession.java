package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class SimpleDisguiseSession implements DisguiseSession {
    private final SimpleDisguiseManager manager;
    private final Entity subject;
    private DisguiseAppearance appearance;

    private final Map<UUID, Boolean> disguiseVisibility = new HashMap<>();
    private final Map<UUID, Boolean> realVisibility = new HashMap<>();

    private boolean defaultRealEntityVisible = true;
    private boolean defaultDisguiseVisible = true;

    SimpleDisguiseSession(SimpleDisguiseManager manager, Entity subject, DisguiseAppearance appearance) {
        this.manager = manager;
        this.subject = subject;
        this.appearance = appearance;
    }

    @Override
    public UUID subjectId() {
        return subject.getUniqueId();
    }

    @Override
    public Entity subject() {
        return subject;
    }

    @Override
    public DisguiseAppearance appearance() {
        return appearance;
    }

    public void appearance(DisguiseAppearance newAppearance) {
        this.appearance = Objects.requireNonNull(newAppearance, "newAppearance");
    }

    @Override
    public void refresh() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (subject.isTrackedBy(viewer)) {
                syncViewer(viewer);
            }
        }
    }

    @Override
    public void destroy() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            manager.destroyPackets(this);
        }
        manager.remove(this);
    }

    @Override
    public boolean canSeeRealEntity(Player viewer) {
        return realVisibility.getOrDefault(viewer.getUniqueId(), defaultRealEntityVisible);
    }

    @Override
    public boolean canSeeDisguise(Player viewer) {
        return disguiseVisibility.getOrDefault(viewer.getUniqueId(), defaultDisguiseVisible);
    }

    @Override
    public void setDisguiseVisible(Player viewer, boolean visible) {
        disguiseVisibility.put(viewer.getUniqueId(), visible);
        syncViewer(viewer);
    }

    @Override
    public void setRealEntityVisible(Player viewer, boolean visible) {
        realVisibility.put(viewer.getUniqueId(), visible);
        syncViewer(viewer);
    }

    void syncViewer(Player viewer) {
        if (subject.isValid() && subject.isTrackedBy(viewer)) {
            manager.syncViewer(this, viewer);
        }
    }
    public void setDefaultRealEntityVisible(boolean visible) {
        this.defaultRealEntityVisible = visible;
        refresh();
    }

    public void setDefaultDisguiseVisible(boolean visible) {
        this.defaultDisguiseVisible = visible;
        refresh();
    }
    public void showDisguiseOnly() {
        defaultRealEntityVisible = false;
        defaultDisguiseVisible = true;
        refresh();
    }

    public void showRealEntityOnly() {
        setDefaultRealEntityVisible(true);
        setDefaultDisguiseVisible(false);
    }

    public void showBoth() {
        setDefaultRealEntityVisible(true);
        setDefaultDisguiseVisible(true);
    }

    public void hideEverything() {
        setDefaultRealEntityVisible(false);
        setDefaultDisguiseVisible(false);
    }

    @Override
    public void defaultDisguiseVisible(boolean visible) {
        this.defaultDisguiseVisible = visible;
    }

    @Override
    public void defaultRealEntityVisible(boolean visible) {
        this.defaultRealEntityVisible = visible;
    }
}
