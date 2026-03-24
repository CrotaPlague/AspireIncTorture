package com.crotaplague.torture.Files.ServerStorage;


import com.crotaplague.torture.Torture;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class RealData {

    private Torture plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public RealData(Torture plugin){
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig(){
        if(this.configFile == null){
            this.configFile = new File(Torture.path, "realData.yml");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile.getAbsoluteFile());

        this.dataConfig.setDefaults(yaml);
    }

    public FileConfiguration getConfig(){
        if(this.dataConfig == null){
            this.reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if(this.dataConfig == null || this.configFile == null) return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }
        reloadConfig();
    }

    public void saveDefaultConfig(){
        if(this.configFile == null) this.configFile = new File(Torture.path, "realData.yml");

        if(!(this.configFile.exists())){
            this.configFile = new File(Torture.path + "realData.yml");
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
