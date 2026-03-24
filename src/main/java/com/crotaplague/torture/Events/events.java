package com.crotaplague.torture.Events;


import com.crotaplague.torture.Files.DataManager;
import com.crotaplague.torture.Files.ServerScriptService.Render;
import com.crotaplague.torture.Files.ServerScriptService.battleEngine;
import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.*;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.AnimationManager;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.Stage;
import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.ChainTask;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.humans.humanDex;
import com.crotaplague.torture.Files.ServerStorage.items.*;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobDex;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Torture;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.google.common.util.concurrent.AtomicDouble;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.crotaplague.torture.Files.ServerScriptService.randomScripts.*;
import static com.crotaplague.torture.Torture.*;


public class events implements Listener {
    public static List<String> registeringClick = new ArrayList<String>();


    @EventHandler
    private void playerCloseInv(InventoryCloseEvent event) {

        InventoryView inv = event.getView();
        if(event.getPlayer() instanceof Player){
            Player player = (Player) event.getPlayer();

            if (inv.title().contains(Component.text("mob box"))) {
                NamespacedKey key = new NamespacedKey(plugin, "noStack");
                SaveFile save = playerSaveFiles.get(player.getUniqueId() + "");
                Map<Integer, mobEnums> playerMobs = new HashMap<>();
                SaveFile.MobBox mobBox = (SaveFile.MobBox) playerSpecifics.get(player.getUniqueId() + " openPCInv");
                int i = 0;
                for (ItemStack itemStack : inv.getBottomInventory()) {
                    if (itemStack != null) {
                        if (itemStack.getType() != Material.AIR && itemStack.getType() != null) {
                            if (itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {

                                mobEnums mob = mobBox.getAllMobs().stream().filter(mobEnums -> {
                                    if (mobEnums.getSlotInMenu() == itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER))
                                        return true;
                                    return false;
                                }).collect(Collectors.toList()).get(0);
                                player.getInventory().remove(itemStack);
                                playerMobs.put(i, mob);
                            }
                        }
                    }
                    i++;
                }
                save.setPlayerMobs(playerMobs);
                HashMap<Integer, mobEnums> boxMobs = new HashMap<>();
                i = 0;
                for (ItemStack itemStack : inv.getTopInventory().getContents()) {
                    if (itemStack != null) {
                        if (itemStack.getType() != Material.AIR && itemStack.getType() != null) {
                            if (itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                                mobEnums mob = mobBox.getAllMobs().stream().filter(mobEnums -> {
                                    if (mobEnums.getSlotInMenu() == itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER))
                                        return true;
                                    return false;
                                }).collect(Collectors.toList()).get(0);
                                player.getInventory().remove(itemStack);
                                boxMobs.put(i, mob);
                            }
                        }
                    }
                    i++;
                }
                int boxNum = (int) playerSpecifics.get(player.getUniqueId() + " boxNum");
                save.setSpecificBox(boxNum, boxMobs);
                playerSaveFiles.put(player.getUniqueId() + "", save);
                if (playerSpecifics.containsKey(player.getUniqueId() + " mouseCursor")) {
                    if (playerSpecifics.get(player.getUniqueId() + " mouseCursor") != null) {
                        mobEnums mob = (mobEnums) playerSpecifics.get(player.getUniqueId() + " mouseCursor");
                        playerSaveFiles.get(player.getUniqueId() + "").addMobToAvailable(mob);
                        playerSpecifics.put(player.getUniqueId() + " mouseCursor", null);
                    }
                }
            }

        }



    }

    @EventHandler
    public static void playerLeaveEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(plugin, "owner");
        for (Entity entity : world.getEntities().stream().filter(entity5 -> {
            if (entity5.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                if (entity5.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase(player.getUniqueId().toString()))
                    return true;
            }
            return false;
        }).collect(Collectors.toList())) {
            entity.remove();
        }
        if (playerSpecifics.containsKey(player.getUniqueId() + " mouseCursor")) {
            if (playerSpecifics.get(player.getUniqueId() + " mouseCursor") != null) {
                mobEnums mob = (mobEnums) playerSpecifics.get(player.getUniqueId() + " mouseCursor");
                playerSaveFiles.get(player.getUniqueId() + "").addMobToAvailable(mob);
            }
        }
        battleClass b = getBattle(player);
        if(b != null){
            for(Map.Entry<Integer, mobEnums> ms : b.getMobs().entrySet()){
                if(ms.getValue().getSelf().isValid()){
                    ms.getValue().getSelf().remove();
                }
            }
            SaveFile file = getPlayerSaveFile(player);
            file.setInBattle(false);
            battles.remove(b);
        }
        int timePlayed = timeTracker.get(player);
        int seconds = (timePlayed/20) % 60;
        int minutes = ((timePlayed/20) / 60) % 60;
        int hours =  ((timePlayed/20) / 60) / 60;

        hook.setContent(player.getName() + " left, they played for " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.");
        try {
            hook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void entDamageEnt(EntityDamageByEntityEvent event) {
        NamespacedKey key = new NamespacedKey(plugin, "hiddenName");

        NamespacedKey key2 = new NamespacedKey(plugin, "humanDexNum");
        NamespacedKey key3 = new NamespacedKey(plugin, "BattleOptions");
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity.getPersistentDataContainer().get(key, PersistentDataType.STRING) != null) {
            if (entity.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase("Walking to")) {
                event.setCancelled(true);
            }
        }
        if(entity.getPersistentDataContainer().has(key2, PersistentDataType.INTEGER) || entity.getPersistentDataContainer().has(key3, PersistentDataType.INTEGER)){
            event.setCancelled(true);
        }
        if(entity.getType() == EntityType.ARMOR_STAND){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entTargetEvent(EntityTargetEvent event) {
        NamespacedKey key = new NamespacedKey(plugin, "hiddenName");
        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            if (entity.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase("Walking to")) {
                if (target instanceof Villager) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void dragEvent(InventoryDragEvent event) {
        Player player = (Player) event.getView().getPlayer();
        InventoryView view = event.getView();
        if (view.title().contains(Component.text("mob box"))) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void invClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryView invView = event.getView();
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) invView.getPlayer();
        player.sendMessage(event.getView().title());
        if(invView.title().equals(Component.text("Left Click add before || Scroll Click play || Right Click add after")) || invView.title().equals(Component.text("Animations: "))){event.setCancelled(true); animationMenuClick(event);} // awdasd addddddd coooooooooodeeee heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeere
        if(invView.title().equals(Component.text("Select Animation To Remove:"))) removeAnimation(event);
        if(invView.title().equals(Component.text("Select Mob To Move"))){
            event.setCancelled(true);
            Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");
            AnimationManager man = (AnimationManager) playerSpecifics.get(player.getUniqueId() + " SelectedAnimation");
            int clickSLot = event.getSlot();
            List<AnimationManager> list = s.getAnimations();
            player.sendMessage((clickSLot > -1) + " " + (clickSLot <= list.size()));
            if(clickSLot > -1 && clickSLot <= list.size()) {
                AnimationManager choosen = list.get(clickSLot);
                if(choosen.getDisguise() != null) {
                    man.setTie(choosen.getId());
                    animationData.getConfig().set("stage " + s.getName(), s.serialize());
                    animationData.saveConfig();
                    player.sendMessage(man.getTie().toString());
                    randomScripts.openStageAnimations(s, player);
                }else{
                    player.sendMessage("Animation can not be selected.");
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 1, 1);
                }
            }
        }


        if (invView.title().contains(Component.text("mob box"))) event.setCancelled(pcBoxCode(player, event.getSlot(), event.getClick(), event.getClickedInventory(), invView, event));
        if (invView.title().equals(Component.text("Select Attack").color(NamedTextColor.WHITE))) {
            battleTypes type = null;
            if(clickedItem != null) if(clickedItem.getType() != Material.AIR){
                NamespacedKey key = new NamespacedKey(getInstance(), "moveNum");
                int indexNum = 0;
                if(clickedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
                    for(battleClass battle : battles){
                        if(battle.getEntityCompetitors().contains(player)){
                            type = battle.getBattleType();

                            humanClass.Trainer trainer = battle.getTrainer(player);
                            mobEnums currentMob = trainer.getCurrentSelecting();
                            player.sendMessage(currentMob + " look for this");
                            moveClass move = null;
                            for (moveClass currentMobMove : currentMob.getMoves()) {
                                if(currentMobMove.getMoveNum() == mobDex.moveList(clickedItem.getItemMeta().getDisplayName().substring(2)).getMoveNum()){
                                    move = currentMobMove;
                                }
                            }
                            if(move != null) {
                                if(type == battleTypes.SOLOPVE || type == battleTypes.SOLOPVP) {
                                    mobEnums target;
                                    if(move.getTarget() == moveClass.moveEffectTarget.SELF){
                                        target = currentMob;
                                    }else{
                                        target = currentMob.getOpponentMob();
                                        player.sendMessage("This toby: " + target);
                                    }
                                    TextComponent ret = applyMoveTo(currentMob, target, move, battle);
                                    if(ret != null){
                                        player.sendMessage(ret);
                                    }
                                }
                                else{
                                    battleEngine.showTargetMenu(player, battle);
                                    return;
                                }
                                confirmingAMove(battle, trainer, move);
                                if(battle.allReady()){
                                    battle.doRound();
                                }else{
                                    for(Map.Entry<mobEnums, Boolean> ent : battle.getMobStatus().entrySet()){
                                        Bukkit.getPlayer("CrotaPlague").sendMessage("the mob: " + ent.getKey() + " the value: " + ent.getValue());
                                    }
                                }
                            }

                            battles.set(indexNum, battle);

                            break;
                        }
                        indexNum++;
                    }

                }

            }
            event.setCancelled(true);
            player.closeInventory();
        }
        if(invView.getTitle().equals(ChatColor.GOLD + "Bag")){
            openBagMenus(player, event);
        }
        if(invView.title().equals(Component.text("Healing Items", NamedTextColor.WHITE)) || invView.title().equals(Component.text("Shulkers", NamedTextColor.WHITE)) || invView.getTitle().equals(ChatColor.WHITE + "Main Items") || invView.getTitle().equals(ChatColor.WHITE + "Holding Items") ||
                invView.getTitle().equals(ChatColor.WHITE + "Move Items") || invView.getTitle().equals(ChatColor.RED + "Use?")){
            event.setCancelled(true);
            SaveFile file;
            file = playerSaveFiles.get(player.getUniqueId().toString());
            Optional<battleClass> canBattle = battles.stream().filter(battleClass -> battleClass.isBattling()).filter(battleClass -> battleClass.entityIsInBattle(player)).findFirst();
            if(file.getInBattle() && canBattle.isPresent() && canBattle.get().isAiBattle()){
                battleClass b = canBattle.get();
                if((invView.title().equals(Component.text("Healing Items", NamedTextColor.WHITE)))){
                    Inventory inv = Bukkit.createInventory(player, 36, "Use?");
                    int counter = 0;
                    for (mobEnums mob : file.getPlayerMobs().values()) {
                        ItemStack disp = createMobDisplayItem(mob);
                        ItemMeta meta = disp.getItemMeta();
                        NamespacedKey key = new NamespacedKey(plugin, "ApplyBoostItem");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, counter);

                        disp.setItemMeta(meta);
                        inv.setItem(counter, disp);
                        counter++;
                    }
                    inv.setItem(8, clickedItem);
                    player.openInventory(inv);
                }
                if(invView.getTitle().equals(ChatColor.WHITE + "Shulkers")){
                    if(b.isWild()) {
                        ShulkerItem item = file.getPlayerBag().getShulkerShells().get(event.getSlot());
                        humanClass.Trainer t = b.getTrainer(player);
                        t.getCurrentSelecting().setNextMove(item);
                        b.setMobStatus(t.getCurrentSelecting());
                    }
                }
            }else{
                if(canBattle.isPresent() && !(canBattle.get().isAiBattle())) {
                    player.sendMessage(Component.text("Items can't be used in a trainer battle", NamedTextColor.RED));
                    player.closeInventory();
                }
            }
        }
        if(invView.getTitle().equalsIgnoreCase("§fSwap Mob")){
            event.setCancelled(true);
            if(event.getCurrentItem() != null){
                battleClass battle = battles.stream().filter(bat -> {if(bat.entityIsInBattle(player)) if(bat.isBattling()) return true; return false;}).collect(Collectors.toList()).get(0);
                if(battle != null){
                    if(event.getCurrentItem().getType() != Material.GRAY_STAINED_GLASS_PANE && event.getCurrentItem().getItemMeta().getEnchants().size() > 0){
                        humanClass.Trainer trainer = battle.getTrainer(player);
                        mobEnums newMob = trainer.getMobs().get(event.getSlot()-1);
                        player.sendMessage("Currently selecting: " + trainer.getCurrentSelecting().getName());
                        trainer.getCurrentSelecting().setNextMove(newMob);
                        player.sendMessage("new move: " + newMob.getName());
                        trainer.setStatus(true);
                        SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
                        file.getArmorStands().forEach(stand -> {stand.setCustomNameVisible(false);});
                        battle.setMobStatus(trainer.getCurrentSelecting());
                        battle.setTrainer(trainer);

                        event.getInventory().close();
                        if(!battle.inRound()){
                            if(battle.allReady()){
                                battle.doRound();
                            }
                        }else{

                        }

                    }

                }
            }

        }
        if(clickedItem != null && !clickedItem.getType().equals(Material.AIR)){
            NamespacedKey key = new NamespacedKey(plugin, "ItemDexNum");
            NamespacedKey boost = new NamespacedKey(plugin, "ApplyBoostItem");
            if((clickedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER) && invView.title().equals(Component.text("Shulkers"))) || clickedItem.getItemMeta().getPersistentDataContainer().has(boost, PersistentDataType.INTEGER)){
                battleClass battle = battles.stream().filter(battles -> {return battles.getEntityCompetitors().contains(player) && battles.isBattling();}).collect(Collectors.toList()).get(0);
                ItemClass item = null;
                humanClass.Trainer trainer = battle.getTrainer(player);
                if(invView.title().equals(Component.text("Shulkers"))) {
                    int itemDexNum = clickedItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    item = TItemDex.getItem(itemDexNum).clone();
                }else{
                    if(inventory.getItem(8) != null && !inventory.getItem(8).getType().equals(Material.AIR)){
                        ItemMeta it = inventory.getItem(8).getItemMeta();
                        if(it.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
                            int itemDexNum = inventory.getItem(8).getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                            int thing = clickedItem.getItemMeta().getPersistentDataContainer().get(boost, PersistentDataType.INTEGER);
                            mobEnums m = trainer.getMobs().get(thing);
                            item = TItemDex.getItem(itemDexNum).clone();
                            item.setTarget(m);
                        }
                    }
                }
                if (item == null) return;
                item.setAmount(1);
                if(item instanceof ShulkerItem){
                    if(!battle.isWild()){
                        randomScripts.actionBarMessage(player, "Shulkers can not be used in trainer battles!", SaveFile.MessagePurpose.NONE);
                        return;
                    }
                }
                trainer.getCurrentSelecting().setNextMove(item);
                trainer.setStatus(true);
                SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
                if(battle.nextUnready(trainer) == null) {
                    file.hideArmorStands();
                }else{
                    trainer.setCurrentSelecting(battle.nextUnready(trainer));
                }
                battle.setMobStatus(trainer.getCurrentSelecting());
                battle.setTrainer(trainer);
                invView.close();
                if(!battle.inRound()){
                    if(battle.allReady()){
                        battle.doRound();
                    }
                }

            }
        }


    }
    public static void swapMob(mobEnums newMob, humanClass.Trainer trainer, battleClass battle){
        swapMob(newMob, trainer, battle, null);
    }
    public static void swapMob(mobEnums newMob, humanClass.Trainer trainer, battleClass battle, Runnable term) {
        Entity entity  = trainer.getSelf();
        mobEnums current = trainer.getCurrentSelecting();
        Bukkit.getPlayer("CrotaPlague").sendMessage("Current: " + current);
        // Step 1: removal
        ChainTask removalTask = onComplete -> {
            // this runs immediately on the main thread,
            // and calls onComplete() when done
            removeMob(current, onComplete::run);
        };

        // Step 2: spawn+setup new mob
        ChainTask spawnTask = onComplete -> {
            try {
                Bukkit.getPlayer("CrotaPlague").sendMessage("I mean wait!! it is running though");
                // everything you had *after* the runSequentialAndWait
                int spot = current.getBattleSpot();
                Location loc = current.getSelf().getLocation();
                battle.getEntityCompetitors().remove(current.getSelf());

                LivingEntity oldOpponent = (LivingEntity) current.getOpponentMob().getSelf();
                LivingEntity live = (LivingEntity) entity.getWorld().spawnEntity(loc, newMob.getMojangMobType());
                newMob.setSelf(live);

                live.setAI(false);
                live.setSilent(true);
                live.setInvulnerable(true);
                live.customName(Component.text(newMob.getCurrentHp(),
                        NamedTextColor.RED));
                live.setCustomNameVisible(true);
                Bukkit.getPlayer("CrotaPlague").sendMessage("this does run at least once");
                battleEngine.faceLoc(live, oldOpponent);
                battleEngine.faceLoc(oldOpponent, live);
                battle.getEntityCompetitors().add(live);
                battle.setMobStatus(newMob);
                Bukkit.getPlayer("CrotaPlague").sendMessage("part 3");
                trainer.setCurrentMobEnt(live);
                trainer.setCurrentSelecting(newMob);
                newMob.setOpponentMob(current.getOpponentMob());
                newMob.setBattleSpot(spot);
                trainer.replaceMobInPlay(current, newMob);
                newMob.setTrainer(trainer);

                battle.subOutMob(current, newMob);
                Bukkit.getPlayer("CrotaPlague").sendMessage("and 5");

                battle.getMobs().put(spot, newMob);
                battle.setTrainer(trainer.setPrimaryMob(newMob));

                // signal that step 2 is done (and since this is last, nothing else follows)
                if(term != null){
                    term.run();
                }
                onComplete.run();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        };

        // run both tasks *sequentially* on the main thread
        runSequential(removalTask, spawnTask);

    }


    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()){
            player.setGameMode(GameMode.ADVENTURE);
        }
        String message = player.getName() + " has joined the server";

        if (!(playerHasJoined)){
            playerHasJoined = true;
            int counter = worldData.getConfig().getInt("Entity count");
            player.getWorld().getEntities().forEach(entity -> {if(!(entity instanceof Player || entity instanceof ItemFrame)){ entity.getLocation().getChunk().load(true); entity.remove(); Bukkit.getLogger().log(Level.WARNING, "TRUUUUE");}});
            for(int i = 1; i<=counter; i++){ //spawn them all, needs entity type location and data
                if(worldData.getConfig().isConfigurationSection("spawnNPC " + i)){
                    ConfigurationSection configurationSection = worldData.getConfig().getConfigurationSection("spawnNPC " + i);
                    Map<String, Object> data = configurationSection.getValues(true);
                    String str = data.entrySet().stream().findFirst().get().getKey();
                    Villager mob = (Villager) player.getWorld().spawnEntity((Location) data.entrySet().stream().findFirst().get().getValue(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM, vil -> {((Villager) vil).setAI(false); ((Villager) vil).setAdult();});
                    mob.getChunk().load();
                    final int finalI = i;
                    final Disguise disguise;
                    try {
                        DisguiseAPI.addCustomDisguise("tempDis" + finalI, str);
                    } catch (DisguiseParseException e) {
                        e.printStackTrace();
                    }
                    disguise = DisguiseAPI.getCustomDisguise("tempDis" + finalI);
                    DisguiseAPI.disguiseToAll(mob, disguise);
                    final BukkitTask[] task = new BukkitTask[1];
                    task[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        if (!DisguiseAPI.isDisguised(mob)) {
                            DisguiseAPI.disguiseToAll(mob, disguise);
                        } else {
                            if(mob.isValid()) {
                                Bukkit.getPlayer("CrotaPlague").sendMessage(Component.text("This is when it finishes", NamedTextColor.GREEN));
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    DisguiseAPI.removeCustomDisguise("tempDis" + finalI);
                                }, 5L);
                                task[0].cancel();
                            }
                        }
                    }, 5L, 1L);

                    NamespacedKey key = new NamespacedKey(plugin, "humanDexNum");
                    mob.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
                    ConfigurationSection section = worldData.getConfig().getConfigurationSection("humanDexNum " + i);
                    data = section.getValues(true);
                    humanClass human = humanClass.deserialize(data);
                    human.setMobEntity(mob);
                    if(human.getType() == 2){
                        raycasting.add(mob);
                    }
                }else{
                    break;
                }

            }
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.42);
            player.setFoodLevel(20);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            //start item generation
            if(worldData.getConfig().contains("worldItemCount")){
                counter = (int) worldData.getConfig().get("worldItemCount");
                for(int count = 0; count < counter+1; count++){
                    List<Object> list = (List<Object>) worldData.getConfig().getList("worldItem " + count);
                    Location amLoc = (Location) list.get(0);
                    ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(amLoc, EntityType.ARMOR_STAND);

                    armorStand.getEquipment().setItemInMainHand(TItemDex.getItem((Integer) list.get(1)).getDisplayItem());
                    armorStand.setInvulnerable(true);
                    armorStand.setGravity(false);
                    armorStand.setInvisible(true);
                    armorStand.setArms(true);
                    EulerAngle angle = new EulerAngle(Math.PI,0,0);
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
                    Location loc = armorStand.getLocation().add(vec);

                    double yawRightHandDirection = Math.toRadians(-1 * loc.getYaw() - 45);
                    double x = 0.5 * Math.sin(yawRightHandDirection) + loc.getX();
                    double y = loc.getY() + 1.65;
                    double z = 0.5 * Math.cos(yawRightHandDirection) + loc.getZ();
                    Location l = new Location(armorStand.getWorld(), x, y, z);
                    MagmaCube puffer = (MagmaCube) armorStand.getWorld().spawnEntity(l, EntityType.MAGMA_CUBE);
                    puffer.setAI(false);
                    puffer.setInvulnerable(true);
                    puffer.setSize(1);
                    puffer.setInvisible(true);
                    NamespacedKey key = new NamespacedKey(plugin, "itemNumber");
                    puffer.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, count);
                    //player.getWorld().spawnEntity();
                }
            }
        }else{
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Entity ent : player.getWorld().getEntities()) {
                    if (DisguiseAPI.isDisguised(ent)) {
                        DisguiseAPI.disguiseToAll(ent, DisguiseAPI.getDisguise(ent));
                    }
                }
            }, 8L);
        }




        player.setWalkSpeed(0.2f);
        String fullPath = Torture.path + "saves/player-" + player.getUniqueId() + ".yml.zst";
        File f = new File(fullPath);
        if(f.exists()){
            DataManager d = new DataManager(player);
            MemorySection section = (MemorySection) d.getConfig().get(player.getUniqueId() + " saveFile");
            SaveFile file = SaveFile.deserialize(section);
            file.setPlayer(player);
            playerSaveFiles.put(player.getUniqueId() + "", file);
            player.teleport(playerSaveFiles.get(player.getUniqueId().toString()).getPlayerLoc());

            hideClaimed(player);

        }else{
            message = message + " for the first time!";
            player.sendMessage(Component.text("No saved data!", NamedTextColor.RED));
            SaveFile playerSaveFile = new SaveFile();
            playerSaveFile.setMobBox(new HashMap<>());
            playerSaveFile.setPlayerMobs(new HashMap<>());
            playerSaveFile.setRouteNum(1);
            playerSaveFile.instantiateTrainers();
            playerSaveFile.setName(player.getName());
            playerSaveFile.setPlayer(player);
            playerSaveFiles.put(player.getUniqueId().toString(), playerSaveFile);
        }
        hook.setContent(message);
        try {
            hook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timeTracker.put(player, 0);
    }

    public static Boolean pcBoxCode(Player player, int slotNum, ClickType clickType, Inventory inventory, InventoryView inventoryView, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if(item != null && item.getType() != Material.AIR){
            if (slotNum != -999) {
                if (player.getOpenInventory().getTitle().contains("mob box")) { //start of trying to manage player box
                    NamespacedKey key = new NamespacedKey(plugin, "noStack");
                    if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) { //not talking about shift clicking
                        SaveFile.MobBox box = (SaveFile.MobBox)playerSpecifics.get(player.getUniqueId() + " openPCInv");
                        List<mobEnums> list = box.getAllMobs().stream().filter(mobEnums -> {if(mobEnums.getSlotInMenu() == item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER)) return true; return false;}).collect(Collectors.toList());
                        if(list.size() > 0){
                            mobEnums mob = list.get(0);
                            playerSpecifics.put(player.getUniqueId() + " mouseCursor", mob);
                        }
                    } else {
                        return true;
                    }

                } else {
                    return true;
                }

            }else return true;
        }else{playerSpecifics.put(player.getUniqueId() + " mouseCursor", null);}
        return false;
    }
    @EventHandler
    public void InteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem() != null){
                if(event.getItem().equals(ItemManager.showMobMoves)){
                    for(battleClass battle : battles){
                        if(battle.getEntityCompetitors().contains(player)) showAttackMenu(player, battle);
                    }

                }
            }
        }
        Mob mob;

        if(player.getTargetBlock((Set<Material>) null,3).getType() == Material.BARRIER){
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
                battleClass battle = getBattle(player);
                if(battle != null){
                if(playerSpecifics.get(player.getUniqueId() + " fullMessage") == null && file.getArmorStands().get(0).isCustomNameVisible()){
                    Location location = player.getTargetBlock((Set<Material>) null,3).getLocation().toCenterLocation();
                    NamespacedKey key = new NamespacedKey(plugin, "BattleOptions");

                    Location eye   = player.getEyeLocation();
                    Vector direction = eye.getDirection();
                    double maxDistance = 2.0;

                    RayTraceResult result = player.getWorld().rayTraceEntities(
                            eye,
                            direction,
                            maxDistance,
                            entity -> entity instanceof MagmaCube
                    );
                    if (result == null || !(result.getHitEntity() instanceof MagmaCube)) {
                        return;
                    }
                    MagmaCube magma = (MagmaCube) result.getHitEntity();
                    Integer i = magma.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    if(magma.getPersistentDataContainer().has(key)) {
                        if (i != null && i != 4) {
                            humanClass.Trainer trainer = battle.getTrainer(player);
                            if (trainer.getPrimaryMob().getCurrentHp() > 0) {
                                if (i == 2) showAttackMenu(player, battle);
                                if (i == 1) openBag(player);
                            } else {
                                event.setCancelled(true);
                                player.sendActionBar(ChatColor.RED + "You have to select your mob to switch in!");
                                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, SoundCategory.MASTER, 1, 1);
                            }
                        } else {
                            showSwapMenu(player, battle);
                        }
                    }

                    }
                }
            }

        }
    }

    public static void showSwapMenu(@NotNull Player player, battleClass battle){
        humanClass.Trainer trainer = battle.getTrainer(player);
        Inventory inv = Bukkit.createInventory(player, 9, "§fSwap Mob");
        inv.setMaxStackSize(1);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        glass.getItemMeta().setDisplayName("");
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName("");
        glass.setItemMeta(meta);
        inv.setItem(0, glass);
        inv.setItem(7, glass);
        inv.setItem(8, glass);
        int i = 0;
        for(int counter = 1; counter < 7; counter++){
            mobEnums m = trainer.getMobs().size() > (i) ? trainer.getMobs().get(i) : null;
            if(m != null){
                ItemStack ite = createMobDisplayItem(m);
                ItemMeta metaaaa = ite.getItemMeta();
                metaaaa.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                if(trainer.getMobs().get(i).getIdent() != trainer.getPrimaryMob().getIdent()  && trainer.getMobs().get(i).getCurrentHp() > 0) metaaaa.addEnchant(Enchantment.LURE, 1, true);
                ite.setItemMeta(metaaaa);
                inv.setItem(counter, ite);
            }

            i++;
        }
        player.openInventory(inv);
    }
    public void showAttackMenu(Player player, battleClass battle){
        humanClass.Trainer trainer = battle.getTrainer(player);
        Inventory inv = Bukkit.createInventory(player, InventoryType.CHEST, "§fSelect Attack");
        mobEnums mob = trainer.getPrimaryMob();
        ItemStack mobIcon = mob.getIcon();
        ItemMeta itemMeta = mobIcon.getItemMeta();
        itemMeta.setDisplayName("§f" + mob.getName());
        mobIcon.setItemMeta(itemMeta);
        inv.setItem(13, mobIcon);
        System.out.println("Patrick is humble");
        List<Integer> numList = new ArrayList<>();
        numList.add(4);
        numList.add(12);
        numList.add(14);
        numList.add(22);
        int tracker = 0;
        ItemStack moveIcon;
        try{
            for(moveClass moveClass : mob.getMoves()){

                if(moveClass.getIcon().getItemMeta() != null){
                    itemMeta = moveClass.getIcon().getItemMeta();
                    itemMeta.setDisplayName("§f" + moveClass.getMoveName());
                    moveIcon = moveClass.getIcon();
                    NamespacedKey key = new NamespacedKey(getInstance(), "moveNum");
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, moveClass.getMoveNum());
                    moveIcon.setItemMeta(itemMeta);
                    inv.setItem(numList.get(tracker), moveIcon);
                    tracker++;
                }


            }
        }catch(IndexOutOfBoundsException out){
            player.sendMessage("for loop");
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void SwapHands(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        battleClass thisBattle = null;
        for(battleClass battle : battles){
            if(battle.getEntityCompetitors().contains(player)){
                thisBattle = battle;
            }
        }
        if ((playerSpecifics.get(player.getUniqueId() + " currentMessageSpot") != null)) {
            SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
            String message = (String) playerSpecifics.get(player.getUniqueId() + " fullMessage");
            Pair<String, SaveFile.MessagePurpose> pair = new Pair<>(message, file.getPurpose());
            if(!(playerSpecifics.get(player.getUniqueId() + " currentMessageSpot").toString().equalsIgnoreCase(message))){
                playerSpecifics.put(player.getUniqueId() + " entity message", playerSpecifics.get(player.getUniqueId() + " fullMessage"));
                return;
            }else{
                if(file.messageQueue().size() > 0){
                    Pair<String, SaveFile.MessagePurpose> next = file.messageList().get(0);
                    BukkitTask scheduledFuture = (BukkitTask) playerSpecifics.get(player.getUniqueId() + " stopMessage");
                    scheduledFuture.cancel();
                    playerSpecifics.put(player.getUniqueId() + " fullMessage", null);
                    file.messageQueue.remove(next);
                    playerSaveFiles.put(player.getUniqueId() + "", file);
                    randomScripts.actionBarMessage(player, next.getLeft(), next.getRight(), true);
                }else{
                    BukkitTask scheduledFuture = (BukkitTask) playerSpecifics.get(player.getUniqueId() + " stopMessage");
                    scheduledFuture.cancel();
                    playerSpecifics.put(player.getUniqueId() + " fullMessage", null);
                }
            }
            if(thisBattle == null && pair.getRight() == SaveFile.MessagePurpose.TRAINER){
                battles.replaceAll(e -> {if(e.getEntityCompetitors().contains(player)) e.setBattling(true); return e;});
                if(playerSpecifics.get(player.getUniqueId() + " stopMessage") != null) {
                    BukkitTask scheduledFuture = (BukkitTask) playerSpecifics.get(player.getUniqueId() + " stopMessage");
                    scheduledFuture.cancel();

                    if(file.messageQueue().size() > 0){
                        playerSpecifics.put(player.getUniqueId() + " fullMessage", file.getOpponent().getPhrase());
                        file.messageQueue.remove(0);
                    }else{
                        playerSpecifics.put(player.getUniqueId() + " fullMessage", null);
                    }

                    NamespacedKey key = new NamespacedKey(getInstance(), "humanDexNum");
                    LivingEntity entity = (LivingEntity) playerSpecifics.get(player.getUniqueId() + " walkingEntityForPVE");
                    Integer dNum = entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    Bukkit.getPlayer("CrotaPlague").sendMessage("Well this is the acquired dex num: " + dNum);
                    humanClass human = humanDex.getHuman(dNum, entity);
                    battleTypes battleType = null;
                    if (battleType == null) {
                        battleType = battleTypes.SOLOPVE;
                    }
                    battleEngine.startBattle(file.createTrainer(), battleType, 1, human.getMobs().get(0));
                }
            }else{ //this runs if the player is in a battle
                if(thisBattle.isOver() && pair.getRight() == SaveFile.MessagePurpose.BATTLE_END){
                    extracted(player, thisBattle);

                }
                for (humanClass.Trainer trainer : thisBattle.getCompetitors()) {
                    if (trainer.getSelf() instanceof Player) {

                    }
                }
                if(playerSpecifics.get(player.getUniqueId() + " fullMessage") == null){
                    file.getArmorStands().forEach(stand -> stand.setCustomNameVisible(true));
                }
        }


        }
        if(player.getGameMode() != GameMode.CREATIVE){
            event.setCancelled(true);
        }

    }

    public static void extracted(Player player, battleClass thisBattle) {
        SaveFile file = randomScripts.getPlayerSaveFile(player);
        player.teleport(file.getPreBattleLocation());
        thisBattle.playerCount--;
        for(mobEnums m : thisBattle.getTrainer(player).getCurrentMobs()){
            if(m != null){
                m.getSelf().remove();
            }
        }
        file.getArmorStands().forEach(stand -> {stand.remove();});
        thisBattle.getCompetitors().remove(thisBattle.getTrainer(player));
        if(thisBattle.playerCount == 0){

            thisBattle.getCompetitors().forEach(tra -> {tra.getPrimaryMob().getSelf().remove();});

            for(battleClass brb : new ArrayList<>(battles)){
                if(brb.thisUUID == thisBattle.thisUUID){
                    battles.remove(brb);
                }
            }
        }
        player.setWalkSpeed(0.2f);
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.42);
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler
    public void entitySpawn(CreatureSpawnEvent event){
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        if(!(reason == CreatureSpawnEvent.SpawnReason.CUSTOM || reason == CreatureSpawnEvent.SpawnReason.BREEDING || reason == CreatureSpawnEvent.SpawnReason.COMMAND || reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) || reason == CreatureSpawnEvent.SpawnReason.TRAP){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void interactEntity(PlayerInteractAtEntityEvent event){
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if(entity.getType() == EntityType.MAGMA_CUBE){
            if(event.getHand() == EquipmentSlot.HAND){
                MagmaCube puffer = (MagmaCube) entity;
                NamespacedKey key = new NamespacedKey(plugin, "itemNumber");
                if(!puffer.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){return;}
                int num = puffer.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                List<Object> infoList = (List<Object>) worldData.getConfig().getList("worldItem " + num);
                int dexNum = (int) infoList.get(1); // 0 is the item's location
                player.sendMessage("Dexnum: " + dexNum);
                ItemClass item = TItemDex.getItem(dexNum);
                item = item.clone();
                player.sendMessage("and the item: " + item);
                item.setAmount((int) infoList.get(2));
                SaveFile save = playerSaveFiles.get(player.getUniqueId() + "");
                if(!save.hasClaimedItem(num)){
                    BagClass bag = save.getPlayerBag();
                    bag.addItem(item);
                    save.setPlayerBag(bag);
                    save.claimItem(num);
                    hideClaimed(player);
                    //openBag(player);
                }else{
                    player.hideEntity(Torture.plugin, entity);
                }
            }
        }
        if(entity.getType() == EntityType.VILLAGER){
            NamespacedKey key = new NamespacedKey(plugin, "BESAT");
            if(entity.getPersistentDataContainer().has(key)){
                Server serv = Bukkit.getServer();
                Merchant merchant = serv.createMerchant(DisguiseAPI.getDisguise(entity).getDisguiseName());
                ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§f" + entity.getPersistentDataContainer().get(key,PersistentDataType.STRING) + " merch");item.setItemMeta(meta);
                MerchantRecipe rec = new MerchantRecipe(item, 999);



                rec.getResult().setItemMeta(meta);
                rec.getResult().setType(Material.LEATHER_CHESTPLATE);
                List<ItemStack> stack = new ArrayList<>();
                stack.add(new ItemStack(Material.EMERALD, 2));
                stack.add(new ItemStack(Material.DIAMOND, 1));
                rec.setIngredients(stack);
                List<MerchantRecipe> recipe = new ArrayList<>(); recipe.add(rec);
                merchant.setRecipes(recipe);

                player.openMerchant(merchant, true);
            }
        }
    }
    public void openBag(Player player){
        SaveFile saveFile = playerSaveFiles.get(player.getUniqueId());
        NamespacedKey key = new NamespacedKey(plugin, "ItemMenuPerc");
        Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Bag");
        ItemStack item = new ItemStack(Material.SHULKER_SHELL);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text("Shulkers", NamedTextColor.WHITE));
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 2);

        item.setItemMeta(itemMeta);
        inventory.setItem(2, item.clone());
        item.setType(Material.GOLDEN_PICKAXE);
        itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text("General Items", NamedTextColor.WHITE));
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 5);
        item.setItemMeta(itemMeta);
        inventory.setItem(1, item);

        ItemStack aItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta m = aItem.getItemMeta();
        m.displayName(Component.text("Healing Items", NamedTextColor.WHITE));
        m.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        aItem.setItemMeta(m);
        inventory.setItem(3, aItem);

        player.openInventory(inventory);

    }
    public void openBagMenus(Player player, InventoryClickEvent event){
        if(event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){
            event.setCancelled(true);
            BagClass bag = playerSaveFiles.get(player.getUniqueId() + "").getPlayerBag();
            NamespacedKey key = new NamespacedKey(plugin, "ItemMenuPerc");
            NamespacedKey key2 = new NamespacedKey(plugin, "ItemDexNum");
            int menuNum = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            Inventory inventory = Bukkit.createInventory(player, 54, Component.text(ItemClass.TItemType.menuNames(menuNum), NamedTextColor.WHITE));
            int menuIndex = 0;
            ItemStack itemStack = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(53, itemStack);
            for(ItemClass item : bag.getNumList(menuNum)){
                if(inventory.getContents()[menuIndex] == null || inventory.getContents()[menuIndex].getType() == Material.AIR){
                    ItemStack i = item.getDisplayItem().clone();
                    ItemMeta meta = i.getItemMeta();
                    meta.getPersistentDataContainer().set(key2, PersistentDataType.INTEGER, item.getItemDexNum());
                    i.setItemMeta(meta);
                    inventory.setItem(menuIndex, i);

                }
                menuIndex++;
            }
            player.openInventory(inventory);
        }

    }

    public void animationMenuClick(InventoryClickEvent event){
        InventoryView view = event.getView();
        Player player = (Player) view.getPlayer();
        if(!playerSpecifics.containsKey(player.getUniqueId() + " SelectedStage")) return;

        Stage s = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");

        ClickType type = event.getClick();
        int clickSLot = event.getSlot();
        List<AnimationManager> list = s.getAnimations();
        if(playerSpecifics.containsKey(player.getUniqueId() + " SelectedAnimation")){
            AnimationManager man;
            boolean ttur = false;
            if(playerSpecifics.get(player.getUniqueId() + " SelectedAnimation") != null){
                man = (AnimationManager) playerSpecifics.get(player.getUniqueId() + " SelectedAnimation"); ttur = true;
            } else{
                man = new AnimationManager(UUID.randomUUID()); player.sendMessage("Generated new");
            }


            if(clickSLot > -1 && clickSLot <= list.size() && ttur) {
                if (type == ClickType.LEFT) {

                    list.add(clickSLot, man);
                    s.setAnimations(list);
                    playerSpecifics.remove(player.getUniqueId() + " SelectedAnimation");
                    player.closeInventory();
                }
                if (type == ClickType.RIGHT) {
                    clickSLot++;
                    if (clickSLot == list.size()) {
                        list.add(man);
                    } else {
                        list.add(clickSLot, man);
                    }
                    playerSpecifics.remove(player.getUniqueId() + " SelectedAnimation");
                    s.setAnimations(list);
                    player.closeInventory();
                }
                animationData.getConfig().set("stage " + s.getName(), s.serialize());
                animationData.saveConfig();

            }
        }
        if(clickSLot > -1 && clickSLot <= list.size()) {
            if (type == ClickType.MIDDLE) {
                middleClickCode(list.get(clickSLot), list, player);
                view.close();
            }
        }

    }


    @EventHandler
    public void playerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
        if(file.autoSaveEnabled()){
            file.resetAutoDelay();
        }
    }

    @EventHandler
    public void playerKickedEvent(PlayerKickEvent event){
        PlayerQuitEvent event1 = new PlayerQuitEvent(event.getPlayer(), event.leaveMessage(), PlayerQuitEvent.QuitReason.KICKED);
        playerLeaveEvent(event1);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {event.setMessage(event.getMessage());}

    @EventHandler
    public void manipulate(PlayerArmorStandManipulateEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void OnMapI(MapInitializeEvent event){
        event.getMap().addRenderer(new Render());
    }

    public void middleClickCode(AnimationManager man, List<AnimationManager> list, Player player){
        int i = 0;
        Map<UUID, AnimationManager> mapped = new HashMap<>();
        Location tLoc = null;
        for (AnimationManager an : list) {
            if (an.getId() == man.getId()) break;   // runs for every animation up to animation to play
            if (an.getGoal() != null && an.getDisguise() != null) {
                Villager vil = (Villager) world.spawnEntity(an.getGoal(), EntityType.VILLAGER);
                vil.setAI(false);
                vil.setSilent(true);          // creates villager and turn of its AI and sets it to silent
                try {
                    DisguiseAPI.addCustomDisguise("tempDis" + i, an.getDisguise());
                } catch (DisguiseParseException e) {
                    e.printStackTrace();
                }
                Disguise dis = DisguiseAPI.getCustomDisguise("tempDis" + i); // generates and sets its disguise
                DisguiseAPI.disguiseEntity(vil, dis);
                an.setActor(vil);
                if(!mapped.containsKey(an.getId())){
                    mapped.put(an.getId(), an);
                }
            } else if (an.getTie() != null && an.getGoal() != null) {
                if (!mapped.containsKey(an.getTie())) {
                    for (AnimationManager slop : list) {
                        if (slop.getId() == an.getTie()) {
                            if (slop.getActor() == null) {
                                player.sendMessage("§cAGHHHH S-shutting down!!! Animation use entity that hasn't spawned!!!!! AGHGHH");
                                return;
                            }
                            mapped.put(an.getTie(), slop);
                            an.setActor(slop.getActor());
                        }
                    }
                } else an.setActor(mapped.get(an.getTie()).getActor());

                Mob m = (Mob) an.getActor();
                m.teleport(an.getGoal());
                if (an.getEndFace() != null) m.lookAt(an.getEndFace());
            }else if(an.getGoal() != null && an.isCameraTp()){
                tLoc = an.getGoal();
            }
            i++;
        }
        if(tLoc != null){
            player.teleport(tLoc);
        }
        if(man.getTie() != null && mapped.containsKey(man.getTie()) && man.getGoal() != null){
            man.setActor(mapped.get(man.getTie()).getActor());
            Villager m = (Villager) Bukkit.getEntity(man.getActor().getUniqueId());

            m.setCollidable(false);
            m.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
            m.setAI(true);
            Pathfinder p = m.getPathfinder();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Pathfinder.PathResult res = m.getPathfinder().findPath(man.getGoal());
                boolean b = m.getPathfinder().moveTo(res, 0.6);
                player.sendMessage(b + " initial");
            }, 5L);


            double[] tracker = {-1};
            Runnable loopTill = () -> {
                Random rand = new Random(System.currentTimeMillis());
                if (tracker[0] == -1) {tracker[0] = m.getLocation().distance(man.getGoal());}
                else {tracker[0] = Math.min(m.getLocation().distance(man.getGoal()), tracker[0]);}
                if (rand.nextInt(1000) == 30) {
                    player.sendMessage(tracker[0] + " ");
                }
                Pathfinder.PathResult re = m.getPathfinder().findPath(man.getGoal());
                /*if (!m.getPathfinder().hasPath() || !m.getPathfinder().getCurrentPath().getFinalPoint().equals(man.getGoal())) {
                    boolean ttur = m.getPathfinder().moveTo(man.getGoal(), 0.6);
                    if(rand.nextInt(1000) == 30) {player.sendMessage("thistur " + ttur + " " + (re == null));}
                    if (!ttur && !(re == null) && rand.nextInt(700000) == 30) player.sendMessage("Star Struck!");
                }else{
                    if(rand.nextInt(1000) == 30) player.sendMessage("you are too right.");
                }*/
                if (m.getLocation().distance(man.getGoal()) < 0.62) {
                    m.setAI(false);
                    player.sendMessage("In range");
                    Location mobStart = m.getLocation();
                    AtomicDouble startPitch = new AtomicDouble(mobStart.getPitch());
                    AtomicDouble startYaw = new AtomicDouble(mobStart.getYaw());
                    AtomicInteger myInt = new AtomicInteger(1);
                    boolean[] arr = new boolean[1];
                    arr[0] = false;
                    Runnable lookTo = () -> {
                        if (myInt.get() < 376) {
                            Location copy = new Location(world, mobStart.getX(), mobStart.getY(), mobStart.getZ(), (float) lerp(startYaw.get(), man.goal.getYaw(), myInt.get() / 600.00), (float) lerp(startPitch.get(), man.getGoal().getPitch(), myInt.get() / 600.00));
                            m.teleport(copy);
                            if (myInt.get() == 375) arr[0] = true;
                            myInt.getAndIncrement();
                        }
                    };
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                    ScheduledFuture future = executor.scheduleAtFixedRate(lookTo, 150, 700, TimeUnit.MICROSECONDS);
                    cancelPair.put(future, arr);
                }
            };
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            ScheduledFuture future = executor.scheduleAtFixedRate(loopTill, 300, 1, TimeUnit.MILLISECONDS);
            toCancel.put(future, m);

        }else if (man.getTie() == null && man.getGoal() != null && man.getDisguise() != null){
            Villager vil = (Villager) world.spawnEntity(man.getGoal(), EntityType.VILLAGER);
            vil.setAI(false);
            vil.setSilent(true);
            try {
                DisguiseAPI.addCustomDisguise("tempDis", man.getDisguise());
            } catch (DisguiseParseException e) {
                e.printStackTrace();
            }
            Disguise dis = DisguiseAPI.getCustomDisguise("tempDis");
            DisguiseAPI.disguiseEntity(vil, dis);
            man.setActor(vil);
        }else if(man.getGoal() != null && man.isCameraTp()){
            player.teleport(man.getGoal());
        }
    }

    public void removeAnimation(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if(playerSpecifics.get(player.getUniqueId() + " SelectedStage") == null) return;
        Stage stage = (Stage) playerSpecifics.get(player.getUniqueId() + " SelectedStage");

    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event){
        NamespacedKey key = new NamespacedKey(plugin, "AnimationMob");
        Entity entity = event.getEntity();
        if(entity.getPersistentDataContainer().has(key, PersistentDataType.INTEGER_ARRAY)){
            int[] arr = entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER_ARRAY);
            Integer x = arr[0];
            Integer z = arr[1];
            Location loc = event.getLoc();

            if(!(loc.getBlockX() == x && loc.getBlockZ() == z)){event.setCancelled(true);}
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        event.getDrops().clear();
    }

    public static void confirmingAMove(battleClass b, humanClass.Trainer t, Object selection){
        SaveFile save = playerSaveFiles.get(t.getSelf().getUniqueId() + "");
        t.getCurrentSelecting().setNextMove(selection);
        b.setMobStatus(t.getCurrentSelecting());
        if(b.nextUnready(t) == null){
            save.hideArmorStands();
        }else{
            t.setCurrentSelecting(b.nextUnready(t));
        }
    }

    @EventHandler
    public void playerStopSpectate(PlayerStopSpectatingEntityEvent event){
        Player player = event.getPlayer();
        Entity target = event.getSpectatorTarget();
        NamespacedKey bindCamera = new NamespacedKey(plugin, "bindCamera");
        Integer te = target.getPersistentDataContainer().get(bindCamera, PersistentDataType.INTEGER);
        if(te != null && te == 2){
            event.setCancelled(true);
            player.setSpectatorTarget(target);
        }
    }

    @EventHandler
    public void playerStatistic(PlayerStatisticIncrementEvent event){
        if(event.getStatistic() == Statistic.JUMP) return;
    }

    @EventHandler
    public void EntityTakeDamage(EntityDamageEvent event){
        if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
            event.setCancelled(true);
        }
    }

}
