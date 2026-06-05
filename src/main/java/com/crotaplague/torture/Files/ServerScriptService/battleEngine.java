package com.crotaplague.torture.Files.ServerScriptService;


import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.CQueue;
import com.crotaplague.torture.Files.ServerStorage.SaveFile;
import com.crotaplague.torture.Files.ServerStorage.NextMove;
import com.crotaplague.torture.Files.ServerStorage.battleClass;
import com.crotaplague.torture.Files.ServerStorage.battleTypes;
import com.crotaplague.torture.Files.ServerStorage.boxs.ArenaBox;
import com.crotaplague.torture.Files.ServerStorage.boxs.boxDex;
import com.crotaplague.torture.Files.ServerStorage.disguises.*;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import com.crotaplague.torture.Files.ServerStorage.specialConditions.SpecialConditions;
import com.crotaplague.torture.Torture;
import com.destroystokyo.paper.entity.Pathfinder;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.*;

import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.crotaplague.torture.Files.ServerScriptService.randomScripts.*;
import static com.crotaplague.torture.Files.ServerStorage.battleTypes.*;
import static com.crotaplague.torture.Torture.*;


public class battleEngine {

    public static void faceLoc(LivingEntity creatureMob, LivingEntity opponentCreature) {
        Location tempLoc = creatureMob.getLocation();
        Vector vec = creatureMob.getEyeLocation().toVector().subtract(opponentCreature.getEyeLocation().toVector());
        Location loc = creatureMob.getEyeLocation().setDirection(vec.multiply(-1));
        loc.setX(tempLoc.getX());
        loc.setY(tempLoc.getY());
        loc.setZ(tempLoc.getZ());
        creatureMob.teleport(loc);
    }
    public static void faceLoc(LivingEntity creatureMob, Location opponentCreature) {
        Location tempLoc = creatureMob.getLocation();
        Vector vec = creatureMob.getEyeLocation().toVector().subtract(opponentCreature.toVector());
        Location loc = creatureMob.getEyeLocation().setDirection(vec.multiply(-1));
        loc.setX(tempLoc.getX());
        loc.setY(tempLoc.getY());
        loc.setZ(tempLoc.getZ());
        creatureMob.teleport(loc);
    }
    public static Location faceLoc(Creature creatureMob, Creature opponentCreature, boolean trueOr) {
        Location tempLoc = creatureMob.getLocation();
        Vector vec = creatureMob.getEyeLocation().toVector().subtract(opponentCreature.getEyeLocation().toVector());
        Location loc = creatureMob.getEyeLocation().setDirection(vec.multiply(-1));
        loc.setX(tempLoc.getX());
        loc.setY(tempLoc.getY());
        loc.setZ(tempLoc.getZ());
        return loc;
    }

    public static void makeMove(moveClass move, battleClass battle, mobEnums mob, Runnable onComplete) {
        if (battle.getBattleType() == battleTypes.SOLOPVE || battle.getBattleType() == battleTypes.SOLOPVP) {
            entMove(mob, move.getMobTarget(), move, battle, () -> {
                boolean hasMoreMob = true;
                if (move.getMobTarget().getCurrentHp() <= 0) {
                    hasMoreMob = false;
                    if (move.getMobTarget().hasTrainer()) {
                        humanClass.Trainer defenderTrainer = move.getMobTarget().getTrainer();
                        for (mobEnums mob1 : defenderTrainer.getMobs()) {
                            if (mob1 != null && mob1.getCurrentHp() > 0) {
                                hasMoreMob = true;
                                break;
                            }
                        }
                    } else {
                        for (Map.Entry<Integer, mobEnums> entry : battle.getMobs().entrySet()) {
                            if (!entry.getValue().hasTrainer() && entry.getValue().getCurrentHp() > 0) {
                                hasMoreMob = true;
                                break;
                            }
                        }
                    }
                }

                if (hasMoreMob) {
                    if (mob.getCondition() != null) {
                        SpecialConditions cond = mob.getCondition();
                        if (cond.getCondition().getEffect() == SpecialConditions.effect.DAMAGE) {
                            if (randomScripts.getRandomNumber(1, 100) >= cond.getCondition().getPercent()) {
                                int fullHp = mob.getStats()[moveClass.Stat.HIT_POINTS.getValue()];
                                int damage = Math.round(fullHp * cond.getCondition().getDamage());
                                mob.setCurrentHp(mob.getCurrentHp() - damage);
                            }
                        }
                    }
                    if (onComplete != null) onComplete.run();
                } else {
                    if (mob.hasTrainer() && mob.getTrainer().getSelf() instanceof Player) {
                        humanClass.Trainer attackerTrainer = mob.getTrainer();
                        if (move.getMobTarget().hasTrainer()) {
                            humanClass.Trainer defender = move.getMobTarget().getTrainer();
                            if (defender.getSelf() instanceof Player) {
                                randomScripts.actionBarMessage((Player) attackerTrainer.getSelf(), attackerTrainer.getName() + " won the match!", SaveFile.MessagePurpose.BATTLE_END);
                            } else {
                                randomScripts.actionBarMessage((Player) attackerTrainer.getSelf(), attackerTrainer.getName() + " received " + defender.getCashReward() + " dollars for winning!", SaveFile.MessagePurpose.BATTLE_END);
                            }
                        }
                    }

                    if (move.getMobTarget().hasTrainer() && move.getMobTarget().getTrainer().getSelf() instanceof Player) {
                        humanClass.Trainer defender = move.getMobTarget().getTrainer();
                        randomScripts.battleMessage(defender.getSelf(), "You are out of mobs and have lost the battle.");
                    }

                    if (mob.hasTrainer() && move.getMobTarget().hasTrainer() && !(move.getMobTarget().getTrainer().getSelf() instanceof Player)) {
                        SaveFile file = Torture.playerSaveFiles.get(mob.getTrainer().getSelf().getUniqueId() + "");
                        humanClass.Trainer defender = move.getMobTarget().getTrainer();
                        file.addDefeatedTrainer(defender.getDexNum());
                        file.addCash(defender.getCashReward());
                    }

                    battle.setBattleOver();
                    if (onComplete != null) onComplete.run();
                }
            });
        } else {
            if (onComplete != null) onComplete.run();
        }
    }
    public static BukkitTask makeEntWalk(Entity attackerEnemy, Player player, Entity humans){
        player.setFoodLevel(6);
        player.setWalkSpeed(0);
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
        NamespacedKey key = new NamespacedKey(Torture.getInstance(), "hiddenName");
        if(attackerEnemy != null) {
            Mob walker = (Mob) player.getWorld().spawnEntity(attackerEnemy.getLocation(), EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM, mo -> {mo.setVisibleByDefault(false);});
            Villager villager = (Villager) walker;
            villager.setAdult();


            Creature creature = (Creature) walker;
            creature.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            creature.setInvulnerable(true);

            creature.setCustomNameVisible(false);
            creature.setAI(true);
            NamespacedKey personalMob = new NamespacedKey(Torture.plugin, "PersonalMob");
            key = new NamespacedKey(Torture.getInstance(), "humanDexNum");
            Integer dexNum = attackerEnemy.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

            walker.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, dexNum);
            walker.getPersistentDataContainer().set(personalMob, PersistentDataType.STRING, player.getUniqueId().toString());
            if(attackerEnemy.getUniqueId().equals(humans.getUniqueId())) {player.sendMessage(Component.text("thought so"));}
            DisguiseAppearance appear = disguiseManager.get(attackerEnemy).get().appearance();
            DisguiseSession session = disguiseManager.disguise(walker, appear);
            session.showDisguiseOnly();
            Location humanLoc = humans.getLocation();
            Location playerLoc = player.getLocation();
            Vector dir = playerLoc.toVector().subtract(humanLoc.toVector());
            dir = dir.normalize();
            double offset = 0.9;
            Vector offsetVec = dir.multiply(offset);
            Location targetLoc = playerLoc.clone().subtract(offsetVec);

            walker.setAI(true);
            SaveFile file = playerSaveFiles.get(player.getUniqueId() + "");
            file.setWalkingMob(walker);
            MobHandler.moveTo(walker, targetLoc);
        }
        return null;
    }
    private static void entMove(mobEnums playerMob, mobEnums opponentMob, moveClass move, battleClass battle, Runnable onComplete) {
        SpecialConditions playerMobCondition = playerMob.getCondition();
        if (playerMobCondition != null && playerMobCondition.getCondition().getEffect() == SpecialConditions.effect.STUN) {
            if (randomScripts.getRandomNumber(1, 100) >= playerMobCondition.getCondition().getPercent()) {
                if (onComplete != null) onComplete.run();
                return;
            }
        }

        int health = opponentMob.getCurrentHp();
        float defence = 0;
        int attackPower = move.getDamage();

        moveClass.Stat s = null;
        if (move.getMoveType().equalsIgnoreCase("special")) {
            s = moveClass.Stat.RANGED_ATTACK;
        } else if (move.getMoveType().equalsIgnoreCase("physical")) {
            s = moveClass.Stat.ATTACK;
        }

        float attackerAttackStat = 0;
        if (s != null) {
            attackerAttackStat = playerMob.calculateStat(s);
        }

        if (move.getMoveType().equalsIgnoreCase("special")) {
            defence = opponentMob.calculateStat(moveClass.Stat.RANGED_DEFENCE);
        } else if (move.getMoveType().equalsIgnoreCase("physical")) {
            defence = opponentMob.calculateStat(moveClass.Stat.DEFENCE);
        }

        if (move.hasEffect()) {
            int chance = randomScripts.getRandomNumber(1, 100);
            if (chance <= move.getPercent()) {
                mobEnums mobToEffect = switch (move.getEffectTarget()) {
                    case SELF -> playerMob;
                    case ENEMY -> opponentMob;
                };
                mobToEffect.changeStage(move.getEffectStat(), move.getEffectAmount());
            }
        }

        if (!move.getConditions().isEmpty()) {
            List<SpecialConditions> conditionsList = move.getConditions();
            int totalChance = 0;
            for (SpecialConditions condition : conditionsList) {
                totalChance += (condition.getCondition().getPercent() / conditionsList.size());
            }
            int random = randomScripts.getRandomNumber(1, 100);
            if (random <= totalChance) {
                List<SpecialConditions> result = conditionsList.stream()
                        .sorted(Comparator.comparingInt(o -> o.getCondition().getPercent()))
                        .collect(Collectors.toList());
                SpecialConditions previous = result.get(0);
                for (SpecialConditions condition : result) {
                    if (!((condition.getCondition().getPercent() / conditionsList.size()) >= random)) {
                        opponentMob.setCondition(previous);
                    }
                    previous = condition;
                }
            }
        }

        final int originalHealth = health;
        if (move.getMoveType().equals("physical") || move.getMoveType().equals("special")) {
            int damage = Math.round((float) (((((playerMob.getLevel() * 1.25) / 3.5) + 2) * (attackerAttackStat / defence) * attackPower)) / 30);
            health -= damage;
            opponentMob.setCurrentHp(health);
        }

        final int finalHealth = health;
        humanClass.Trainer opTrainer = opponentMob.getTrainer();

        if (playerMob.hasTrainer() && playerMob.getTrainer().getSelf() instanceof Player) {
            randomScripts.battleMessage((Player) playerMob.getTrainer().getSelf(), ChatColor.RED + playerMob.getName() + " used " + move.getMoveName());
        }
        if (opTrainer != null && opTrainer.getSelf() instanceof Player) {
            randomScripts.battleMessage((Player) opTrainer.getSelf(), ChatColor.RED + "The opposing " + playerMob.getName() + " used " + move.getMoveName());
        }

        // Animate health bar
        animateHealth(opponentMob, originalHealth, finalHealth, () -> {
            if (finalHealth <= 0) {
                opponentMob.getSelf().playEffect(EntityEffect.ENTITY_DEATH);
                battle.entDead(opponentMob);

                Bukkit.getScheduler().runTask(Torture.plugin, () -> {
                    Creature opponentCreature = (Creature) opponentMob.getSelf();
                    Sound s1 = opponentCreature.getDeathSound() != null ? opponentCreature.getDeathSound() : Sound.ENTITY_PLAYER_HURT;
                    opponentCreature.getWorld().playSound(opponentCreature.getLocation(), s1, Float.MAX_VALUE, 0.5f);
                });

                if (playerMob.hasTrainer() && playerMob.getTrainer().getSelf() instanceof Player) {
                    randomScripts.battleMessage((Player) playerMob.getTrainer().getSelf(), ChatColor.RED + "The opposing " + opponentMob.getName() + " fainted!");
                }
                if (opTrainer != null && opTrainer.getSelf() instanceof Player) {
                    randomScripts.battleMessage((Player) opTrainer.getSelf(), ChatColor.RED + opponentMob.getName() + " fainted!");
                }
            }
            if (onComplete != null) onComplete.run();
        });
    }

    private static void animateHealth(mobEnums mob, int start, int end, Runnable onComplete) {
        if (start <= end) {
            if (onComplete != null) onComplete.run();
            return;
        }

        AtomicInteger current = new AtomicInteger(start);
        AtomicInteger taskId = new AtomicInteger();
        taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(Torture.plugin, () -> {
            int val = current.decrementAndGet();
            if (mob.getSelf() != null) {
                mob.getSelf().setCustomName("§c" + val);
            }
            if (val <= end) {
                Bukkit.getScheduler().cancelTask(taskId.get());
                if (onComplete != null) onComplete.run();
            }
        }, 0L, 2L));
    }

    public static void processNext(CQueue<mobEnums> queue) {
        if (queue.isEmpty()) {
            return; // done processing all dead mobs
        }

        mobEnums mob = queue.poll();
        removeMob(mob, () -> processNext(queue));
    }

    public static void removeMob(mobEnums mob) {
        removeMob(mob, null);
    }


    public static void removeMob(mobEnums mob, Runnable onComplete) {
        if (!(mob.getSelf() instanceof Creature creature)) return;
        World world = creature.getWorld();
        double endX = creature.getLocation().getX();
        Vector vec = creature.getEyeLocation().getDirection().normalize().multiply(-2.9);
        Location loc = creature.getEyeLocation().add(vec);
        loc.setZ(creature.getZ());

        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(mob.getMobBall().getDisplayItem());
        stand.setInvisible(true);

        faceLoc(stand, creature);

        Location og = stand.getLocation();
        double[] x = {0};

        // Animate the armor stand towards the creature
        AtomicInteger taskId = new AtomicInteger();
        taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(Torture.plugin, () -> {
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
                Location headLoc = stand.getEyeLocation();
                stand.remove();
                
                world.getBlockAt(headLoc).setType(mob.getMobBall().getDisplayItem().getType());
                if (world.getBlockAt(headLoc).getState() instanceof ShulkerBox box) {
                    box.open();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Torture.plugin, box::close, 12L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Torture.plugin, () -> {
                        world.getBlockAt(headLoc).setType(Material.AIR);
                        creature.remove();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }, 24L);
                }
                Bukkit.getScheduler().cancelTask(taskId.get());
            }
        }, 0L, 1L));
    }

    public static void showTargetMenu(Player player, battleClass battle) {
        // Implementation placeholder - you might need to adjust this to match previous behavior
        // Based on the context, this should show a menu to select a target for a move.
        Inventory inv = Bukkit.createInventory(player, 9, Component.text("Select Target", NamedTextColor.GOLD));
        int slot = 0;
        for (mobEnums mob : battle.getMobs().values()) {
            if (mob != null && !mob.isDead()) {
                inv.setItem(slot++, randomScripts.createMobDisplayItem(mob));
            }
        }
        player.openInventory(inv);
    }

    /**
     *
     * @param trainer
     * @param type
     * @param boxNum
     * @param mobs Do it in order of enemy A, Ally B, Enemy B
     */
    public static void startBattle(humanClass.Trainer trainer, battleTypes type, int boxNum, mobEnums... mobs){
        SaveFile file = Torture.playerSaveFiles.get(trainer.getSelf().getUniqueId().toString());
        file.setPreBattleLocation(trainer.getSelf().getLocation());

        List<Player> contained = new ArrayList<>();
        contained.add((Player) trainer.getSelf());
        if(mobs[0].hasTrainer() && mobs[0].getTrainer().getSelf() instanceof Player){
            contained.add((Player) mobs[0].getTrainer().getSelf());
        }
        ArenaBox box = boxDex.returnBox(boxNum);
        List<Location> locs = box.getPlayerLocations();
        for(int i = 0; i < contained.size(); i++){
            Player player = contained.get(i);
            SaveFile f = getPlayerSaveFile(player);
            f.setInBattle(true);
            player.setFoodLevel(6);
            player.setWalkSpeed(0);
            player.teleport(locs.get(i));
            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
        }
        List<humanClass.Trainer> toPlace = new ArrayList<>();
        toPlace.add(trainer);
        HashSet<mobEnums> wild = new HashSet<>();
        HashSet<humanClass.Trainer> seen = new HashSet<>();
        for(mobEnums m : mobs){
            if(m.hasTrainer()){
                humanClass.Trainer t = m.getTrainer();
                toPlace.add(t);
                if(!seen.contains(t)){
                    t.setCurrentSelecting(m);
                }
                t.addMobInPlay(m);
                seen.add(t);
            }else{
                wild.add(m);
            }
        }
        List<Location> placeLocs = type.toInt() < TAGBATTLE.toInt() ? box.getSingleLocations() : box.getDoubleLocations();
        Map<Integer, Mob> placed = new HashMap<>();
        Map<humanClass.Trainer, Mob> thrower = new HashMap<>();
        List<Mob> justALittle = new ArrayList<>();
        for(int i = 0; i < toPlace.size(); i++){
            humanClass.Trainer forPlace = toPlace.get(i);
            forPlace.getSelf().sendMessage("Your I is: " + i);
            if(type.toInt() < TAGBATTLE.toInt()){
                if(i == 0){
                    forPlace.setDisplaySlot('a');
                }
                if(i == 1){
                    forPlace.setDisplaySlot('b');
                }
            }else{
                if(i == 0 || i == 1){
                    forPlace.setDisplaySlot('a');
                }else{
                    forPlace.setDisplaySlot('b');
                }
            }
            Location spawnLoc = placeLocs.get(i);
            Mob m = (Mob) world.spawnEntity(spawnLoc, EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM, custom -> {
                custom.setSilent(true);
                custom.setCustomNameVisible(false);
                ((Mob) custom).setAI(false);
                custom.setVisibleByDefault(false);
            });
            Bukkit.getPlayer("CrotaPlague").sendMessage("Spawned: " + m.getUniqueId());
            justALittle.add(m);
            placed.put(i + 1, m);

            LivingEntity targetPlayer = (LivingEntity) forPlace.getSelf();

// Try to reuse an already-resolved disguise from an active session first
            Bukkit.getPlayer("CrotaPlague").sendMessage("Target: " + targetPlayer.getUniqueId());
            if (disguiseManager.get(targetPlayer).isPresent()) {
                DisguiseAppearance appearance = disguiseManager.get(targetPlayer).get().appearance();
                if (appearance instanceof PlayerDisguise existingDisguise) {
                    // Already resolved, apply immediately
                    DisguiseSession session = existingDisguise.apply(m);
                    session.showDisguiseOnly();
                    thrower.put(forPlace, m);
                    continue; // or continue, depending on your surrounding loop context
                }
            }

// No existing disguise — resolve async then apply
            final Mob finalM = m;
            Disguises.playerAsync(targetPlayer.getName()).thenAccept(resolved ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        DisguiseSession session = resolved.apply(finalM);
                        session.showDisguiseOnly();
                        thrower.put(forPlace, finalM);
                    })
            );
        }
        for(Map.Entry<Integer, Mob> entry : placed.entrySet()){
            Integer i = entry.getKey();
            if(i == 1 || i == 3){
                faceLoc(entry.getValue(), placed.get(i+1));
            }else{
                faceLoc(entry.getValue(), placed.get(i-1));
            }
        }
        mobEnums first = trainer.getFirstMobAlive();
        trainer.addMobInPlay(first);
        trainer.setCurrentSelecting(first);
        if(!(type.toInt() < DOUBLEPVE.toInt())){
            if(trainer.getSecondMobAlive() != null)
                trainer.addMobInPlay(trainer.getSecondMobAlive());
        }
        final battleClass battle = new battleClass(toPlace, type);
        for(mobEnums m : mobs){
            if(!m.hasTrainer() || !(m.getTrainer().getSelf() instanceof Player)){
                m.setNextMove(NextMove.fromMove(m.getRandomMove(), m));
                battle.setMobStatus(m);
            }
        }
        for(Mob m : justALittle){
            battle.viewEntity(m);
        }
        battle.defaultMobStatus(wild);
        int i = 0;
        List<Location> hordeLocs = box.getHordeLocations();
        for(mobEnums m : wild){
            Mob mo = (Mob) world.spawnEntity(hordeLocs.get(i), m.getMojangMobType(), CreatureSpawnEvent.SpawnReason.CUSTOM, custom -> {custom.setSilent(true); custom.setCustomNameVisible(true); ((Mob) custom).setAI(false); custom.setVisibleByDefault(false);});
            mo.customName(Component.text(m.getCurrentHp(), NamedTextColor.RED));
            battle.viewEntity(mo);
            ++i;
        }

        List<mobEnums> mos = new ArrayList<>();
        mos.addAll(trainer.getCurrentMobs());
        mos.addAll(List.of(mobs));
        mobEnums moo = null;
        int c = 1;
        for(mobEnums m : mos){
            battle.setMobInPosition(c, m);
            m.setBattleSpot(c++);
            if(moo == null){
                moo = m;
                continue;
            }
            m.setOpponentMob(moo);
            moo.setOpponentMob(m);
            moo=null;
        }
        battle.setBox(box);
        battles.add(battle);
        int size = thrower.size();
        int count = 0;
        AtomicInteger ia = new AtomicInteger(1);
        for(Map.Entry<humanClass.Trainer, Mob> entry : thrower.entrySet()){
            count++;
            final boolean isLast = (count == size);
            final Location l = entry.getValue().getLocation();
            final float pitch = entry.getValue().getPitch(), yaw = entry.getValue().getYaw();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for(humanClass.Trainer t : toPlace){
                    if(t.getSelf() instanceof Player){
                        Player player = (Player) t.getSelf();
                        if(!player.equals(entry.getKey().getSelf())){
                            battleMessage(player, entry.getValue().getName() + " would like to battle!", true);
                        }
                    }
                }
            }, ia.get() - 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                entry.getKey().claimMobs();
                final Mob m = entry.getValue();
                m.setAI(true);
                Location path = m.getLocation().add(m.getLocation().getDirection().normalize().multiply(2));
                NamespacedKey key = new NamespacedKey(plugin, "AnimationMob");
                m.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, new int[]{path.getBlockX(), path.getBlockY(), path.getBlockZ()});
                m.getPathfinder().moveTo(path, 0.35);
                final Vector velocity = m.getLocation().getDirection().multiply(-0.18);

                final int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {m.setRotation(yaw, pitch); m.setVelocity(velocity);}, 0L, 1L);
                final Location atStart = entry.getValue().getEyeLocation();
                final Mob toRemove = entry.getValue();
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    m.swingMainHand();
                    mobEnums sendOut = entry.getKey().getFirstMobAlive();
                    for(humanClass.Trainer t : toPlace){
                        if(t.getSelf() instanceof Player){
                            Player player = (Player) t.getSelf();
                            if(!player.equals(entry.getKey().getSelf())){
                                battleMessage(player, entry.getValue().getName() + " sent out " + sendOut.getName() + "!", true);
                            }else{
                                battleMessage(player, "Go! " + sendOut.getNickname() + "!", true);
                            }
                        }
                    }
                    randomScripts.sendOutMob(sendOut, l, atStart, () -> {Bukkit.getScheduler().cancelTask(taskId);
                        toRemove.remove();
                        if(isLast){
                            for(humanClass.Trainer t : battle.getCompetitors()){
                                if(t.getSelf() instanceof Player){
                                    t.displayOptions(battle);
                                    SaveFile f = getPlayerSaveFile((Player) t.getSelf());
                                    for(ArmorStand s : f.getArmorStands()){
                                        ((Player) t.getSelf()).showEntity(plugin, s);
                                    }
                                }
                            }
                        }
                    });
                }, 15L);

            }, 38L * ia.getAndIncrement());
        }
    }
}
