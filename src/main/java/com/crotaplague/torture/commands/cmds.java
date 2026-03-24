package com.crotaplague.torture.commands;



import com.crotaplague.torture.Files.DataManager;
import com.crotaplague.torture.Files.ServerScriptService.battleEngine;
import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.AnimationManager;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.Stage;
import com.crotaplague.torture.Files.ServerStorage.SaveFile;
import com.crotaplague.torture.Files.ServerStorage.battleTypes;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.items.TItemDex;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Torture;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.crotaplague.torture.Files.ServerStorage.mobs.mobDex.mobDex1;
import static com.crotaplague.torture.Torture.*;

import static com.crotaplague.torture.Files.ServerScriptService.randomScripts.*;

public class cmds implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("openBox") && args.length > 0){
            try{
                Torture.openBox(Integer.parseInt(args[0]), player);
            }catch(IndexOutOfBoundsException ex){
                ex.printStackTrace();
            }

        }
        if(cmd.getName().equals("MRBEAST")){
            Location loc = player.getLocation();
            List<Location> locs = randomScripts.generateSphere(loc, 3, true);
            List<String> names = new ArrayList<>();
            names.add("Technoblade"); names.add("Dream"); names.add("BadBoyHalo"); names.add("GeorgeNotFound"); names.add("Sapnap"); names.add("Chandler_Hallow");
            names.add("mrbeastgaming1"); names.add("Chris_MemeGod"); names.add("KarlJacobs");
            for(Location loc2 : locs){
                if(randomScripts.getRandomNumber(0, 2) == 1 && Math.abs(loc2.getY()-player.getLocation().getY()) < 1.5){
                    String youtuber =names.get(randomScripts.getRandomNumber(0, 8));
                    Disguise disguise = new PlayerDisguise(youtuber);
                    Entity en = world.spawnEntity(loc2, EntityType.VILLAGER);
                    DisguiseAPI.disguiseEntity(en, disguise);
                    NamespacedKey key = new NamespacedKey(plugin, "BESAT");
                    en.getPersistentDataContainer().set(key, PersistentDataType.STRING, youtuber);
                }
            }
        }
        if(cmd.getName().equals("save")){
            Location loc = player.getLocation();
            AtomicBoolean canSave = new AtomicBoolean(true);
            Torture.battles.forEach(battle -> {if(battle.getCompetitors().stream().filter(entry -> entry.getSelf().equals(player)).collect(Collectors.toList()).size() != 0) canSave.set(false);});
            if(canSave.get()){
                if(Torture.playerSaveFiles.get(player.getUniqueId() + "") != null){
                    try{
                        SaveFile pSave = Torture.playerSaveFiles.get(player.getUniqueId() + "");
                        pSave.setPlayerLoc(loc);
                        DataManager pData = pSave.getDataFile();
                        pData.getConfig().set(player.getUniqueId() + " saveFile", pSave.serialize());
                        pData.saveConfig();
                    }catch(Exception ex){
                        ex.printStackTrace();
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 100, 1);
                        player.sendMessage(ChatColor.RED + "There was an error saving your data, please retry!");
                    }


                }else{
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 100, 1);
                    player.sendMessage(ChatColor.RED + "There was an error saving your data, please retry!");
                }

            }else{
                player.sendMessage(ChatColor.RED + "Can not save at this time.");
            }

        }

        if(cmd.getName().equalsIgnoreCase("mobMe")){
            if(args.length < 2){
                player.sendMessage(ChatColor.RED + "At least 2 arguments are needed.");
                return true;
            }
            mobEnums mob = mobDex1(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            mob = theGreatEqualizer(mob);
            int i;
            int foundOne = 0;
            SaveFile playerSaveFile = getPlayerSaveFile(player);
            if(playerSaveFile != null){
                if(!(playerSaveFile.getPlayerMobs().size() < 6)){
                    player.sendMessage("Sorry, hotbar full!");
                }else {
                    Map<Integer, mobEnums> mobs = playerSaveFile.getPlayerMobs();
                    player.sendMessage(String.valueOf(mobs.size()+1));
                    playerSaveFile.setPlayerMob(mobs.size(), mob);

                }
            }else{
                MemorySection section = (MemorySection) Torture.data.getConfig().get(player.getUniqueId() + " saveFile");
                playerSaveFile = SaveFile.deserialize(section);
                Map<Integer, mobEnums> map2 = new HashMap<>();
                map2.put(1, mob);
                playerSaveFile.setPlayerMobs(map2);
                Torture.playerSaveFiles.put(player.getUniqueId() + "", playerSaveFile);
            }
            mob.setTrainer(playerSaveFile.createTrainer());

        }

        if(cmd.getName().equalsIgnoreCase("addNPC")){
            NamespacedKey key = new NamespacedKey(Torture.plugin, "humanDexNum");
            if(args.length < 1){
                player.sendMessage(Color.RED + "Wrong argument count, correct format is /addNPC <username>!");
                return true;
            }
            Location playerLoc = player.getLocation();
            PlayerDisguise disguise = new PlayerDisguise("", args[0]);
            Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
            villager.setSilent(true);
            villager.setAI(false);
            DisguiseAPI.disguiseEntity(villager, disguise);
            int count = 0;
            if(worldData.getConfig().contains("Entity count")){
                count = worldData.getConfig().getInt("Entity count");
                count = count + 1;
            }else{
                count = 1;
            }

            Map<String, Location> map = new HashMap<>();
            map.put(DisguiseAPI.parseToString(disguise), playerLoc);

            worldData.getConfig().set("spawnNPC " + count, map);
            worldData.getConfig().set("Entity count", count);
            villager.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, count);
            worldData.getConfig().set("humanDexNum " + count, new humanClass("").serialize());
            worldData.saveConfig();


        }

        if(player.isOp()){
            if(cmd.getName().equalsIgnoreCase("battle")){
                if(args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
                    List<Player> players = new ArrayList<>();
                    players.add(player);
                    try {
                        SaveFile file = getPlayerSaveFile(player);
                        humanClass.Trainer t = file.createTrainer();
                        Player p = Bukkit.getPlayer(args[0]);
                        humanClass.Trainer op = getPlayerSaveFile(p).createTrainer();
                        mobEnums m = op.getFirstMobAlive();
                        player.sendMessage("The op mob: " + m + " " + m.hasTrainer() + " " + m.getTrainer());
                        battleEngine.startBattle(t, battleTypes.SOLOPVP, 1, m);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if(cmd.getName().equalsIgnoreCase("setNPC")){
                if(args.length < 2) return false;
                World world = player.getWorld();
                RayTraceResult rayTraceResult = world.rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 7, 0, entity -> {if(!(entity.equals(player))) return true; else return false;});
                if(!(rayTraceResult.getHitEntity() instanceof LivingEntity)) return true;
                LivingEntity tracedEnt = (LivingEntity) rayTraceResult.getHitEntity();
                if(tracedEnt != null){
                    if(DisguiseAPI.isDisguised(tracedEnt)) sender.sendMessage("yeah");
                    NamespacedKey key = new NamespacedKey(Torture.plugin, "humanDexNum");
                    int hDexNum = tracedEnt.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    ConfigurationSection configurationSection = worldData.getConfig().getConfigurationSection("humanDexNum " + hDexNum);
                    Map<String, Object> data = configurationSection.getValues(true);
                    player.sendMessage(data.entrySet().stream().findFirst().get().getKey());
                    humanClass human = humanClass.deserialize(data);
                    if(args[0].equalsIgnoreCase("setMob")){
                        if(args.length < 4){
                            player.sendMessage(Component.text("The arguments for that command are /setnpc setmob <slot> <mobDexNum> <level>", NamedTextColor.RED));
                            return true;
                        }
                        // /setNPC <setMob> <slot> <mobDexNum> <level>
                        human.setMob((Integer.parseInt(args[1])-1), Torture.theGreatEqualizer(mobDex1(Integer.parseInt(args[2]), Integer.parseInt(args[3]))));
                    }
                    if(args[0].equalsIgnoreCase("getMobs")){
                        AtomicReference<Boolean> fair = new AtomicReference<>(false);
                        human.getMobs() .forEach(mob -> {if(mob.getMojangMobType() != null) if(mob.getLevel() != 0){ player.sendMessage("Level " + mob.getLevel() + " " + mob.getType().toString()); fair.set(true);} if(!fair.get()) player.sendMessage("Empty");});
                    }
                    if(args[0].equalsIgnoreCase("setPhrase")){
                        StringJoiner joined = new StringJoiner(" ");
                        boolean oneTime = false;
                        for(String word : args){
                            if(oneTime){
                                joined.add(word);
                            }else{
                                oneTime = true;
                            }
                        }
                        human.setPhrase(joined.toString());
                    }
                    if(args[0].equalsIgnoreCase("setNPCType")){
                        human.setType(Integer.parseInt(args[1]));
                        if(!(Torture.raycasting.contains(tracedEnt))){
                            if(human.getType() == 2){
                                Torture.raycasting.add(tracedEnt);
                            }

                        }
                        human.setThisMob(tracedEnt);
                    }
                    if(args[0].equals("setCashReward")){
                        human.setCashReward(Integer.parseInt(args[1]));
                    }
                    if(args[0].equalsIgnoreCase("setName")){
                        StringBuilder sb = new StringBuilder();
                        for(int i = 1; i < args.length; i++){
                            sb.append(args[i]);
                            if(i < args.length - 1){
                                sb.append(" ");
                            }
                        }
                        human.setName(sb.toString());
                    }
                    worldData.getConfig().set("humanDexNum " + hDexNum, human.serialize());

                }
                worldData.saveConfig();
            }
            if(cmd.getName().equalsIgnoreCase("addItem")){
                Location location = player.getLocation();
                location.setPitch(0); location.setYaw(0);
                location.setY(location.getY() - 1.85);
                //itemDex, Amount
                List<Object> toConfig = new ArrayList<>();
                int itemNum;
                if(args.length == 2){
                    ItemClass item = TItemDex.getItem(Integer.parseInt(args[0]));
                    if(item == null){
                        player.sendMessage(Component.text("No item with that registry number", NamedTextColor.RED));
                        return true;
                    }
                    if(!worldData.getConfig().contains("worldItemCount")){
                        worldData.getConfig().set("worldItemCount", 0);
                        toConfig.add(location);
                        toConfig.add(Integer.parseInt(args[0])); //item

                        toConfig.add(Integer.parseInt(args[1])); //amount
                        worldData.getConfig().set("worldItem " + 0, toConfig);

                        itemNum= 0;
                    }else{
                        toConfig.add(location);
                        toConfig.add(Integer.parseInt(args[0])); //item
                        toConfig.add(Integer.parseInt(args[1])); //amount

                        itemNum = ((int)worldData.getConfig().get("worldItemCount") + 1);
                        worldData.getConfig().set("worldItem " + ((int)worldData.getConfig().get("worldItemCount") + 1), toConfig);
                        worldData.getConfig().set("worldItemCount", ((int)worldData.getConfig().get("worldItemCount") + 1));
                    }
                    ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                    armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.RED_SHULKER_BOX));
                    armorStand.setInvulnerable(true);
                    armorStand.setGravity(false);
                    armorStand.setInvisible(true);
                    armorStand.setArms(true);
                    EulerAngle angle = new EulerAngle(Math.PI - 0.4,0,0);
                    armorStand.setRightArmPose(angle);
                    armorStand.setFireTicks(Integer.MAX_VALUE);
                    armorStand.setMarker(true);

                    Vector vec = armorStand.getLocation().getDirection();
                    vec.setY(0);
                    vec = vec.normalize();
                    vec.multiply(-1);
                    if(Math.abs(vec.getX()) > Math.abs(vec.getZ())){
                        vec.setX(vec.getX()/1.5);
                    }else if(Math.abs(vec.getZ()) > Math.abs(vec.getX())){
                        vec.setZ(vec.getZ()/1.5);
                    }
                    player.sendMessage(vec.toString());
                    Location loc = armorStand.getLocation().add(vec);

                    double yawRightHandDirection = Math.toRadians(-1 * loc.getYaw() - 45);
                    double x = 0.5 * Math.sin(yawRightHandDirection) + loc.getX();
                    double y = loc.getY() + 1.65;
                    double z = 0.5 * Math.cos(yawRightHandDirection) + loc.getZ();
                    Location l = new Location(loc.getWorld(), x, y, z);
                    MagmaCube puffer = (MagmaCube) loc.getWorld().spawnEntity(l, EntityType.MAGMA_CUBE);
                    puffer.setSize(1);
                    puffer.setAI(false);
                    puffer.setInvulnerable(true);
                    puffer.setInvisible(true);
                    NamespacedKey key = new NamespacedKey(Torture.plugin, "itemNumber");
                    puffer.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemNum);
                    worldData.saveConfig();

                }else{
                    player.sendMessage("Wrong args!");
                }
            }
            if(cmd.getName().equalsIgnoreCase("stage")){
                if(args.length >= 2){
                    if(args[0].equalsIgnoreCase("create")){
                        Stage s = new Stage(args[1]);
                        s.setStartLocation(player.getLocation());
                        animationData.getConfig().set("stage " + s.getName(), s.serialize());
                        playerSpecifics.put(player.getUniqueId() + " SelectedStage", s);
                        player.sendMessage(s.getName() + " has been selected.");
                    }
                    if(args[0].equalsIgnoreCase("select")){
                        if(animationData.getConfig().contains("stage " + args[1])){
                            Stage s = Stage.deserialize((MemorySection) animationData.getConfig().get("stage " + args[1]));
                            player.sendMessage(String.valueOf(s.getAnimations().size() + " " + s.getName()));
                            playerSpecifics.put(player.getUniqueId() + " SelectedStage", s);
                        }else{
                            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 2f, 1f);
                            player.sendMessage("§cThere is no stage with that name.");
                        }

                    }
                }
                if(args.length > 0){
                    if(args[0].equalsIgnoreCase("viewAnimations")){
                        if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")){
                            Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                            randomScripts.openStageAnimations(s, player, Component.text("Animations: "));
                        }

                    }
                   if(args[0].equalsIgnoreCase("removeAnimation")) {
                        if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")) {
                            Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                            randomScripts.openStageAnimations(s, player, Component.text("Select Animation To Remove:"));

                        }
                    }
                   if(args[0].equalsIgnoreCase("play")){
                       if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")) {
                           Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                           s.setViewer(player);
                           s.run();
                       }
                   }
                   if(args[0].equalsIgnoreCase("returnLocation")){
                       if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")){
                           Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                           s.setEndLocation(player.getLocation());
                           animationData.getConfig().set("stage " + s.getName(), s.serialize());
                       }
                   }
                   if(args[0].equalsIgnoreCase("startLocation")){
                       if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")){
                           Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                           s.setStartLocation(player.getLocation());
                           animationData.getConfig().set("stage " + s.getName(), s.serialize());
                       }
                   }
                }
                animationData.saveConfig();
            }
            if(cmd.getName().equalsIgnoreCase("animation")){
                if(!playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")){
                    player.sendMessage("Please select a stage by running: /stage select <name>");
                    return true;
                }
                Stage stage = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
                if(args.length > 1){
                    if(args[0].equalsIgnoreCase("addMob")){
                        Entity ent = null;
                        AnimationManager man = new AnimationManager(ent);
                        PlayerDisguise disguise = new PlayerDisguise("", args[1]);
                        Villager v = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                        DisguiseAPI.disguiseEntity(v, disguise);
                        man.setDisguise(DisguiseAPI.parseToString(disguise));
                        man.setGoal(player.getLocation());
                        Location endFace = player.getEyeLocation();
                        Vector vec = player.getEyeLocation().toVector().normalize().multiply(0.25);
                        man.setEndFace(endFace.add(vec));
                        playerSpecifics.put(player.getUniqueId() + " SelectedAnimation", man);
                        if(stage.getAnimations().size() > 0)
                        openStageAnimations(stage, player);
                        else stage.getAnimations().add(man);
                        v.remove();
                    }
                    if(args[0].equalsIgnoreCase("addComment")){
                        if(!playerSpecifics.containsKey(player.getUniqueId() + " SelectedAnimation")){
                            player.sendMessage(ChatColor.RED + "No animation selected!!");
                            return true;
                        }
                        AnimationManager man = (AnimationManager) playerSpecifics.get(player.getUniqueId() + " SelectedAnimation");
                        String full = "";
                        if(args.length < 2){
                            player.sendMessage("Must have at least two arguments!!");
                            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 1, 1);
                            return true;
                        }
                        for(int i = 1; i < args.length; i++){
                            full += args[i];
                        }
                        man.setComments(full);
                    }
                    animationData.getConfig().set("stage " + stage.getName(), stage.serialize());
                    animationData.saveConfig();
                }
                if(args.length > 0){
                    if(args[0].equalsIgnoreCase("moveMob")){
                        Entity ent = null;
                        AnimationManager man = new AnimationManager(ent);
                        man.setGoal(player.getLocation());
                        Vector dir = player.getEyeLocation().toVector().normalize().multiply(0.25);
                        man.setEndFace(player.getEyeLocation().add(dir));
                        playerSpecifics.put(player.getUniqueId() + " SelectedAnimation", man);
                        openStageAnimations(stage, player, Component.text("Select Mob To Move"));
                    }
                    if(args[0].equalsIgnoreCase("cameraTeleport")){
                        AnimationManager man = new AnimationManager(player.getLocation());
                        playerSpecifics.put(player.getUniqueId() + " SelectedAnimation", man);
                        if(stage.getAnimations().size() > 0)
                            openStageAnimations(stage, player);
                        else stage.getAnimations().add(man);
                    }
                }
            }
            if(cmd.getName().equals("sc")){
                if(player.getUniqueId().toString().equals("4d959091-e12c-4269-afc5-e15d8b91bd7e"))
                if(args.length > 0){
                    if(args[0].equals("close")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    }
                }
            }
        }


        return true;
    }
}
