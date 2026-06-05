package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class SimpleDisguiseManager implements DisguiseManager {
    private final Plugin plugin;
    private final DisguiseRenderer renderer;
    private DisguiseVisibilityPolicy visibilityPolicy;

    private final Map<UUID, SimpleDisguiseSession> sessions = new HashMap<>();
    private final Map<NamespacedKey, DisguiseAppearanceSerializer<?>> serializers = new HashMap<>();

    public SimpleDisguiseManager(Plugin plugin, DisguiseRenderer renderer, DisguiseVisibilityPolicy visibilityPolicy) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.visibilityPolicy = Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
    }

    @Override
    public DisguiseSession disguise(Entity subject, DisguiseAppearance appearance) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(appearance, "appearance");

        SimpleDisguiseSession session = new SimpleDisguiseSession(this, subject, appearance);

        // Default behavior: hide the real entity from everyone
        session.setDefaultRealEntityVisible(false);

        sessions.put(subject.getUniqueId(), session);
        session.refresh();
        return session;
    }

    @Override
    public boolean undisguise(Entity subject) {
        SimpleDisguiseSession session = sessions.remove(subject.getUniqueId());
        if (session == null) {
            return false;
        }
        session.destroy();
        return true;
    }

    @Override
    public Optional<DisguiseSession> get(Entity subject) {
        for(Map.Entry<UUID, SimpleDisguiseSession> entry : sessions.entrySet()) {
            Bukkit.getPlayer("CrotaPlague").sendMessage(entry.getKey().toString());
        }
        return Optional.ofNullable(sessions.get(subject.getUniqueId()));
    }

    @Override
    public Collection<DisguiseSession> getAll() {
        return Collections.unmodifiableCollection(sessions.values().stream().map(s -> (DisguiseSession) s).toList());
    }

    @Override
    public void sync(Player viewer) {
        for (SimpleDisguiseSession session : sessions.values()) {
            if (session.subject().isValid() && session.subject().isTrackedBy(viewer)) {
                session.syncViewer(viewer);
            }
        }
    }

    @Override
    public void sync(Entity subject) {
        SimpleDisguiseSession session = sessions.get(subject.getUniqueId());
        if (session == null) {
            return;
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (subject.isTrackedBy(viewer)) {
                session.syncViewer(viewer);
            }
        }
    }

    @Override
    public <T extends DisguiseAppearance> void registerSerializer(DisguiseAppearanceSerializer<T> serializer) {
        serializers.put(serializer.typeKey(), serializer);
    }

    @Override
    public DisguiseVisibilityPolicy visibilityPolicy() {
        return visibilityPolicy;
    }

    @Override
    public void visibilityPolicy(DisguiseVisibilityPolicy policy) {
        this.visibilityPolicy = Objects.requireNonNull(policy, "policy");
    }

    void remove(SimpleDisguiseSession session) {
        sessions.remove(session.subjectId());
    }

    void destroyPackets(SimpleDisguiseSession session) {
        renderer.destroy(session);
    }

    void syncViewer(SimpleDisguiseSession session, Player viewer) {
        DisguiseView view = new DisguiseView(viewer, session.subject(), session.appearance());

        boolean disguiseVisible = session.canSeeDisguise(viewer) && visibilityPolicy.canSeeDisguise(view);
        boolean realVisible = session.canSeeRealEntity(viewer) && visibilityPolicy.canSeeRealEntity(view);

        Entity subject = session.subject();
        PersistentDataContainer subjectData = subject.getPersistentDataContainer();
        if(subjectData.has(new NamespacedKey(plugin, "PersonalMob"), PersistentDataType.STRING) && subjectData.get(new NamespacedKey(plugin, "PersonalMob"), PersistentDataType.STRING) != null){
            UUID ownerId = UUID.fromString(subjectData.get(new NamespacedKey(plugin, "PersonalMob"), PersistentDataType.STRING));
            if(!ownerId.equals(viewer.getUniqueId())){
                disguiseVisible = false;
                realVisible = false;
            }
        }

        if (disguiseVisible) {
            renderer.showDisguise(viewer, session);
        } else {
            renderer.hideDisguise(viewer, session);
        }

        if (realVisible) {
            renderer.showRealEntity(viewer, session);
        } else {
            renderer.hideRealEntity(viewer, session);
        }
    }
}
