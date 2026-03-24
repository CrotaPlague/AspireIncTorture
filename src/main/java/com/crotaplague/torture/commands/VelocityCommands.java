package com.crotaplague.torture.commands;

import com.crotaplague.torture.Files.DataManager;
import com.crotaplague.torture.Torture;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class VelocityCommands {
    public VelocityCommands(){

    }
    public String runCommand(String cmd, String[] args){
        File folder = new File(Torture.path + "saves");
        File[] files = folder.listFiles();
        if(cmd.equals("playaverage")) {
            float total = 0;
            int count = 0;

            for(File file : files){
                DataManager m = new DataManager(file);
                Set<String> s = m.getConfig().getKeys(false);
                Optional<String> save = s.stream().findFirst();
                if(save.isPresent()){
                    MemorySection mem = (MemorySection) m.getConfig().get(save.get());
                    Map<String, Object> ma = mem.getValues(false);
                    total += (((Double) ma.get("timeOnServer")).floatValue());
                    count++;
                }
            }
            return "the average on the server is: " + String.valueOf(Float.valueOf(total)/count) + " hours";
        }
        if(cmd.equals("playertime")){
            String name = args[0];

            for(File file : files){
                DataManager m = new DataManager(file);
                Set<String> s = m.getConfig().getKeys(false);
                Optional<String> save = s.stream().findFirst();
                if(save.isPresent()){
                    MemorySection mem = (MemorySection) m.getConfig().get(save.get());
                    Map<String, Object> ma = mem.getValues(false);
                    if(ma.get("username").toString().equalsIgnoreCase(args[0])){
                        return ma.get("username") + " has been on for " + ma.get("timeOnServer") + " hours.";
                    }
                }
            }
            return name + " has not been on the server.";
        }
        if(cmd.equals("enabled")){
            return "Yeah it's chillin' fr fr";
        }

        return "";
    }
}
