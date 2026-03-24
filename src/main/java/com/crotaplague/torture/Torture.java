package com.crotaplague.torture;




import com.crotaplague.torture.Events.events;
import com.crotaplague.torture.Files.DataManager;
import com.crotaplague.torture.Files.ServerScriptService.DiscordWebhook;
import com.crotaplague.torture.Files.ServerScriptService.battleEngine;
import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.*;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.AnimationData;
import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.CQueue;
import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.ChainTask;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.humans.humanDex;
import com.crotaplague.torture.Files.ServerStorage.items.TItemManager;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.commands.VelocityCommands;
import com.crotaplague.torture.commands.cmdTabCompleter;
import com.crotaplague.torture.commands.cmds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.crotaplague.torture.Files.ServerScriptService.battleEngine.faceLoc;
import static com.crotaplague.torture.Files.ServerScriptService.randomScripts.inBattle;
import static com.crotaplague.torture.Files.ServerStorage.boxs.boxDex.*;

public final class Torture extends JavaPlugin {

//player.setWalkSpeed(0.2f);
//player.setFoodLevel(20);
    public static Map<ScheduledFuture, boolean[]> cancelPair = new HashMap<>();
    public static final int statLength = 6;
    public static Torture plugin;
    public static World world;
    public static AnimationData animationData;
    public static Boolean playerHasJoined = false;
    public static Map<String, Object> playerSpecifics = new HashMap<String, Object>();
    public static Map<String, SaveFile> playerSaveFiles = new HashMap<>();

    public static List<battleClass> battles = new ArrayList<>();

    public static DataManager data;
    public static RealData worldData;

    public static Entity toHide;
    public static String path;
    public static List<Entity> raycasting = new ArrayList<>();
    public static DiscordWebhook hook = new DiscordWebhook("https://discord.com/api/webhooks/947272743156596776/fFKovJYzy34llCLwvNhqfIy8777EYRuOPVA8jSNFE3JZ64s-e-1CiOoQlqpVRjd3ti_n");
    public static Map<Player, Integer> timeTracker = new HashMap<>();
    public static Map<ScheduledFuture, Mob> toCancel = new HashMap<>();
    //public static SussyBot bot;
    public static boolean closing;
    public static Runnable myRunTur;
    public static Location defaultHealLoc;

    @Override
    public void onEnable() {
        closing = false;
        try{
            ServerSocket ss=new ServerSocket(6666);
            Socket s=ss.accept();//establishes connection
            DataInputStream dis=new DataInputStream(s.getInputStream());
            path =(String)dis.readUTF();

            ss.close();
        }catch(Exception e){System.out.println(e);}

        plugin = this;
        new TItemManager();
        this.data = new DataManager(this);

        this.worldData = new RealData(this);
        this.animationData = new AnimationData(this);

        ItemManager.init();


        ConfigurationSerialization.registerClass(mobEnums.class);
        NamespacedKey entityIdKey = new NamespacedKey(Torture.getInstance(), "humanDexNum");
        getCommand("mobMe").setExecutor(new cmds());
        getCommand("sc").setExecutor(new cmds());
        getCommand("sc").setTabCompleter(new cmdTabCompleter());
        getCommand("MRBEAST").setExecutor(new cmds());
        getCommand("battle").setExecutor(new cmds());
        getCommand("openBox").setExecutor(new cmds());
        getCommand("save").setExecutor(new cmds());
        getCommand("addNPC").setExecutor(new cmds());
        getCommand("setNPC").setExecutor(new cmds());
        getCommand("setNPC").setTabCompleter(new cmdTabCompleter());
        getCommand("addItem").setExecutor(new cmds());
        getCommand("addItem").setTabCompleter(new cmdTabCompleter());
        getCommand("animation").setExecutor(new cmds());
        getCommand("animation").setTabCompleter(new cmdTabCompleter());
        getCommand("stage").setExecutor(new cmds());
        getCommand("stage").setTabCompleter(new cmdTabCompleter());
        getCommand("MRBEAST").setTabCompleter(new cmdTabCompleter());

        world = Bukkit.createWorld(new WorldCreator("world"));
        Location location = new Location(world, -132, 69, 712);

        Entity fakeEntity = world.spawnEntity(location, EntityType.VILLAGER);

        location = location.add(2, 0, 0);
        toHide = world.spawnEntity((location), EntityType.VILLAGER);
        fakeEntity.getPersistentDataContainer().set(entityIdKey, PersistentDataType.INTEGER, 1);
        LivingEntity livingEntity1 = (LivingEntity) fakeEntity;
        livingEntity1.setAI(false);

        defaultHealLoc = location(0, 100, 0);
        raycasting.add(fakeEntity);
        Bukkit.getPluginManager().registerEvents(new events(), this);


        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
                file.tickWentBy();
                NamespacedKey key = new NamespacedKey(plugin, "owner");
                NamespacedKey key1 = new NamespacedKey(plugin, "hiddenName");
                if(!inBattle(player)){
                    player.setFoodLevel(20);
                }
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
            for (Entity humans : raycasting) {
                // Only proceed for living entities
                if (!(humans instanceof LivingEntity)) continue;
                LivingEntity livingEntity = (LivingEntity) humans;

                // Raytrace players up to 35 blocks ahead
                Location eyeLoc = livingEntity.getEyeLocation();
                Vector dir = eyeLoc.getDirection();
                World world = livingEntity.getWorld();

                RayTraceResult trace = world.rayTraceEntities(
                        eyeLoc,
                        dir,
                        35,
                        // only consider players
                        entity -> entity instanceof Player
                );

                if (trace == null) continue;
                Entity hit = trace.getHitEntity();
                if (!(hit instanceof Player)) continue;
                Player player5 = (Player) hit;

                // Skip if player is already in a battle
                boolean inBattle = Torture.battles.stream()
                        .anyMatch(mover -> mover.getEntityCompetitors().contains(player5));
                if (inBattle) continue;

                String fullMsgKey = player5.getUniqueId() + " fullMessage";
                // If they've already got a full message queued, skip initial encounter
                if (Torture.playerSpecifics.get(fullMsgKey) != null) continue;

                // Trainer detection
                NamespacedKey key = new NamespacedKey(Torture.getInstance(), "humanDexNum");
                Integer dexNum = livingEntity.getPersistentDataContainer()
                        .get(key, PersistentDataType.INTEGER);
                SaveFile playerSave = playerSaveFiles.get(player5.getUniqueId().toString());
                humanClass human = humanDex.getHuman(dexNum, livingEntity);
                // Skip if they've already defeated this trainer
                player5.sendMessage("This is a thingy? " + dexNum + " " + playerSave.getDefeatedTrainers().toString());
                if (playerSave.hasDefeatedTrainer(human.getDexNum())) continue;

                // Hide the trainer and start battle
                player5.hideEntity(Torture.plugin, humans);

                playerSave.setOpponent(human.toAi());

                // Send action bar message or queue it
                if (Torture.playerSpecifics.get(fullMsgKey) == null) {
                    randomScripts.actionBarMessage(
                            player5,
                            human.getPhrase(),
                            SaveFile.MessagePurpose.TRAINER
                    );
                } else {
                    playerSave.addToQueue(human.getPhrase());
                }

                // Use your custom movement logic
                battleEngine.makeEntWalk(human.getMob(), player5, humans);
            }

            for(Map.Entry<String, Object> map : playerSpecifics.entrySet()){
                if(map.getKey().split(" ").length > 1){
                    if(map.getKey().split(" ")[1] == "hideForAllElse"){
                        List<Player> list = Bukkit.getOnlinePlayers().stream().filter(player -> player.getUniqueId() != (UUID.fromString(map.getKey().split(" ")[0]))).collect(Collectors.toList());
                        for(Player notHave : list){
                            Entity entity = (Entity) map.getValue();
                            notHave.hideEntity(Torture.plugin, entity);
                        }
                    }
                }
            }
            for(battleClass forLoopedBattle : battles){
                Bukkit.getScheduler().runTask(this, () -> {
                    CQueue<mobEnums> deadMobsQueue = forLoopedBattle.deadMobs();
                    processNext(deadMobsQueue);
                });
                Bukkit.getOnlinePlayers().forEach(player -> {
                    for (Entity entity : forLoopedBattle.getEntityCompetitors()){
                        if(entity instanceof Player){
                            SaveFile file = playerSaveFiles.get(entity.getUniqueId() + "");
                            file.getArmorStands().forEach(stand -> {Bukkit.getOnlinePlayers().forEach(playerfive ->  {if(playerfive.getUniqueId() != entity.getUniqueId()){ playerfive.hideEntity(this, stand);};});});
                        }
                        if(!forLoopedBattle.getEntityCompetitors().contains(player)){
                            player.hideEntity(this, entity);
                        }
                    }
                })

            ;}
            for(Map.Entry<Player, Integer> iner : timeTracker.entrySet()){
                int iVar = iner.getValue();
                iVar++;
                timeTracker.put(iner.getKey(), iVar);
            }
            for(Map.Entry<ScheduledFuture, Mob> entry : new HashMap<>(toCancel).entrySet()){
                if(!entry.getValue().hasAI()){ entry.getKey().cancel(true); toCancel.remove(entry.getKey()); Bukkit.getPlayer("CrotaPlague").sendMessage("done! :8yolsb:");}
            }
            for(Map.Entry<ScheduledFuture, boolean[]> entry : cancelPair.entrySet()){
                if(entry.getValue()[0]){ entry.getKey().cancel(true); Bukkit.getPlayer("CrotaPlague").sendMessage("terminated");}
            }

        }, 0L, 1L);

        myRunTur = ()->{
            while(true) {
                try {
                    VelocityCommands cmds = new VelocityCommands();
                    ServerSocket ss = new ServerSocket(6667);
                    Socket s = ss.accept();
                    DataInputStream dis = new DataInputStream(s.getInputStream());

                    String[] args = new String[2];
                    String command = dis.readUTF();
                    s.close();
                    s = ss.accept();
                    dis = new DataInputStream(s.getInputStream());
                    args[0] = dis.readUTF();

                    String out = cmds.runCommand(command, args);
                    s.close();
                    s = new Socket("localhost", 6668);

                    if (s.isConnected() && s.isBound()) {
                        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                        dout.writeUTF(out);
                        dout.flush();
                        dout.close();
                    }
                    ss.close();
                    s.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        hook.setContent("Torture server is enabled.");

        try {
            hook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //bot = new SussyBot();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, myRunTur);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Torture] plugin is now enabled!");




    }

    @Override
    public void onDisable() {
        if(world != null){
            for(Entity entity : world.getEntities()){
                if(!(entity instanceof Player) && !(entity instanceof ItemFrame)){
                    entity.getLocation().getChunk().load(true);
                    if(entity instanceof LivingEntity) ((LivingEntity) entity).damage(Integer.MAX_VALUE);
                    entity.remove();
                }
            }
        }
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            Component component = Component.text("Server closing!", NamedTextColor.RED);
            player.kick(component);
            PlayerQuitEvent event = new PlayerQuitEvent(player, Component.text("Server closing!", NamedTextColor.RED), PlayerQuitEvent.QuitReason.KICKED);
            events.playerLeaveEvent(event);

        }

        File file = new File("temp.yml");
        file.delete();

        hook.setContent("Torture server is disabled.");

        try {
            hook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Torture] plugin is now disabled!");

    }



    public static void openBox(int boxNum, Player player){
        Inventory inv = Bukkit.createInventory(player, 27, "mob box " + boxNum);

        player.openInventory(inv);
        ItemStack itemStack;
        ItemMeta itemMeta;
        mobEnums mob = null;
        SaveFile.MobBox box;
        Map<Integer, mobEnums> hotbar = Torture.playerSaveFiles.get(player.getUniqueId() + "").getPlayerMobs();
        Map<Integer, mobEnums> hotbar2 = new HashMap<>(hotbar);
        for(int i = 0; i<9; i++){


            if(i < hotbar.values().toArray().length){
                mob = (mobEnums) hotbar.values().toArray()[i];
                itemStack = mob.getIcon();
                itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§f" + mob.getName());
                NamespacedKey key = new NamespacedKey(Torture.plugin, "noStack");
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (i*(-1)));
                itemStack.setItemMeta(itemMeta);
                mob.setSlotInMenu((i*(-1)));
                hotbar2.put(new ArrayList<>(hotbar.keySet()).get(i), mob);
                player.getInventory().setItem(i, itemStack);
            }

        }
        if(!(Torture.playerSaveFiles.get(player.getUniqueId() + "").getMobBoxes().containsKey(boxNum))){
            Torture.playerSaveFiles.get(player.getUniqueId() + "").getMobBoxes().put(boxNum, new HashMap<>());
        }
        Map<Integer, mobEnums> mobBox = Torture.playerSaveFiles.get(player.getUniqueId() + "").getMobBoxes().get(boxNum);
        Map<Integer, mobEnums> mobBox2 = Torture.playerSaveFiles.get(player.getUniqueId() + "").getMobBoxes().get(boxNum);
        for(int i = 0; i<28; i++){
            if(mobBox.containsKey(i)){
                mob = mobBox.get(i);
                mob.setSlotInMenu(i);
                mobBox2.put(i, mob);
                itemStack = mob.getIcon();
                itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§f" + mob.getName());
                NamespacedKey key = new NamespacedKey(Torture.plugin, "noStack");
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
                itemStack.setItemMeta(itemMeta);
                inv.setItem(i, itemStack);
            }

        }
        box = new SaveFile.MobBox(mobBox2, hotbar2);
        playerSpecifics.put(player.getUniqueId() + " openPCInv", box);
        playerSpecifics.put(player.getUniqueId() + " boxNum", boxNum);
    }

    public static mobEnums theGreatEqualizer(mobEnums mob){
        int chooser = 0;
        int[] stats;

        for(int i = 0; i <= mob.getLevel(); i++){
            Random rand = new Random();

            chooser = rand.nextInt(5);
             stats = mob.getStats();
             for(int i1 = 0; i1 <= stats.length-1; i1++){
                 stats[i1] = stats[i1]+1;
             }
             stats[chooser] = stats[chooser]+1;
        }
        return mob;
    }

    public static Torture getInstance(){
        return plugin;
    }

    @Nullable
    public static UUID getUserId(String username){
        UUID id = null;
        try{
            id = Bukkit.getPlayer("").getUniqueId();
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
        return id;
    }

    public static double lerp(double start, double end, double step){
        return start + (end - start) * step;
    }

    private void processNext(CQueue<mobEnums> queue) {
        if (queue.isEmpty()) {
            return; // done processing all dead mobs
        }

        mobEnums mob = queue.poll();
        removeMob(mob, () -> processNext(queue));
    }

    public static void removeMob(mobEnums mob){
        removeMob(mob, null);
    }


    public static void removeMob(mobEnums mob, Runnable onComplete) {
        Creature creature = (Creature) mob.getSelf();
        World world = creature.getWorld();
        double endX = creature.getLocation().getX();
        Vector vec = creature.getEyeLocation().getDirection().normalize().multiply(-2.9);
        Location loc = creature.getEyeLocation().add(vec);
        loc.setY(loc.getY());
        loc.setZ(creature.getZ());

        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(mob.getMobBall().getDisplayItem());
        stand.setInvisible(true);

        faceLoc(stand, creature);

        Location og = stand.getLocation();
        double[] x = {0};
        Location[] armorHead = {null};

        // Runnable to execute when stand reaches the mob
        Runnable finalizeRemoval = () -> {
            Location headLoc = armorHead[0];
            world.getBlockAt(headLoc).setType(mob.getMobBall().getDisplayItem().getType());
            ShulkerBox box = (ShulkerBox) world.getBlockAt(headLoc).getState();
            box.open();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, box::close, 12L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                world.getBlockAt(headLoc).setType(Material.AIR);
                creature.remove();
                if(onComplete != null){
                    onComplete.run();
                }
            }, 24L);
        };

        // Animate the armor stand towards the creature
        AtomicInteger taskId = new AtomicInteger();
        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                double y = -(0.6 * x[0] * x[0]) + (1.6 * x[0]) + og.getY();
                Location current = stand.getLocation();

                if (current.getX() < creature.getLocation().getX()) {
                    current.add(0.1, 0, 0);
                } else {
                    current.add(-0.1, 0, 0);
                }

                current.setY(y);
                stand.teleport(current);
                x[0] += 0.12;

                if (Math.abs(current.getX() - endX) < 0.4) {
                    armorHead[0] = stand.getEyeLocation();
                    stand.remove();
                    Bukkit.getScheduler().runTask(plugin, finalizeRemoval);
                    Bukkit.getScheduler().cancelTask(taskId.get());
                }
            }
        }, 0L, 1L);
        taskId.set(id);
    }

    public static void runSequential(ChainTask... tasks) {
        runTasksSequentially(0, tasks);
    }
    public static void runSequential(List<ChainTask> l) {
        runTasksSequentially(0, l.toArray(ChainTask[]::new));
    }

    private static void runTasksSequentially(int index, ChainTask[] tasks) {
        if (index >= tasks.length) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            tasks[index].run(() -> runTasksSequentially(index + 1, tasks));
        });
    }

    public static void runSequentialAndWait(List<ChainTask> l) {
        CountDownLatch latch = new CountDownLatch(1);
        runTasksSequentially(0, l.toArray(ChainTask[]::new), latch);
        try {
            latch.await(); // Blocks until latch.countDown() is called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runTasksSequentially(int index, ChainTask[] tasks, CountDownLatch latch) {
        if (index >= tasks.length) {
            latch.countDown(); // Signal completion
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            tasks[index].run(() -> runTasksSequentially(index + 1, tasks, latch));
        });
    }


}



//§




//§ ☗