package aspireinc.torture.commands;



import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import aspireinc.torture.Torture;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static aspireinc.torture.Files.ServerStorage.mobs.mobDex.mobDex1;

import static aspireinc.torture.Torture.theGreatEqualizer;

public class cmds implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("openBox")){
            Torture.openBox(Integer.parseInt(args[0]), player);
        }
        if(cmd.getName().equalsIgnoreCase("mobMe")){
            mobEnums mob = mobDex1(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            mob = theGreatEqualizer(mob);
            int i;
            int foundOne = 0;
            for(i = 1; i<10; i++){
                if(!(Torture.data.getConfig().contains("player " + player.getUniqueId() + " hotbar " + i))){
                    foundOne=i;
                    break;
                }
            }
            if(foundOne == 0){
                player.sendMessage("Sorry, hotbar full!");
            }else {
                Torture.data.getConfig().set("player " + player.getUniqueId() + " hotbar " + i, mob.serialize());
                Torture.data.saveConfig();
            }
        }

        try {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntLists().write(0, Collections.singletonList(Torture.toHide.getEntityId()));
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }
}
