package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.entity.Player;

public interface DisguiseRenderer {
    void showDisguise(Player viewer, DisguiseSession session);
    void hideDisguise(Player viewer, DisguiseSession session);

    void showRealEntity(Player viewer, DisguiseSession session);
    void hideRealEntity(Player viewer, DisguiseSession session);

    void destroy(DisguiseSession session);
}