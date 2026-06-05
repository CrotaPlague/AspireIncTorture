package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;

/**
 * Marker interface for any disguise appearance.
 *
 * <p>Block disguises, item disguises, player-like disguises, and future custom
 * disguise kinds should all implement this interface.</p>
 */
public interface DisguiseAppearance {

    /**
     * Returns the unique type ID for this appearance.
     *
     * <p>This should be stable and namespaced so it can be stored in configs safely.</p>
     *
     * @return the appearance type key
     */
    NamespacedKey typeKey();
}
