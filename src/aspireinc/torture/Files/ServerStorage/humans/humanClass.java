package aspireinc.torture.Files.ServerStorage.humans;

import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import org.bukkit.entity.Entity;


import java.util.List;

public class humanClass {

    private final String name;
    private List<mobEnums> mobs;
    private String phrase;
    private Integer boxNum = null;
    private Entity thisMob;



    public humanClass(String name, List<mobEnums> mobs, Entity entity, String phrase, Integer boxNum){
        this.name = name;
        this.mobs = mobs;
        this.phrase = phrase;
        this.boxNum = boxNum;
        this.thisMob = entity;
    }

    public List<mobEnums> getMobs(){ return this.mobs;}

    public String getName(){ return this.name;}

    public String getPhrase(){return this.phrase;}

    public Entity getMob(){return this.thisMob;}

    public Integer getBoxNum(){return this.boxNum;}

    public void setMobs(List<mobEnums> newMobs){this.mobs = newMobs;}

    public void setMob(final int index, final mobEnums newMob){this.mobs.set(index, newMob);}

    public static class Trainer {
        private List<mobEnums> mobs;
        private Entity entityReference;
        public Trainer(Entity entity, List<mobEnums> mobs){
            this.mobs = mobs;
            this.entityReference = entity;
        }
    }
}
