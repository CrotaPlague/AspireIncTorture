package com.crotaplague.torture.Files;

import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.ZstdCodec;
import com.crotaplague.torture.Torture;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class DataManager {

    private Torture plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;
    private String fullPath;

    public DataManager(Torture plugin) {
        this.plugin = plugin;
        fullPath = Torture.path + "data.yml.zst";
        saveDefaultConfig();
    }

    public DataManager(Player player) {
        this.plugin = Torture.plugin;
        fullPath = Torture.path + "saves/player-" + player.getUniqueId() + ".yml.zst";
        saveDefaultConfig();
    }

    public DataManager(File file) {
        this.plugin = Torture.plugin;
        this.configFile = file;
        this.fullPath = file.getAbsolutePath();
        saveDefaultConfig();
    }

    public void reloadConfig() {
        this.configFile = new File(fullPath);

        if (!this.configFile.exists()) {
            this.dataConfig = new YamlConfiguration();
            return;
        }

        try {
            String base64 = Files.readString(Path.of(fullPath), StandardCharsets.UTF_8);
            String yamlText = ZstdCodec.decompressFromBase64(base64);
            this.dataConfig = YamlConfiguration.loadConfiguration(new StringReader(yamlText));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not decompress/load config: " + fullPath, e);
            this.dataConfig = new YamlConfiguration(); // fallback
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            this.reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null) return;

        try {
            String yamlText = this.dataConfig.saveToString();
            String base64 = ZstdCodec.compressToBase64(yamlText);
            Files.writeString(Path.of(fullPath), base64, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not compress/save config to " + fullPath, e);
        }

        reloadConfig();
    }

    public void saveDefaultConfig() {
        this.configFile = new File(fullPath);

        if (!this.configFile.exists()) {
            try {
                this.configFile.getParentFile().mkdirs();
                this.configFile.createNewFile();

                YamlConfiguration empty = new YamlConfiguration();
                String base64 = ZstdCodec.compressToBase64(empty.saveToString());
                Files.writeString(Path.of(fullPath), base64, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create default config at " + fullPath, e);
            }
        }
    }

    public File getFile() {
        return this.configFile;
    }
}

