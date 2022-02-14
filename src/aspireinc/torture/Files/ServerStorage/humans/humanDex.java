package aspireinc.torture.Files.ServerStorage.humans;

import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

import static aspireinc.torture.Files.ServerStorage.mobs.mobDex.mobDex1;

public class humanDex {
    public static humanClass getHuman(int input, Entity entity){
        humanClass human = null;
        List<mobEnums> mobs = new ArrayList<mobEnums>();

        if(input == 1){
            mobs.add(mobDex1(1, 10));
            mobs.add(mobDex1(3, 10));
            human = new humanClass("§cgjery", mobs, entity, "hiiii", null);
        }
        return human;
    }
}
