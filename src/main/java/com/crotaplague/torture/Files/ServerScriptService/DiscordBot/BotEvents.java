package com.crotaplague.torture.Files.ServerScriptService.DiscordBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotEvents extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().getIdLong() != 865386789459591238L){
            if(event.getAuthor() instanceof User){
                MessageChannelUnion union = event.getChannel();
                union.sendMessage("zamn").queue();
            }
        }
    }
}
