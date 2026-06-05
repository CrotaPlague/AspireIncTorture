package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.NamespacedKey;

import java.util.Map;

/**
 * Describes how a disguise appearance should be serialized and deserialized.
 *
 * @param <T> the appearance type
 */
public interface DisguiseAppearanceSerializer<T extends DisguiseAppearance> {

    /**
     * Returns the type key handled by this serializer.
     *
     * @return the handled type key
     */
    NamespacedKey typeKey();

    /**
     * Serializes an appearance to config-friendly data.
     *
     * @param appearance the appearance to serialize
     * @return a map suitable for saving to config, JSON, or a database
     */
    Map<String, Object> serialize(T appearance);

    /**
     * Deserializes an appearance from stored data.
     *
     * @param data the stored appearance data
     * @return the reconstructed appearance
     */
    T deserialize(Map<String, Object> data);
}
