package aspireinc.torture.Events;
import aspireinc.torture.Files.ServerScriptService.battleEngine;
import aspireinc.torture.Files.ServerStorage.ItemManager;
import aspireinc.torture.Files.ServerStorage.battleClass;
import aspireinc.torture.Files.ServerStorage.humans.humanClass;
import aspireinc.torture.Files.ServerStorage.humans.humanDex;
import aspireinc.torture.Files.ServerStorage.mobs.mobDex;
import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;
import aspireinc.torture.Files.ServerStorage.moveClass;
import aspireinc.torture.Torture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;


public class events implements Listener {
    public static List<String> registeringClick = new ArrayList<String>();


    @EventHandler
    private void playerCloseInv(InventoryCloseEvent event) {
        InventoryView inv = event.getView();
        if(event.getPlayer() instanceof Player){
            Player player = (Player) event.getPlayer();

            if (inv != null) {
                if (inv.getTitle().contains("mob box")) {
                    NamespacedKey key = new NamespacedKey(Torture.plugin, "noStack");
                    for (ItemStack itemStack : inv.getBottomInventory()) {
                        if (itemStack != null) {
                            if (itemStack.getType() != Material.AIR && itemStack.getType() != null) {
                                if(itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
                                    player.getInventory().remove(itemStack);
                                }
                            }
                        }
                    }

                }
            }
        }



    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(Torture.plugin, "owner");
        for (Entity entity : Torture.world.getEntities().stream().filter(entity5 -> {
            if (entity5.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                if (entity5.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase(player.getUniqueId().toString()))
                    return true;
            }
            return false;
        }).collect(Collectors.toList())) {
            entity.remove();
        }
        if (Torture.temp.getConfig().contains(player.getUniqueId() + " mouseCursor")) {
            if (Torture.temp.getConfig().get(player.getUniqueId() + " mouseCursor") != null) {

            }
        }
    }


    @EventHandler
    public void entDamageEnt(EntityDamageByEntityEvent event) {
        NamespacedKey key = new NamespacedKey(Torture.plugin, "hiddenName");
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (damager.getPersistentDataContainer().get(key, PersistentDataType.STRING) != null) {
            if (damager.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase("Walking to")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void entTargetEvent(EntityTargetEvent event) {
        NamespacedKey key = new NamespacedKey(Torture.plugin, "hiddenName");
        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (entity.getPersistentDataContainer() != null) {
            if(entity.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                if (entity.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase("Walking to")) {
                    if (target instanceof Villager) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void dragEvent(InventoryDragEvent event) {
        Player player = (Player) event.getView().getPlayer();
        player.sendMessage(event.getRawSlots().toString());
        if (event.getView() != null) {
            if (event.getView().getTitle().contains("mob box")) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void invClickEvent(InventoryClickEvent event) {
        InventoryView invView = event.getView();
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) invView.getPlayer();
        if (invView.getTitle().contains("mob box")) event.setCancelled(pcBoxCode(player, event.getSlot(), event.getClick(), event.getClickedInventory(), invView));
        if (invView.getTitle().equalsIgnoreCase("§fSelect Attack")) {
            battleClass.battleTypes type = null;
            if(clickedItem != null) if(clickedItem.getType() != Material.AIR){
                NamespacedKey key = new NamespacedKey(Torture.getInstance(), "moveNum");
                int indexNum = 0;
                if(clickedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
                    for(battleClass battle : Torture.battles){
                        if(battle.getCompetitors().containsKey(player)){
                            type = battle.getBattleType();
                            if(type == battleClass.battleTypes.SOLOPVE || type == battleClass.battleTypes.SOLOPVP)
                                battleEngine.makeMove(mobDex.moveList(clickedItem.getItemMeta().getDisplayName().substring(2)), player);
                            else{
                                battleEngine.showTargetMenu();
                            }
                            battle.setPlayerCondition(player, true);
                            Torture.battles.set(indexNum, battle);
                            break;
                        }
                        indexNum++;
                    }

                }

            }
            event.setCancelled(true);
            player.closeInventory();
        }

    }


    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Torture.openBox(100, player);
        Torture.data.getConfig().set(player.getUniqueId() + " route", 1);
        Torture.data.saveConfig();
        player.setWalkSpeed(0.2f);
    }

    public static Boolean pcBoxCode(Player player, int slotNum, ClickType clickType, Inventory inventory, InventoryView inventoryView) {
        if (slotNum != -999) {
            if (player.getOpenInventory().getTitle().contains("mob box")) { //start of trying to manage player box
                Boolean bool = true;
                if (inventory != null) {
                    if (inventory.getType() == InventoryType.PLAYER) bool = false;
                }
                if (bool) {
                    if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) { //not talking about shift clicking
                        int boxNum = Integer.parseInt(inventoryView.getTitle().split(" ")[2]); //get the player's box number (mob box #)
                        ConfigurationSection boxSection = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " box" + boxNum + " slot " + (slotNum + 1));


                        if (Torture.data.getConfig().contains("player " + player.getUniqueId() + " box" + boxNum + " slot " + (slotNum + 1))) {
                            Map<String, Object> boxMap = boxSection.getValues(false);
                            if (Torture.temp.getConfig().contains(player.getUniqueId() + " mouseCursor")) {
                                ConfigurationSection cursorSection = Torture.temp.getConfig().getConfigurationSection(player.getUniqueId() + " mouseCursor");
                                Map<String, Object> cursorMap = cursorSection.getValues(false);
                                Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", boxMap);
                                if(cursorMap == null){
                                }
                                Torture.data.getConfig().set("player " + player.getUniqueId() + " box" + boxNum + " slot " + (slotNum + 1), cursorMap);
                            } else {
                                Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", boxMap);
                                Torture.data.getConfig().set("player " + player.getUniqueId() + " box" + boxNum + " slot " + (slotNum + 1), null);
                            }


                        } else if (Torture.temp.getConfig().contains(player.getUniqueId() + " mouseCursor")) {
                            ConfigurationSection cursorSection = Torture.temp.getConfig().getConfigurationSection(player.getUniqueId() + " mouseCursor");
                            Map<String, Object> cursorMap = cursorSection.getValues(false);
                            Torture.data.getConfig().set("player " + player.getUniqueId() + " box" + boxNum + " slot " + (slotNum + 1), cursorMap);
                            Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", null);
                        }

                    } else {
                        return true;
                    }
                } else {
                    if (slotNum == 1 || slotNum == 2 || slotNum == 3 || slotNum == 4 || slotNum == 5 || slotNum == 0) {
                        if (Torture.temp.getConfig().contains(player.getUniqueId() + " mouseCursor")) {
                            ConfigurationSection cursorSection = Torture.temp.getConfig().getConfigurationSection(player.getUniqueId() + " mouseCursor");
                            Map<String, Object> cursorMap = cursorSection.getValues(false);
                            if (Torture.data.getConfig().contains("player " + player.getUniqueId() + " hotbar " + (slotNum + 1))) {
                                ConfigurationSection hotbarSection = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " hotbar " + (slotNum + 1));
                                Map<String, Object> hotbarMap = hotbarSection.getValues(false);
                                Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", hotbarMap);
                            } else {
                                Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", null);
                            }
                            Torture.data.getConfig().set("player " + player.getUniqueId() + " hotbar " + (slotNum + 1), cursorMap);
                        } else {
                            if (Torture.data.getConfig().contains("player " + player.getUniqueId() + " hotbar " + (slotNum + 1))) {
                                ConfigurationSection hotbarSection = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " hotbar " + (slotNum + 1));
                                Map<String, Object> hotbarMap = hotbarSection.getValues(false);
                                Torture.temp.getConfig().set(player.getUniqueId() + " mouseCursor", hotbarMap);
                                Torture.data.getConfig().set("player " + player.getUniqueId() + " hotbar " + (slotNum + 1), null);
                            }
                        }
                    } else {
                        return true;
                    }
                }
                Torture.data.saveConfig();
                Torture.temp.saveConfig();

            } else {
                return true;
            }

        }else return true;
        return false;
    }
    @EventHandler
    public void InteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem() != null){
                if(event.getItem().equals(ItemManager.showMobMoves)){
                    Inventory inv = Bukkit.createInventory(player, InventoryType.CHEST, "§fSelect Attack");
                    mobEnums mob = (mobEnums) Torture.playerSpecifics.get(player.getUniqueId() + " currentMob");
                    ItemStack mobIcon = mob.getIcon();
                    ItemMeta itemMeta = mobIcon.getItemMeta();
                    itemMeta.setDisplayName("§f" + mob.getName());
                    mobIcon.setItemMeta(itemMeta);
                    inv.setItem(13, mobIcon);
                    List<Integer> numList = new ArrayList<>();
                    numList.add(4);
                    numList.add(12);
                    numList.add(14);
                    numList.add(22);
                    int tracker = 0;
                    ItemStack moveIcon;
                    for(moveClass moveClass : mob.getMoves()){

                        if(moveClass.getIcon().getItemMeta() != null){
                            itemMeta = moveClass.getIcon().getItemMeta();
                            itemMeta.setDisplayName("§f" + moveClass.getMoveName());
                            moveIcon = moveClass.getIcon();
                            NamespacedKey key = new NamespacedKey(Torture.getInstance(), "moveNum");
                            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, moveClass.getMoveNum());
                            moveIcon.setItemMeta(itemMeta);
                            inv.setItem(numList.get(tracker), moveIcon);
                            tracker++;
                        }


                    }
                    player.openInventory(inv);
                }
            }
        }
    }
    @EventHandler
    public void SwapHands(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        if(Torture.playerSpecifics.get(player.getUniqueId() + " stopMessage") != null){
            String message = (String) Torture.playerSpecifics.get(player.getUniqueId() + " fullMessage");
            if((Torture.playerSpecifics.get(player.getUniqueId() + " entity message") != null)){
                if(Torture.playerSpecifics.get(player.getUniqueId() + " entity message").toString().equalsIgnoreCase(message)){
                    ScheduledFuture scheduledFuture = (ScheduledFuture) Torture.playerSpecifics.get(player.getUniqueId() + " stopMessage");
                    scheduledFuture.cancel(true);
                    Torture.playerSpecifics.put(player.getUniqueId() + " fullMessage", null);
                    NamespacedKey key = new NamespacedKey(Torture.getInstance(), "humanDexNum");
                    Entity entity = (Entity) Torture.playerSpecifics.get(player.getUniqueId() + " walkingEntityForPVE");
                    humanClass human = humanDex.getHuman(entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER),entity);
                    battleEngine.startPveBattle(player, human, entity);
                }else{
                    Torture.playerSpecifics.put(player.getUniqueId() + " entity message", Torture.playerSpecifics.get(player.getUniqueId() + " fullMessage"));
                }
            }


        }


        event.setCancelled(true);
    }
}
