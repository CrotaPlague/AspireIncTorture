package com.crotaplague.torture.Files.ServerStorage.AnimationParts;

import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private List<TextDisplay> backgroundComponents;
    private TextDisplay mainBackground;
    private String currentMessage;
    private String fullMessage;
    private BukkitTask task;
    public Message(BukkitTask task){
        backgroundComponents = new ArrayList<>();
        mainBackground = null;
        currentMessage = "";
        fullMessage = "";
        this.task = task;
    }

    public void addBackgroundComponent(TextDisplay t){this.backgroundComponents.add(t);}
    public void setBackgroundComponents(List<TextDisplay> d){backgroundComponents = d;}
    public BukkitTask getTask(){return this.task;}
    public String getCurrentMessage(){return this.currentMessage;}
    public String getFullMessage(){return this.fullMessage;}
    public void setCurrentMessage(String m){this.currentMessage = m;}
    public void setFullMessage(String f){this.fullMessage = f;}
    public void setMainBackground(TextDisplay d){this.mainBackground = d;}
    public TextDisplay getMainBackground(){return this.mainBackground;}

}
