package com.crotaplague.torture.Files.ServerStorage.humans;



import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.crotaplague.torture.Torture.worldData;


public class humanDex {
    public static humanClass getHuman(int input, LivingEntity entity){
        humanClass human = null;
        List<mobEnums> mobs = new ArrayList<mobEnums>();
        if (worldData.getConfig().isConfigurationSection("humanDexNum " + input)){
            ConfigurationSection configurationSection = worldData.getConfig().getConfigurationSection("humanDexNum " + input);
            Map<String, Object> data = configurationSection.getValues(true);
            human = humanClass.deserialize(data);
            human.setMobEntity(entity);
            human.setThisMob(entity);
        }
        if(human != null) {
            human.setDexNum(input);
        }
        return human;
    }
}
