package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class SimpleDisguiseApi implements DisguiseApi {

    private final Plugin plugin;
    private final DisguiseManager manager;

    public SimpleDisguiseApi(Plugin plugin, DisguiseManager manager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    @Override
    public DisguiseManager manager() {
        return manager;
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }
}