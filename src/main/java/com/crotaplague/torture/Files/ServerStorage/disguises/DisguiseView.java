package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Snapshot of one viewer's relationship to one disguise.
 */
public record DisguiseView(
        Player viewer,
        Entity subject,
        DisguiseAppearance appearance
) {}
