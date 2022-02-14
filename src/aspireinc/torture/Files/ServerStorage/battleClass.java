package aspireinc.torture.Files.ServerStorage;

import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public class battleClass {

    public enum battleTypes{
        SOLOPVP, DOUBLEPVP, SOLOPVE, DOUBLEPVE;
    }

    private final Map<Player, Boolean> competitors;
    private Map<Entity, mobEnums> mobs;
    private final Map<Entity, Entity> entities;
    private final battleTypes battleType;


    public battleClass(Map<Player, Boolean> competitors, Map<Entity, mobEnums> mobs, Map<Entity, Entity> entities, battleTypes battleType){
        this.competitors = competitors;
        this.mobs = mobs;
        this.entities = entities;
        this.battleType = battleType;
    }

    public Map<Player, Boolean> getCompetitors(){return this.competitors;}

    public void setPlayerCondition(Player player, Boolean ready){this.competitors.put(player, ready);}

    public Map<Entity, Entity> getEntities(){return this.entities;}

    public Map<Entity, mobEnums> getMobs(){return this.mobs;}
    public void setMobs(final Map<Entity, mobEnums> map){this.mobs = map;}

    public battleTypes getBattleType(){return this.battleType;}

}
