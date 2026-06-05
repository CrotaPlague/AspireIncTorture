package com.crotaplague.torture.Files.ServerStorage.disguises;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityMetadataProvider;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PacketEvents-based disguise renderer.
 *
 * <p>Player disguises are rendered as fake client-side players.
 * Block and item disguises are rendered as real display entities that are hidden or shown per viewer.</p>
 */
public final class SimpleDisguiseRenderer implements DisguiseRenderer {

    private final Plugin plugin;
    private final AtomicInteger nextFakeEntityId = new AtomicInteger(1_000_000);

    /**
     * One real display entity per session for block and item disguises.
     */
    private final Map<UUID, Entity> visualEntities = new HashMap<>();

    /**
     * One fake player entity id per session for player disguises.
     */
    private final Map<UUID, Integer> fakePlayerEntityIds = new HashMap<>();

    public SimpleDisguiseRenderer(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public void showDisguise(Player viewer, DisguiseSession session) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(session, "session");

        DisguiseAppearance appearance = session.appearance();

        if (appearance instanceof PlayerDisguise playerDisguise) {
            showPlayerDisguise(viewer, session, playerDisguise);
            return;
        }

        Entity visualEntity = ensureVisualEntity(session);
        if (visualEntity == null || !visualEntity.isValid()) {
            return;
        }

        visualEntity.teleport(session.subject().getLocation());
        viewer.showEntity(plugin, visualEntity);
    }

    @Override
    public void hideDisguise(Player viewer, DisguiseSession session) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(session, "session");

        DisguiseAppearance appearance = session.appearance();

        if (appearance instanceof PlayerDisguise) {
            hidePlayerDisguise(viewer, session);
            return;
        }

        Entity visualEntity = visualEntities.get(session.subjectId());
        if (visualEntity != null && visualEntity.isValid()) {
            viewer.hideEntity(plugin, visualEntity);
        }
    }

    @Override
    public void showRealEntity(Player viewer, DisguiseSession session) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(session, "session");
        viewer.showEntity(plugin, session.subject());
    }

    @Override
    public void hideRealEntity(Player viewer, DisguiseSession session) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(session, "session");
        viewer.hideEntity(plugin, session.subject());
    }

    @Override
    public void destroy(DisguiseSession session) {
        Objects.requireNonNull(session, "session");

        // Remove any block/item display entity.
        Entity visual = visualEntities.remove(session.subjectId());
        if (visual != null && visual.isValid()) {
            visual.remove();
        }

        // Remove any fake player entity from every viewer and clear tab-list traces.
        if (session.appearance() instanceof PlayerDisguise) {
            Integer entityId = fakePlayerEntityIds.remove(session.subjectId());
            if (entityId != null) {
                WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(entityId);
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, destroy);
                }
            }
        }

        // Restore the real entity to anyone currently online.
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showEntity(plugin, session.subject());
        }
    }

    private void showPlayerDisguise(Player viewer, DisguiseSession session, PlayerDisguise playerDisguise) {
        UserProfile profile = toPacketEventsProfile(playerDisguise);

        int entityId = fakePlayerEntityIds.computeIfAbsent(
                session.subjectId(),
                ignored -> nextFakeEntityId.getAndIncrement()
        );

        Location bukkitLocation = session.subject().getLocation();

        // FIX 1: PlayerInfo constructor now requires listed, ping, gamemode, displayName, chatSession
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        profile,
                        false,           // listed in tab
                        0,               // latency/ping
                        GameMode.SURVIVAL,
                        null,            // display name (null = use profile name)
                        null             // chat session
                );

        WrapperPlayServerPlayerInfoUpdate addToTab =
                new WrapperPlayServerPlayerInfoUpdate(
                        EnumSet.of(
                                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY
                        ),
                        Collections.singletonList(info)
                );

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, addToTab);

        Vector3d position = new Vector3d(
                bukkitLocation.getX(),
                bukkitLocation.getY(),
                bukkitLocation.getZ()
        );

        // FIX 2: WrapperPlayServerSpawnPlayer no longer exists as a standalone packet.
        // Use WrapperPlayServerSpawnEntity with EntityTypes.PLAYER instead.
        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(profile.getUUID()),
                EntityTypes.PLAYER,
                position,
                bukkitLocation.getPitch(),
                bukkitLocation.getYaw(),
                bukkitLocation.getYaw(), // head yaw
                0,                       // data
                Optional.empty()                     // velocity
        );

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, spawn);

        WrapperPlayServerEntityMetadata skinMeta = new WrapperPlayServerEntityMetadata(
                entityId,
                List.of(new EntityData(16, EntityDataTypes.BYTE, (byte) 0x7f))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, skinMeta);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team noNames = scoreboard.getTeam("disguised_npcs");
        if (noNames == null) {
            noNames = scoreboard.registerNewTeam("disguised_npcs");
            noNames.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        noNames.addEntry(profile.getName());

        // Remove from tab after 2 ticks so the skin loads but the player isn't cluttering the tab list
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!viewer.isOnline()) {
                return;
            }

            WrapperPlayServerPlayerInfoRemove removeFromTab =
                    new WrapperPlayServerPlayerInfoRemove(
                            Collections.singletonList(profile.getUUID())
                    );

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, removeFromTab);
        }, 2L);
    }

    private void hidePlayerDisguise(Player viewer, DisguiseSession session) {
        Integer entityId = fakePlayerEntityIds.get(session.subjectId());
        if (entityId == null) {
            return;
        }

        WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, destroy);
    }

    private Entity ensureVisualEntity(DisguiseSession session) {
        DisguiseAppearance appearance = session.appearance();
        Entity existing = visualEntities.get(session.subjectId());

        if (appearance instanceof BlockDisguise blockDisguise) {
            if (existing instanceof BlockDisplay blockDisplay && existing.isValid()) {
                blockDisplay.setBlock(blockDisguise.blockData());
                blockDisplay.teleport(session.subject().getLocation());
                return blockDisplay;
            }

            if (existing != null && existing.isValid()) {
                existing.remove();
            }

            World world = session.subject().getWorld();
            Location location = session.subject().getLocation();

            BlockDisplay display = world.spawn(
                    location,
                    BlockDisplay.class,
                    spawned -> {
                        spawned.setVisibleByDefault(false);
                        spawned.setInvulnerable(true);
                        spawned.setGravity(false);
                        spawned.setSilent(true);
                        spawned.setPersistent(false);
                        spawned.setBlock(blockDisguise.blockData());
                    },
                    CreatureSpawnEvent.SpawnReason.CUSTOM
            );

            visualEntities.put(session.subjectId(), display);
            return display;
        }

        if (appearance instanceof ItemDisguise itemDisguise) {
            if (existing instanceof ItemDisplay itemDisplay && existing.isValid()) {
                itemDisplay.setItemStack(itemDisguise.itemStack());
                itemDisplay.teleport(session.subject().getLocation());
                return itemDisplay;
            }

            if (existing != null && existing.isValid()) {
                existing.remove();
            }

            World world = session.subject().getWorld();
            Location location = session.subject().getLocation();

            ItemDisplay display = world.spawn(
                    location,
                    ItemDisplay.class,
                    spawned -> {
                        spawned.setVisibleByDefault(false);
                        spawned.setInvulnerable(true);
                        spawned.setGravity(false);
                        spawned.setSilent(true);
                        spawned.setPersistent(false);
                        spawned.setItemStack(itemDisguise.itemStack());
                        spawned.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    },
                    CreatureSpawnEvent.SpawnReason.CUSTOM
            );

            visualEntities.put(session.subjectId(), display);
            return display;
        }

        return null;
    }

    private UserProfile toPacketEventsProfile(PlayerDisguise disguise) {
        com.destroystokyo.paper.profile.PlayerProfile paperProfile = disguise.profile();

        String name = paperProfile.getName();

        if (name == null || name.isBlank()) {
            name = "npc_unknown";
        }

        // Always use a random UUID for the fake entity — never the real player's UUID.
        // If we use the real UUID and that player is online (e.g. the viewer themselves),
        // the client will refuse to render a duplicate entity for their own UUID.
        UUID fakeUuid = UUID.randomUUID();

        List<TextureProperty> textures = new ArrayList<>();
        for (com.destroystokyo.paper.profile.ProfileProperty prop : paperProfile.getProperties()) {
            if (prop.getName().equalsIgnoreCase("textures")) {
                textures.add(new TextureProperty("textures", prop.getValue(), prop.getSignature()));
            }
        }

        return new UserProfile(fakeUuid, name, textures);
    }

    private Object extractProfileObject(PlayerDisguise disguise) {
        for (String methodName : List.of("profile", "getProfile")) {
            try {
                Method method = disguise.getClass().getMethod(methodName);
                method.setAccessible(true);
                return method.invoke(disguise);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return disguise;
    }

    private UUID extractUuid(Object profileObject) {
        for (String methodName : List.of("getUniqueId", "getUUID", "getId")) {
            try {
                Method method = profileObject.getClass().getMethod(methodName);
                method.setAccessible(true);
                Object result = method.invoke(profileObject);
                if (result instanceof UUID uuid) {
                    return uuid;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private String extractName(Object profileObject) {
        for (String methodName : List.of("getName")) {
            try {
                Method method = profileObject.getClass().getMethod(methodName);
                method.setAccessible(true);
                Object result = method.invoke(profileObject);
                if (result instanceof String name) {
                    return name;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private List<TextureProperty> extractTextureProperties(Object profileObject) {
        List<TextureProperty> textures = new ArrayList<>();

        try {
            Method getProperties = profileObject.getClass().getMethod("getProperties");
            getProperties.setAccessible(true);

            Object result = getProperties.invoke(profileObject);
            if (!(result instanceof Iterable<?> iterable)) {
                return textures;
            }

            for (Object property : iterable) {
                String propertyName = invokeString(property, "getName");
                if (propertyName == null || !propertyName.equalsIgnoreCase("textures")) {
                    continue;
                }

                String value = invokeString(property, "getValue");
                String signature = invokeString(property, "getSignature");

                if (value != null) {
                    textures.add(new TextureProperty("textures", value, signature));
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return textures;
    }

    private String invokeString(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}