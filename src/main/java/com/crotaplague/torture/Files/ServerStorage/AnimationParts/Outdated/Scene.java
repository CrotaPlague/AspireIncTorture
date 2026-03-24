package com.crotaplague.torture.Files.ServerStorage.AnimationParts.Outdated;



import com.crotaplague.torture.Torture;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Scene implements ConfigurationSerializable {
    List<Animation> EntMoves = new ArrayList<>();
    List<Location> mobLocs = new ArrayList<>();
    Map<Integer, LivingEntity> ents = new HashMap<>();
    private Integer index = 0;
    String name;
    Location playerBoxer;
    public Scene(){}
    public Scene(String string){this.name = string;}
    public void addMob(Location loc){mobLocs.add(loc);}
    public void addAnimation(Animation an){EntMoves.add(an);}
    public void setAnimations(List<Animation> ans){this.EntMoves = ans;}
    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}
    public void setPlayerLocation(Location loc){this.playerBoxer = loc;}
    public void start(){
        int count = 0;
        int futureCount = 1;
        for(Animation an : EntMoves){
            if(futureCount == count+1){
                if(!ents.containsKey(an.getEntNumber())){
                    Villager vil = (Villager) mobLocs.get(an.getEntNumber()).getWorld().spawnEntity(mobLocs.get(an.getEntNumber()), EntityType.VILLAGER);
                    vil.setSilent(true);
                    String str = an.getSkin();
                    Disguise disguise;
                    try {
                        DisguiseAPI.addCustomDisguise("tempDis", str);
                    } catch (DisguiseParseException e) {
                        e.printStackTrace();
                    }
                    disguise = DisguiseAPI.getCustomDisguise("tempDis");
                    DisguiseAPI.disguiseEntity(vil, disguise); //requires string disguise data
                    ents.put(an.getEntNumber(), vil);
                }
                if(EntMoves.size()-1 >= futureCount){
                    if(EntMoves.get(futureCount).isTiedToPrevious()){
                        while(EntMoves.get(futureCount).isTiedToPrevious()){
                            Animation finalAn = an;
                            new Thread(() -> {
                                finalAn.play();
                            }).start();
                            futureCount++;
                            an = EntMoves.get(futureCount);
                        }
                    }else{
                        an.setEntity(ents.get(an.getEntNumber()));
                        an.play();
                        an.getTask().cancel();
                    }
                }
            }else{
                count = futureCount;
                futureCount++;
            }
            count++;
            futureCount++;
        }
    }
    public void runFrame(int num){
        EntMoves.get(num).play();
    }
    public void next(){
        if(index != null){
            index++;
        }else{
            index = 0;
        }
        EntMoves.get(index).play();
    }
    public void back(){
        if(index != null){
            index--;
        }else{
            index = 0;
        }
        EntMoves.get(index).play();
    }
    public void replay(){
        EntMoves.get(index).play();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", this.name);
        List<String> ids = new ArrayList<>();
        for(Animation an : this.EntMoves){
            ids.add(an.getName());
        }
        result.put("ids", ids);
        if(this.playerBoxer != null){
            result.put("playerLocation", playerBoxer.serialize());
        }
        return result;
    }
    public static Scene deserialize(MemorySection section){
        Map<String, Object> values = section.getValues(false);
        Set<String> keys = values.keySet();
        Scene result = new Scene();
        List<String> ids = new ArrayList<>();
        for(String path : keys){
            String rawPath = path;
            path = section.getCurrentPath() + "." + path;
            if(rawPath.equals("ids")){
                ids = (List<String>) section.getList(path);
            }

        }
        List<Animation> animations = new ArrayList<>();
        if(ids != null){
            for(String id : ids){
                Animation an = Animation.deserialize((MemorySection) Torture.animationData.getConfig().get("Animation " + id));
                animations.add(an);
            }
        }
        result.setAnimations(animations);
        result.setName((String) values.get("name"));
        if(values.containsKey("playerLocation")){
            result.setPlayerLocation(Location.deserialize((Map<String, Object>) values.get("playerLocation")));
        }
        return result;
    }
}
