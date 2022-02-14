package aspireinc.torture;

import aspireinc.torture.Files.DataManager;
import aspireinc.torture.Files.DataManager2;
import aspireinc.torture.Files.ServerScriptService.ReflectionUtils;
import aspireinc.torture.Files.ServerScriptService.battleEngine;
import aspireinc.torture.Files.ServerScriptService.randomScripts;
import aspireinc.torture.Files.ServerStorage.ItemManager;
import aspireinc.torture.Files.ServerStorage.battleClass;
import aspireinc.torture.Files.ServerStorage.humans.humanClass;
import aspireinc.torture.Files.ServerStorage.humans.humanDex;
import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import aspireinc.torture.commands.cmds;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.world.entity.EntityInsentient;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class Torture extends JavaPlugin {

//player.setWalkSpeed(0.2f);
//player.setFoodLevel(20);
    public static Torture plugin;
    public static World world;

    public static Map<String, Object> playerSpecifics = new HashMap<String, Object>();


    public static List<battleClass> battles = new ArrayList<>();

    public static DataManager data;

    public static DataManager2 temp;

    public static Entity toHide;

    @Override
    public void onEnable() {

        plugin = this;
        ItemManager.init();

        ConfigurationSerialization.registerClass(mobEnums.class);
        NamespacedKey entityIdKey = new NamespacedKey(Torture.getInstance(), "humanDexNum");

        getCommand("mobMe").setExecutor(new cmds());
        getCommand("openBox").setExecutor(new cmds());

        world = Bukkit.createWorld(new WorldCreator("world"));

        Location location = new Location(world, -275f, 79f, 438f);


        Entity fakeEntity = world.spawnEntity(location, EntityType.VILLAGER);

        location = location.add(2, 0, 0);
        toHide = world.spawnEntity((location), EntityType.VILLAGER);
        fakeEntity.getPersistentDataContainer().set(entityIdKey, PersistentDataType.INTEGER, 1);
        LivingEntity livingEntity1 = (LivingEntity) fakeEntity;
        livingEntity1.setAI(false);


        List<Entity> raycasting = new ArrayList<>();
        raycasting.add(fakeEntity);


        getServer().getPluginManager().registerEvents(new aspireinc.torture.Events.events(), this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                NamespacedKey key = new NamespacedKey(plugin, "owner");
                NamespacedKey key1 = new NamespacedKey(plugin, "hiddenName");
                if(world != null){
                    List<Entity> ents =  world.getEntities().stream().filter(entity2 -> {
                        if(!(entity2.getPersistentDataContainer().has(key, PersistentDataType.STRING))) return false;
                        if(Objects.requireNonNull(entity2.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase(player.getUniqueId().toString()))){
                            if(entity2.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return false;
                            if(entity2.getPersistentDataContainer().get(key1, PersistentDataType.STRING).equalsIgnoreCase("Walking to")){
                                return true;
                            }
                        }
                        return false;
                    }).collect(Collectors.toList());
                    if(ents.size() != 0) {
                        for (Entity entity : ents) {
                            if (entity.getLocation().distanceSquared(Objects.requireNonNull(Bukkit.getEntity(UUID.fromString(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING)))).getLocation()) < 1.5) {
                                Player player1 = (Player) Bukkit.getEntity(UUID.fromString(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                            }
                        }
                    }
                }
            }
            for(Entity humans : raycasting) {
                LivingEntity livingEntity = (LivingEntity) humans;
                Vector vector = livingEntity.getLocation().getDirection();
                Vector vector1 = vector;
                for (int i = 0; i < 35; i++) {
                    vector.add(vector1);
                    Location loc = livingEntity.getLocation().add(vector);
                    loc.setY(loc.getY() + 0.5);
                    List<Player> closePlayers = Bukkit.getOnlinePlayers().stream().filter(player5 -> player5.getLocation().distanceSquared(loc) < 1.5).collect(Collectors.toList());
                    for (Player player5 : closePlayers) {

                        if ((Torture.battles.stream().filter(mover -> mover.getCompetitors().containsKey(player5)).collect(Collectors.toList()).size() == 0)) {
                            if(Torture.playerSpecifics.get(player5.getUniqueId() + " fullMessage") == null){
                                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(humans.getEntityId());
                                ReflectionUtils.sendPacket(player5, packet);
                                NamespacedKey key = new NamespacedKey(Torture.getInstance(), "humanDexNum");
                                humanClass human = humanDex.getHuman(humans.getPersistentDataContainer().get(key, PersistentDataType.INTEGER),humans);

                                randomScripts.actionBarMessage(player5, human.getPhrase());

                                battleEngine.makeEntWalk(human.getMob(), player5, humans);
                            }else{


                                Entity setTarget = (Entity) Torture.playerSpecifics.get(player5.getUniqueId() + " hideForAllElse");

                                Vector vec = humans.getLocation().toVector();
                                vec = player5.getLocation().toVector().subtract(vec);

                                if(Math.abs(vec.getX()) < Math.abs(vec.getZ())){
                                    if(vec.getX() < 0){
                                        vec.add(new Vector(0.5, 0, 0));
                                    }else{
                                        vec.add(new Vector(-0.5, 0, 0));
                                    }
                                }else{
                                    if(vec.getZ() < 0){
                                        vec.add(new Vector(0, 0, 0.5));
                                    }else{
                                        vec.add(new Vector(0, 0, -0.5));
                                    }
                                }

                                Location targetLoc = humans.getLocation();
                                targetLoc = targetLoc.add(vec);
                                float f = 1;
                                ((EntityInsentient) ((CraftEntity) setTarget).getHandle()).getNavigation().a(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), f);


                            }

                        }

                    }
                }
            }
            for(Map.Entry<String, Object> map : playerSpecifics.entrySet()){
                if(map.getKey().split(" ")[1] == "hideForAllElse"){
                    List<Player> list = Bukkit.getOnlinePlayers().stream().filter(player -> player.getUniqueId() != (UUID.fromString(map.getKey().split(" ")[0]))).collect(Collectors.toList());
                    for(Player notHave : list){
                        try {
                            CraftPlayer craftPlayer = (CraftPlayer) notHave;
                            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                            Entity entity = (Entity) map.getValue();
                            packet.getModifier().write(0, new int[]{entity.getEntityId()});
                            ProtocolLibrary.getProtocolManager().sendServerPacket(craftPlayer, packet);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            for(battleClass forLoopedBattle : battles){
                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                Bukkit.getOnlinePlayers().stream().filter(player -> !(forLoopedBattle.getCompetitors().containsKey(player))).forEach(player -> {
                    for (Map.Entry<Entity, Entity> map: forLoopedBattle.getEntities().entrySet()){
                        packet.getModifier().write(0,  map.getValue());
                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    })

                ;}
        }, 0L, 1L);

        this.data = new DataManager(this);

        this.temp = new DataManager2(this);

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Torture is now enabled!");


    }

    @Override
    public void onDisable() {
        if(world != null){
            for(Entity entity : world.getEntities()){
                if(!(entity instanceof Player)){
                    entity.remove();
                }

            }
        }
        File file = new File("temp.yml");
        file.delete();


    }



    public static void openBox(int boxNum, Player player){
        Inventory inv = Bukkit.createInventory(player, 27, "mob box " + boxNum);

        player.openInventory(inv);
        ItemStack itemStack;
        ItemMeta itemMeta;
        mobEnums mob = null;
        for(int i = 0; i<9; i++){
            if(Torture.data.getConfig().contains("player " + player.getUniqueId() + " hotbar " + i)){

                assert mob != null;
                ConfigurationSection section = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " hotbar " + i);
                if(section == null){
                    player.sendMessage(String.valueOf(i));
                }else{
                    Map<String, Object> map = section.getValues(false);
                    mob = mobEnums.deserialize(map);
                    itemStack = mob.getIcon();
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§f" + mob.getName());
                    NamespacedKey key = new NamespacedKey(Torture.plugin, "noStack");
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (i*(-1)));
                    itemStack.setItemMeta(itemMeta);
                    player.getInventory().setItem(i-1, itemStack);
                }

            }
        }
        for(int i = 1; i<28; i++){
            if(Torture.data.getConfig().contains("player " + player.getUniqueId() + " box" + boxNum + " slot " + i)){
                ConfigurationSection section = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " box" + boxNum + " slot " + i);
                if(section == null){
                    player.sendMessage(String.valueOf(i));
                }else {
                    Map<String, Object> map = section.getValues(false);
                    mob = mobEnums.deserialize(map);
                    itemStack = mob.getIcon();
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§f" + mob.getName());
                    NamespacedKey key = new NamespacedKey(Torture.plugin, "noStack");
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
                    itemStack.setItemMeta(itemMeta);
                    inv.setItem(i - 1, itemStack);
                }
            }

        }
    }

    public static mobEnums theGreatEqualizer(mobEnums mob){
        int chooser = 0;
        List<Integer> stats;

        for(int i = 0; i <= mob.getLevel(); i++){
            Random rand = new Random();

            chooser = rand.nextInt(5);
             stats = mob.getStats();
             for(int i1 = 0; i1 <= stats.size()-1; i1++){
                 stats.set(i1, stats.get(i1)+1);
             }
             stats.set(chooser, stats.get(chooser)+1);
        }
        return mob;
    }

    public static Torture getInstance(){
        return plugin;
    }


}



//§




//§