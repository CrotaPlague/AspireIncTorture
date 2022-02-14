package aspireinc.torture.Files.ServerStorage.boxs;

import aspireinc.torture.Torture;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class boxDex {
    public static List<Location> returnBox(int boxNum){ // Boxes are the battle boxes where the mobs are spawned and then battle
        List<Location> list = new ArrayList<Location>();
        if(boxNum == 1){
            list.add(new Location(Torture.world,-208,90,603));
            list.add(new Location(Torture.world,-201,90,609));
        }
        return list;
    }
}
