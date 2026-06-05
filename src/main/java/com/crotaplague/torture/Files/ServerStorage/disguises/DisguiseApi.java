package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.plugin.Plugin;

/**
 * Convenience access to the API from your plugin.
 */
public interface DisguiseApi {

    /**
     * Gets the disguise manager.
     *
     * @return the disguise manager
     */
    DisguiseManager manager();

    /**
     * Gets the plugin instance used by the disguise system.
     *
     * @return the owning plugin
     */
    Plugin plugin();
}
