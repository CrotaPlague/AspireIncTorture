package com.crotaplague.torture.Files.ServerStorage.AnimationParts.Outdated;



import com.crotaplague.torture.Torture;
import com.destroystokyo.paper.entity.Pathfinder;


import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Animation extends BukkitRunnable implements ConfigurationSerializable {
    private boolean tiedToPrevious = false;
    private int entNumber;
    private int identifier;
    private Entity thisEnt;
    private List<Location> points = new ArrayList<Location>();
    private BukkitTask task;
    private boolean animationCompleted = false;
    private long delay = 10;
    private String entitySkin;
    private String name;

    public Animation(String name){this.name = name; this.identifier = Torture.animationData.getConfig().getInt("count"); Torture.animationData.getConfig().set("count", identifier+1);};
    public Animation(int id){this.identifier = id;}
    public Animation(Entity self){
        this.thisEnt = self; this.identifier = Torture.animationData.getConfig().getInt("count"); Torture.animationData.getConfig().set("count", identifier);
    }
    public void setDelay(long delay){this.delay = delay;}
    public long getDelay(){return this.delay;}
    public void addToPoints(Location loc){ this.points.add(loc);}
    public void setPoints(List<Location> locs){ this.points =  locs;}
    public List<Location> getPoints(){return this.points;}
    public void setName(String na){this.name = na;}
    public void setSkin(String skin){this.entitySkin = skin;}
    public String getSkin(){return this.entitySkin;}
    public String getName(){return this.name;}
    public int getIdentifier(){return this.identifier;}
    public void setEntNum(int en){this.entNumber = en;}
    public int getEntNumber(){return this.entNumber;}
    public BukkitTask getTask(){return this.task;}
    public void setEntity(Entity ent){this.thisEnt = ent;}
    public void setTiedToPrevious(boolean yn){this.tiedToPrevious = yn;}
    public boolean isTiedToPrevious(){return this.tiedToPrevious;}
    public void play() {
        List<Location> locations = this.points;
        final Location[] loc = {locations.get(0)};
        Pathfinder path = ((Mob) thisEnt).getPathfinder();
        Pathfinder.PathResult result = path.findPath(loc[0]);
        this.task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Torture.getInstance(), () -> {
            if(thisEnt.getLocation().distanceSquared(loc[0]) < 0.05){ loc[0] = locations.get(locations.indexOf(loc[0]));}else{animationCompleted = true;}
            path.findPath(loc[0]);

        }, delay, 1L);
    }

    public void repeating(){

    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        if(this.delay != 10){
            result.put("delay", delay);
        }
        if(this.points != null){
            result.put("locations", points);
        }
        if(this.entitySkin != null){
            result.put("skin", this.entitySkin);
        }
        result.put("identifier", identifier);

        result.put("name", name);
        result.put("entityIdent", entNumber);
        result.put("tiedToPrevious", tiedToPrevious);
        return result;
    }
    public static Animation deserialize(MemorySection memory){
        Map<String, Object> result = memory.getValues(false);
        Animation an = new Animation((int) result.get("identifier"));
        an.setEntNum((int) result.get("identifier"));
        Set<String> keys = memory.getKeys(false);
        for(String path : keys){
            String rawPath = path;
            path = memory.getCurrentPath() + "." + path;

            if(rawPath.equals("locations")){
                List<Location> locs = (List<Location>) Torture.animationData.getConfig().getList(path);
                an.setPoints(locs);
            }else{
                an.setPoints(new ArrayList<Location>());
            }

        }

        an.setName(String.valueOf(result.get("name")));
        if(result.containsKey("delay")){
            an.setDelay(Long.valueOf(result.get("delay").toString()));
        }
        if(result.containsKey("skin")){
            an.setSkin(String.valueOf(result.get("skin")));
        }
        an.setTiedToPrevious((Boolean) result.get("tiedToPrevious"));



        return an;
    }

    @Override
    public void run() {

    }
}
