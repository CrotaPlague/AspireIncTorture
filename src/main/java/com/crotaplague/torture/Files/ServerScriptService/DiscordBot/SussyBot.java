package com.crotaplague.torture.Files.ServerScriptService.DiscordBot;

import com.crotaplague.torture.Files.ServerScriptService.DiscordBot.BotCommands.CommandManagers;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class SussyBot {
    private final ShardManager manager;
    public SussyBot(){
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(System.getenv("TOKEN"));
        manager = builder.build();
        manager.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.watching("your every movement. Watch your back."));
        manager.addEventListener(new BotEvents());
        manager.addEventListener(new CommandManagers());
    }

    public ShardManager getShardManager(){
        return manager;
    }
}
