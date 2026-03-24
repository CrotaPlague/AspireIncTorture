package com.crotaplague.torture.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class cmdTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> posArgs = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("setNPC")){
            if(args.length <= 1){
                posArgs.add("setMob");
                posArgs.add("setPhrase");
                posArgs.add("setNPCType");
                posArgs.add("getMobs");
                posArgs.add("setCashReward");
                posArgs.add("setName");
                posArgs = posArgs.stream().filter(s -> {if(s.startsWith(args[0])) return true; return false;}).collect(Collectors.toList());
            }
        }
        if(command.getName().equals("animation")){
            if(args.length == 1){
                posArgs.add("addMob");
                posArgs.add("moveMob");
                posArgs.add("cameraTeleport");
            }
        }
        if(command.getName().equals("stage")){
            if(args.length <= 1){
                posArgs.add("play");
                posArgs.add("create");
                posArgs.add("addAnimation");
                posArgs.add("select");
                posArgs.add("viewAnimations");
                posArgs.add("returnLocation");
                posArgs.add("startLocation");
                posArgs = posArgs.stream().filter(s -> {if(s.startsWith(args[0])) return true; return false;}).collect(Collectors.toList());
            }
        }
        if(command.getName().equals("sc")){
            if(args.length == 1){
                posArgs.add("close");
            }
        }




        return posArgs;
    }
}
