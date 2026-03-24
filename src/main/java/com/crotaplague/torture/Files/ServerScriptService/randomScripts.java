package com.crotaplague.torture.Files.ServerScriptService;

import com.crotaplague.torture.Files.ServerStorage.AnimationParts.AnimationManager;
import com.crotaplague.torture.Files.ServerStorage.AnimationParts.Stage;
import com.crotaplague.torture.Files.ServerStorage.SaveFile;
import com.crotaplague.torture.Files.ServerStorage.battleClass;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.crotaplague.torture.Events.events.confirmingAMove;
import static com.crotaplague.torture.Files.ServerScriptService.battleEngine.faceLoc;
import static com.crotaplague.torture.Torture.*;
import static java.lang.Thread.sleep;

public class randomScripts {
    public static void actionBarMessage(Player player, String message, SaveFile.MessagePurpose purpose) {
        SaveFile file = Torture.playerSaveFiles.get(player.getUniqueId() + "");
        if (file.messageQueue().size() != 0 || Torture.playerSpecifics.get(player.getUniqueId() + " fullMessage") != null) {
            file.addToQueue(message, purpose);
            Torture.playerSaveFiles.put(player.getUniqueId() + "", file);
            return;
        }
        file.setMessagePurpose(purpose);
        Torture.playerSaveFiles.put(player.getUniqueId() + "", file);

        message = message + " §3[" + "§eF" + "§3]";
        message = ChatColor.translateAlternateColorCodes('§', message);

        if (!message.equals(Torture.playerSpecifics.get(player.getUniqueId() + " entity message"))) {
            Torture.playerSpecifics.put(player.getUniqueId() + " fullMessage", message);
        }

        List<Character> charList = message.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger();
        Torture.playerSpecifics.put(player.getUniqueId() + " currentMessageSpot", null);
        String finalMessage = message;

        Runnable task = () -> {
            String innerString = (String) Torture.playerSpecifics.get(player.getUniqueId() + " currentMessageSpot");
            if (innerString == null) {
                innerString = charList.get(counter.getAndIncrement()).toString();
            } else {
                if (!innerString.equalsIgnoreCase(finalMessage) && (counter.get() + 1) < charList.size()) {
                    innerString = innerString + charList.get(counter.getAndIncrement());
                }
            }

            if (innerString.endsWith("§")) {
                innerString = innerString + charList.get(counter.getAndIncrement());
                innerString = innerString + charList.get(counter.getAndIncrement());
            }

            Torture.playerSpecifics.put(player.getUniqueId() + " currentMessageSpot", innerString);

            Component adventureComponent = LegacyComponentSerializer.legacySection().deserialize(innerString);
            player.sendActionBar(adventureComponent); // Modern API
        };

        BukkitTask newTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, 0, 1);
        Torture.playerSpecifics.put(player.getUniqueId() + " stopMessage", newTask);
    }
    public static boolean battleMessage(LivingEntity player, String message){
        final String[] oneByOne = {""};
        if(player instanceof Player)
        for(char aChar : message.toCharArray()){
            try{
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                oneByOne[0] = oneByOne[0] + aChar;
                Component comp = Component.text(oneByOne[0]);
                player.sendActionBar(comp);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void battleMessage(LivingEntity player, String message, boolean async) {
        if (!(player instanceof Player)) return;

        final StringBuilder oneByOne = new StringBuilder();
        final char[] chars = message.toCharArray();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (char c : chars) {
                oneByOne.append(c);
                Component comp = Component.text(oneByOne.toString());
                // Schedule sending the action bar on the main thread
                player.sendActionBar(comp);

                try {
                    Thread.sleep(30); // This is safe here since it's on an async thread
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }


    public static int getRandomNumber(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be ≤ max");
        }
        // (max - min + 1) is the count of possible values
        return (int)(Math.random() * (max - min + 1)) + min;
    }

    public static <K, V> Set<K> getKeys(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static <T> List<T> ArrayToListConversion(T array[])
    {
//creating the constructor of the List class
        List<T> list = new ArrayList<>();
//using for-each loop to iterate over the array
        for (T t : array)
        {
//adding each element to the List
            list.add(t);
        }
//returns the list converted into Array
        return list;
    }

    public static int findIndex(List<Object> arr, Object t)
    {

        // if array is Null
        if (arr == null) {
            return -1;
        }

        // find length of array
        int len = arr.size();
        int i = 0;

        // traverse in the array
        while (i < len) {

            // if the i-th element is t
            // then return the index
            if (arr.get(0).equals(t)) {
                return i;
            }
            else {
                i = i + 1;
            }
        }
        return -1;
    }
    public static Color fromHex(int hex){
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        Color bukkitColor = Color.fromBGR(r, g, b);
        return bukkitColor;
    }

    public static void actionBarMessage(Player player, String message, SaveFile.MessagePurpose purpose, boolean force) {
        SaveFile file = Torture.playerSaveFiles.get(player.getUniqueId() + "");
        if ((file.messageQueue().size() != 0 && !force) || Torture.playerSpecifics.get(player.getUniqueId() + " fullMessage") != null) {
            file.addToQueue(message, purpose);
            Torture.playerSaveFiles.put(player.getUniqueId() + "", file);
            return;
        }

        file.setMessagePurpose(purpose);
        Torture.playerSaveFiles.put(player.getUniqueId() + "", file);

        message = message + " §3[" + "§eF" + "§3]";
        message = ChatColor.translateAlternateColorCodes('§', message);

        if (!message.equals(Torture.playerSpecifics.get(player.getUniqueId() + " entity message"))) {
            Torture.playerSpecifics.put(player.getUniqueId() + " fullMessage", message);
        }

        List<Character> charList = message.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger();
        Torture.playerSpecifics.put(player.getUniqueId() + " currentMessageSpot", null);
        String finalMessage = message;

        Runnable task = () -> {
            String innerString = (String) Torture.playerSpecifics.get(player.getUniqueId() + " currentMessageSpot");

            if (innerString == null) {
                innerString = charList.get(counter.getAndIncrement()).toString();
            } else {
                if (!innerString.equalsIgnoreCase(finalMessage) && (counter.get() + 1) < charList.size()) {
                    innerString = innerString + charList.get(counter.getAndIncrement());
                }
            }

            if (innerString.endsWith("§")) {
                innerString = innerString + charList.get(counter.getAndIncrement());
                innerString = innerString + charList.get(counter.getAndIncrement());
            }

            Torture.playerSpecifics.put(player.getUniqueId() + " currentMessageSpot", innerString);

            Component component = LegacyComponentSerializer.legacySection().deserialize(innerString);
            player.sendActionBar(component); // Paper modern API
        };

        BukkitTask newTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, 0, 1);
        Torture.playerSpecifics.put(player.getUniqueId() + " stopMessage", newTask);
    }

    public static List<Location> generateSphere(Location centerBlock, int radius, boolean hollow) {
        if (centerBlock == null) {
            return new ArrayList<>();
        }

        List<Location> circleBlocks = new ArrayList<Location>();

        int bx = centerBlock.getBlockX();
        int by = centerBlock.getBlockY();
        int bz = centerBlock.getBlockZ();

        for(int x = bx - radius; x <= bx + radius; x++) {
            for(int y = by - radius; y <= by + radius; y++) {
                for(int z = bz - radius; z <= bz + radius; z++) {

                    double distance = ((bx-x) * (bx-x) + ((bz-z) * (bz-z)) + ((by-y) * (by-y)));

                    if(distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {

                        Location l = new Location(centerBlock.getWorld(), x, y, z);

                        circleBlocks.add(l);

                    }

                }
            }
        }

        return circleBlocks;
    }

    /**
     * Opens a menu with all the animations in it
     * @param stage the stage to load the animations from
     * @param player the player to open the menu for
     */
    public static void openStageAnimations(Stage stage, Player player){
        Inventory inv = Bukkit.createInventory(player, 27, "Left Click add before || Scroll Click play || Right Click add after");
        if(stage.getAnimations().size() > 0){
            int counter = 0;
            for(AnimationManager a : stage.getAnimations()){
                player.sendMessage("AOWUDHaWD");
                ItemStack item = menuItem(a);

                inv.setItem(counter, item);
                counter++;
            }
        }
        player.openInventory(inv);
    }

    /**
     * Opens a menu with all the animations in it with a specific name
     * @param stage the stage to load the animations from
     * @param player the player to open the menu for
     * @param title the title of the menu
     */
    public static void openStageAnimations(Stage stage, Player player, TextComponent title){
        Inventory inv = Bukkit.createInventory(player, 27, title);
        if(!stage.getAnimations().isEmpty()){
            int counter = 0;
            for(AnimationManager a : stage.getAnimations()){
                ItemStack item = menuItem(a);

                inv.setItem(counter, item);
                counter++;
            }
        }
        player.openInventory(inv);
    }

    private static ItemStack menuItem(AnimationManager a){
        ItemStack item = null;
        if(a.hasDisguise()){ item = new ItemStack(Material.CREEPER_SPAWN_EGG);}else
        if(a.isCameraTp()) item = new ItemStack(Material.PLAYER_HEAD); else
        if(a.hasGoal()){item = new ItemStack(Material.PAPER);}else
        if(a.hasText()){item = new ItemStack(Material.BOOK);}else
        if(a.hasComments()){List<String> lore = new ArrayList<>(); lore.add("§f" + a.getComments()); if(a.simultaneous()) lore.add("§aSimultaneous"); item.getItemMeta().setLore(lore);}
        return item;
    }

    public static TextDisplay generateMessageBox(Player p, Location loc, String message) {
        // CONFIGURABLES
        final int maxCharsPerLine = 32;      // how many characters before we force a new line
        final float expandPerCharX = 0.1f;   // how much to grow in X when you overflow a line
        final float expandPerLineY = 0.2f;   // how much to grow in Y when you add a new line
        final long tickInterval = 2L;        // 2 ticks between each character

        World world = p.getWorld();
        // Face the player
        loc = loc.clone();
        loc.setPitch(0);

        List<String> wrapped = new ArrayList<>();
        {
            String[] words = message.split(" ");
            StringBuilder line = new StringBuilder();
            for (String w : words) {
                if (line.length() == 0) {
                    line.append(w);
                } else if (line.length() + 1 + w.length() <= maxCharsPerLine) {
                    line.append(' ').append(w);
                } else {
                    wrapped.add(line.toString());
                    line = new StringBuilder(w);
                }
            }
            wrapped.add(line.toString());
        }

        // 1) Spawn background “box”

        Location base = loc.clone().add(0, 1.25, 0);

        Vector forward = base.getDirection().clone().normalize();
        forward = forward.setY(0);
        forward = forward.normalize();

        Vector up   = new Vector(0, 1, 0);
        Vector left = up.crossProduct(forward).normalize();

        forward = forward.multiply(0.5);   // 0.5 blocks forward
        left = left   .multiply(1.2);   // 0.5 blocks to the left

        Location spawn = base.clone()
                .add(forward)    // move forward
                .add(left);      // then move left


        TextDisplay bg = world.spawn(spawn, TextDisplay.class);
        bg.setInvulnerable(true);
        bg.setGravity(false);
        bg.setText("█");                     // single block to get a white rectangle
        bg.setBackgroundColor(Color.WHITE);
        Transformation bgT = bg.getTransformation();
        bgT.getScale().set(4.0F, 1.0F, 0.0F);  // initial approx width=4, height=1
        bg.setTransformation(bgT);
        bg.setRotation((loc.getYaw()+180) % 360, 0);
        p.showEntity(plugin, bg);


        base = loc.clone().add(0, 3, 0);

        forward = base.getDirection().clone();
        forward.setY(0);
        forward.normalize();          // now (x,z) is a unit vector pointing where loc faces

        left = up.crossProduct(forward).normalize();

        forward = forward.multiply(0.52);   // 0.5 blocks forward
        left = left   .multiply(2.25);   // 0.5 blocks to the left

        spawn = base.clone()
                .add(forward)    // move forward
                .add(left);



        TextDisplay txt = world.spawn(spawn, TextDisplay.class);

        txt.setInvulnerable(true);
        txt.setGravity(false);
        txt.setAlignment(TextDisplay.TextAlignment.CENTER);
        txt.setBackgroundColor(Color.WHITE);
        txt.setLineWidth(maxCharsPerLine * 5);  // tweak 5 px per char as needed
        p.showEntity(plugin, txt);

        // State for the scheduler
        String fullWrapped = String.join("\n", wrapped);
        AtomicInteger idx = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                int i = idx.getAndIncrement();
                if (i > fullWrapped.length()) {
                    cancel();
                    return;
                }

                // reveal substring [0..i)
                String portion = ChatColor.BLACK + fullWrapped.substring(0, i);
                txt.setText(portion);

                // auto‑scale background to exactly longest‑line × rows
                int longest  = wrapped.stream().mapToInt(String::length).max().orElse(0);
                int rowCount = wrapped.size();
                Transformation t = txt.getTransformation();
                t.getScale().set(
                        longest * (5f/16f),  // roughly px→block scale
                        rowCount * expandPerLineY,
                        0
                );
                bg.setTransformation(t);

            }
        }.runTaskTimer(plugin, 0, tickInterval);

        return txt;
    }

    public static TextComponent applyMoveTo(mobEnums user, mobEnums target, moveClass move, battleClass b) {
        if(move.getTarget() == moveClass.moveEffectTarget.SELF){
            if(!target.equals(user)) return Component.text("Move can only be applied to mob.", NamedTextColor.RED);
        }
        if(move.getTarget() == moveClass.moveEffectTarget.ENEMY){
            if(target.equals(user)) return Component.text("Move can not be used on own mob.", NamedTextColor.RED);
        }
        if(move.hasEffect()){
            moveClass.Stat stat = move.getEffectStat();
            if(Math.abs(target.getStages()[stat.getValue()]) >= 6){
                return Component.text("Mob already has " + stat.getName() + " as low as possible.", NamedTextColor.RED);
            }
        }
        move.setMobTarget(target);
        b.setMobStatus(user);
        confirmingAMove(b, user.getTrainer(), move);
        return null;
    }

    public static ItemStack createMobDisplayItem(mobEnums mob) {
        ItemStack disp = mob.getIcon().clone();
        ItemMeta meta = disp.getItemMeta();
        double hpRatio = ((double) mob.getCurrentHp()) / mob.getMaxHp();

        // Determine color: green → yellow → red
        int r, g, blue = 0;
        if (hpRatio >= 0.5) {
            // Interpolate from green (0,255,0) to yellow (255,255,0)
            double t = (hpRatio - 0.5) * 2; // scaled to [0,1]
            r = (int) Math.round(255 * (1 - t)); // from 255 to 0 as t→1
            g = 255;
        } else {
            // Interpolate from yellow (255,255,0) to red (255,0,0)
            double t = hpRatio * 2; // scaled to [0,1]
            r = 255;
            g = (int) Math.round(255 * t); // from 0 to 255 as t→1 (reverse if needed)
        }

        List<Component> lore = new ArrayList<>();

        // Build the bar
        final int totalSquares = 20;
        int filledSquares = (int) Math.round(hpRatio * totalSquares);

        Component bar = Component.text("");
        for (int i = 0; i < totalSquares; i++) {
            if (i < filledSquares) {
                // Filled part (on the left side)
                bar = bar.append(Component.text("▌", TextColor.color(r, g, blue)));
            } else {
                // Empty part (gray, on the right side)
                bar = bar.append(Component.text("▌", TextColor.color(64, 64, 64)));
            }
        }

        // Add the bar as lore
        lore.add(bar.decoration(TextDecoration.ITALIC, false));
        String levelText = "Lv. " + mob.getLevel();
        String hpText = mob.getCurrentHp() + "/" + mob.getMaxHp();

        int totalWidth = 20; // adjust as desired for your GUI alignment

        int usedWidth = levelText.length() + hpText.length();
        int padding = totalWidth - usedWidth;
        if (padding < 0) padding = 0;

        String spaces = " ".repeat(padding);
        String fullLine = levelText + spaces + hpText;

        lore.add(Component.text(fullLine, NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        if(mob.getCondition() != null){
            lore.add(Component.text(mob.getCondition().getCondition().getName(), mob.getCondition().getCondition().getColor()));
        }
        meta.lore(lore);
        meta.displayName(Component.text(mob.getName(), NamedTextColor.WHITE).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false).append(Component.text(" " + mob.getGenderAsSymbol(), mob.getGenderAsColor()).decoration(TextDecoration.BOLD, false)));

        disp.setItemMeta(meta);
        return disp;
    }

    @Nullable
    public static SaveFile getPlayerSaveFile(Player player){
        SaveFile file = null;
        if(playerSaveFiles.containsKey(player.getUniqueId().toString())) {
            file = playerSaveFiles.get(player.getUniqueId().toString());
        }
        return file;
    }
    public static void sendOutMob(mobEnums mob, Location sendTo){
        sendOutMob(mob, sendTo, (Runnable) null);
    }
    public static void sendOutMob(mobEnums mob, Location sendTo, Runnable onComplete) {
        World world = sendTo.getWorld();
        double endX = sendTo.getX();
        humanClass.Trainer t = mob.getTrainer();
        Vector vec = t.getSelf().getEyeLocation().getDirection().normalize().multiply(-2.9);
        Location loc = t.getSelf().getEyeLocation().add(vec);
        loc.setY(loc.getY());
        loc.setZ(sendTo.getZ());

        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(mob.getMobBall().getDisplayItem());
        stand.setInvisible(true);

        faceLoc(stand, sendTo.add(new Vector(0, 2, 0)));

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
                Mob m = (Mob) world.spawnEntity(sendTo, mob.getMojangMobType(), CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {e.setVisibleByDefault(false); e.setSilent(true); e.setInvulnerable(true); ((Mob) e).setAI(false);
                    e.customName(Component.text(mob.getCurrentHp(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)); e.setCustomNameVisible(true);});
                battleClass battle = getBattle(mob);
                battle.viewEntity(m);
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

                if (current.getX() < sendTo.getX()) {
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

    public static battleClass getBattle(Entity ent){
        for(battleClass b : battles){
            for(Map.Entry<Integer, mobEnums> entry : b.getMobs().entrySet()){
                mobEnums y = entry.getValue();
                if(y.getSelf() != null && y.getSelf().equals(ent)){
                    return b;
                }
            }
            for(humanClass.Trainer t : b.getCompetitors()){
                if(t.getSelf().equals(ent)){
                    return b;
                }
            }
        }
        return null;
    }
    public static battleClass getBattle(mobEnums m){
        for(battleClass b : battles){
            for(Map.Entry<Integer, mobEnums> entry : b.getMobs().entrySet()){
                mobEnums y = entry.getValue();
                if(y.equals(m)){
                    return b;
                }
            }
            for(humanClass.Trainer t : b.getCompetitors()){
                for(mobEnums a : t.getMobs()){
                    if(a.equals(m)){
                        return b;
                    }
                }
            }
        }
        return null;
    }

    public static void sendOutMob(mobEnums mob, Location sendTo, Mob fake){
        sendOutMob(mob, sendTo, fake, null);
    }

    public static void sendOutMob(mobEnums mob, Location sendTo, Mob fake, Runnable onComplete) {
        sendOutMob(mob, sendTo, fake.getLocation(), onComplete);
    }

    public static void genMoveTarget(battleClass b, moveClass m, mobEnums user){
        if(m.getMobTarget() != null){

            return;
        }
        Bukkit.getPlayer("CrotaPlague").sendMessage("The target is: " + m.getTarget());
        if(m.getTarget() == moveClass.moveEffectTarget.SELF){
            m.setMobTarget(user);
            return;
        }
        if(m.getTarget() == moveClass.moveEffectTarget.ENEMY){
            Bukkit.getPlayer("CrotaPlague").sendMessage("The spot is: " + b.getBattleSlot(user));
            if(b.getBattleSlot(user) == 1 || b.getBattleSlot(user) == 3){
                int a = randomOf(2, 4);
                if(b.getMobs().containsKey(a) && b.getMobs().get(a) != null){
                    m.setMobTarget(b.getMobInPosition(a));
                }else{
                    a = a == 2 ? 4 : 2;
                    m.setMobTarget(b.getMobInPosition(a));
                }
            }else{
                int a = randomOf(1, 3);
                if(b.getMobs().containsKey(a) && b.getMobs().get(a) != null){
                    Bukkit.getPlayer("CrotaPlague").sendMessage("Are either running here?");
                    m.setMobTarget(b.getMobInPosition(a));
                }else{
                    a = a == 1 ? 3 : 1;
                    Bukkit.getPlayer("CrotaPlague").sendMessage("Could also be this");
                    m.setMobTarget(b.getMobInPosition(a));
                }
            }
        }
    }

    /**
     * Returns a random integer from the provided values.
     *
     * @param values the integers to choose from
     * @return a randomly selected integer from values
     * @throws IllegalArgumentException if no values are provided
     */
    public static int randomOf(int... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one value must be provided.");
        }
        int index = getRandomNumber(0, values.length - 1);
        return values[index];
    }

    public static void hideClaimed(Player player){
        SaveFile file = getPlayerSaveFile(player);
        for(MagmaCube e : player.getWorld().getEntitiesByClass(MagmaCube.class)){
            NamespacedKey key = new NamespacedKey(plugin, "itemNumber");
            if(e.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
                Integer a = e.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                if(a != null){
                    if(file.hasClaimedItem(a)){
                        List<ArmorStand> stands = e.getWorld().getEntities().stream()
                                .filter(ent -> ent instanceof ArmorStand)
                                .map(ent -> (ArmorStand) ent)
                                .toList();

                        Location targetLoc = e.getLocation();
                        ArmorStand closest = stands.stream()
                                .min(Comparator.comparingDouble(b -> b.getEyeLocation().distance(targetLoc)))
                                .orElse(null);

                        if(closest != null){
                            player.hideEntity(plugin, closest);
                        }
                        player.hideEntity(plugin, e);
                    }else{
                        player.sendMessage("Look for this: " + a);
                    }
                }
            }
        }
    }

    public static boolean inBattle(Player player){
        for(battleClass b : battles){
            for(humanClass.Trainer t : b.getCompetitors()){
                if(t.getSelf() != null && t.getSelf().equals(player)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void sendOutMob(mobEnums mob, Location sendTo, Location fake, Runnable onComplete) {
        World world = sendTo.getWorld();
        double endX = sendTo.getX();
        Vector vec = fake.getDirection().normalize().multiply(-2.9);
        Location loc = fake.add(vec);
        loc.setY(loc.getY());
        loc.setZ(sendTo.getZ());

        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(mob.getMobBall().getDisplayItem());
        stand.setInvisible(true);
        final Location copy = sendTo.clone();
        faceLoc(stand, copy.add(new Vector(0, 2, 0)));

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
                Mob m = (Mob) world.spawnEntity(sendTo, mob.getMojangMobType(), CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {e.setVisibleByDefault(false); e.setSilent(true); e.setInvulnerable(true); ((Mob) e).setAI(false);
                    e.customName(Component.text(mob.getCurrentHp(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)); e.setCustomNameVisible(true);});
                battleClass battle = getBattle(mob);
                battle.viewEntity(m);
                mob.setSelf(m);
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

                if (current.getX() < sendTo.getX()) {
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

}





































// Open this link after the project is completed
// https://media.discordapp.net/attachments/493913675867095040/1016898787639308289/unknown.png?width=861&height=484