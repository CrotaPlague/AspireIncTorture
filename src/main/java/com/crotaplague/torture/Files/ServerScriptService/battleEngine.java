package com.crotaplague.torture.Files.ServerScriptService;


import com.crotaplague.torture.Files.ServerStorage.SaveFile;
import com.crotaplague.torture.Files.ServerStorage.battleClass;
import com.crotaplague.torture.Files.ServerStorage.battleTypes;
import com.crotaplague.torture.Files.ServerStorage.boxs.ArenaBox;
import com.crotaplague.torture.Files.ServerStorage.boxs.boxDex;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.moveClass;
import com.crotaplague.torture.Files.ServerStorage.specialConditions.specialConditions;
import com.crotaplague.torture.Torture;
import com.destroystokyo.paper.entity.Pathfinder;


import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.*;

import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

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

    /*public static void startPveBattle(Player player, humanClass opponent, LivingEntity attackerEnemy, battleTypes type){ // Work?
        if(!(Torture.data.getConfig().contains("Player " + player.getUniqueId() + " hotbar"))){
            Torture.playerSpecifics.put(player.getUniqueId() + " preBattleLoc", player.getLocation());

            List<mobEnums> playerHotBar = new ArrayList<>();
            Map<Integer, mobEnums> map;

            for(int i = 0; i<7;i++) {
                map = Torture.playerSaveFiles.get(player.getUniqueId().toString()).getPlayerMobs();

                if (map != null){
                    playerHotBar.add(map.get(i));
                }
            }

            Torture.playerSpecifics.put(player.getUniqueId() + " currentMob", playerHotBar.get(0));
            Integer boxNum = opponent.getBoxNum();
            if(boxNum == null || boxNum == -1){
                boxNum = getBoxNum(Torture.playerSaveFiles.get(player.getUniqueId() + "").getRouteNum());
            }
            List<Location> locationList = boxDex.returnBox(boxNum);
            player.setFoodLevel(6);
            player.setWalkSpeed(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200, false, false));
            player.teleport(locationList.get(0));
            int trinhex = 0;
            for (mobEnums specificMob : opponent.getMobs()){
                opponent.setMob(trinhex, Torture.theGreatEqualizer(specificMob));
                trinhex++;
            }
            mobEnums opponentMobZero = opponent.getMobs().get(0);

            Mob opponentMob = (Mob) player.getWorld().spawnEntity(locationList.get(1).add(-1, 0, -3), opponentMobZero.getType());
            Mob playerMob = (Mob) player.getWorld().spawnEntity(locationList.get(0).add(1, 0, 3), playerHotBar.get(0).getType());

            faceLoc(playerMob, opponentMob);
            Vector vec;
            Location loc;
            playerMob.setAI(false);
            playerMob.setSilent(true);
            playerMob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100,true, false));
            playerMob.setCustomName("§c" + playerHotBar.get(0).getStats()[5]);
            playerMob.setCustomNameVisible(true);

            vec = opponentMob.getEyeLocation().toVector().subtract(playerMob.getEyeLocation().toVector());
            loc = opponentMob.getEyeLocation().setDirection(vec.multiply(-1));
            loc.setX(opponentMob.getLocation().getX());
            loc.setY(opponentMob.getLocation().getY());
            loc.setZ(opponentMob.getLocation().getZ());
            opponentMob.teleport(loc);
            opponentMob.setAI(false);
            opponentMob.setSilent(true);
            opponentMob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100,true, false));
            opponentMob.setCustomName("§c" + opponentMobZero.getStats()[5]);
            opponentMob.setCustomNameVisible(true);


            Torture.playerSpecifics.put(player.getUniqueId() + " player hotbar", playerHotBar);
            Torture.playerSpecifics.put(player.getUniqueId() + " hideForAllElse", player);

            Torture.playerSpecifics.put(player.getUniqueId() + " opponentMobs", opponent.getMobs());

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 10, true, false, false));


            humanClass.Trainer human = new humanClass.Trainer(player, playerHotBar, player.getName());
            mobEnums keptMob = playerHotBar.get(0);
            if(type == battleTypes.SOLOPVE || type == battleTypes.SOLOPVP){
                keptMob = playerHotBar.get(0);
                for(int i = 0; i < keptMob.getMoves().size()-1; i++){
                    moveClass move = keptMob.getMove(i);
                    move.setMobTarget(opponentMobZero);
                    keptMob.setMove(move, i);
                }
            }
            keptMob.setBattleSpot(1);
            keptMob.setSelf(playerMob);
            keptMob.setSelfUUID(playerMob.getUniqueId());
            keptMob.setTrainer(human);
            human.setPrimaryMob(keptMob);
            playerHotBar.set(0, keptMob);
            Torture.playerSaveFiles.get(player.getUniqueId() + "").setInBattle(true);

            opponent.setMob(0, opponentMobZero.setSelf(opponentMob));
            humanClass.Trainer opTrainer = new humanClass.Trainer(attackerEnemy, opponent.getMobs(), opponent.getName());
            List<mobEnums> mobs = human.getMobs();
            List<mobEnums> ms = opponent.getMobs();
            if(type == battleTypes.DOUBLEPVE){
                opTrainer.setMobsInPlay(ms.size() == 1 ? ms : ms.subList(0, 1));
                human.setMobsInPlay(mobs.size() == 1 ? mobs : mobs.subList(0, 1));
            }else{
                opTrainer.setMobsInPlay(ms.subList(0, 1));
                human.setMobsInPlay(mobs.subList(0, 1));
            }
            opTrainer.setCurrentSelecting(ms.get(0));
            human.setCurrentSelecting(mobs.get(0));

            opTrainer.setFalseLOc(locationList.get(1));
            opponentMobZero.setTrainer(opTrainer);
            opponentMobZero.setBattleSpot(2);
            opponentMobZero.setOpponentMob(human.getPrimaryMob());
            opTrainer.setPrimaryMob(opponentMobZero);
            human.getPrimaryMob().setOpponentMob(opponentMobZero);
            opponentMobZero.setNextMove(opponentMobZero.getRandomMove());
            opTrainer.setCurrentMobEnt(opponentMob);
            opTrainer.setStupid(opponentMob.getUniqueId());
            human.setCurrentMobEnt(playerMob);
            List<humanClass.Trainer> comps = new ArrayList<>();
            comps.add(opTrainer);
            comps.add(human);
            battleClass battle = new battleClass(comps, type);
            battle.getMobs().put(1, keptMob);
            battle.getMobs().put(2, opponentMobZero);
            battle.setMobStatus(opTrainer.getCurrentSelecting());
            battles.add(battle);
            human.displayOptions(battle);
        }else{
            Torture.plugin.getLogger().log(Level.SEVERE, "[Torture] Player " + player.getDisplayName() + " has no mob to battle with");
            Torture.hook.setContent(player.getName() + " went into a PVE battle with no mob, they were at " + player.getLocation());
            player.teleport(player.getWorld().getSpawnLocation());
        }

    }
    public static void pvpStart(List<Player> players, @NotNull battleTypes type){

        int spot = 0;
        if(type.equals(battleTypes.SOLOPVP)){
            Map<Integer, mobEnums> theMap = new HashMap<>();
            List<humanClass.Trainer> list = new ArrayList<>();
            int counter = 0;
            List<Location> locList = boxDex.returnBox(1);
            Entity firstInstance = null;
            Entity secondInstance = null;
            for(Player player : players){
                Torture.playerSaveFiles.get(player.getUniqueId() + "").setInBattle(true);
                Torture.playerSpecifics.put(player.getUniqueId() + " preBattleLoc", player.getLocation());
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 10, true, false, false));
                player.setFoodLevel(6);
                player.setWalkSpeed(0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200, false, false));
                List<mobEnums> mobs = new ArrayList<>(Torture.playerSaveFiles.get(player.getUniqueId() + "").getPlayerMobs().values());
                humanClass.Trainer trainer = new humanClass.Trainer(player, mobs, player.getName());
                mobs.get(0).setBattleSpot(++spot);
                theMap.put(spot, mobs.get(0));

                player.teleport(locList.get(counter));

                Location loc = new Location(player.getWorld(), -1, 0, -3);
                Location loc2 = new Location(player.getWorld(), 1, 0, 3);
                LivingEntity entity = null;
                if(counter == 0){
                    firstInstance = (LivingEntity) player.getWorld().spawnEntity(locList.get(counter).add(loc2), mobs.get(0).getType());
                }else{
                    secondInstance = (LivingEntity) player.getWorld().spawnEntity(locList.get(counter).add(loc), mobs.get(0).getType());
                }

                list.add(trainer);
                counter++;
            }
            counter = 0;
            for(humanClass.Trainer train : new ArrayList<>(list)){
                humanClass.Trainer copiedTrainer = train;
                LivingEntity entity = null;
                mobEnums mob = copiedTrainer.getMobs().get(0);
                if(counter == 0){
                    entity = (LivingEntity) copiedTrainer.getSelf().getWorld().spawnEntity(faceLoc((Creature) firstInstance, (Creature) secondInstance, true), firstInstance.getType(), CreatureSpawnEvent.SpawnReason.CUSTOM, zombie -> {zombie.setSilent(true); zombie.setCustomNameVisible(true);
                        zombie.setCustomName(ChatColor.RED + "" + mob.getCurrentHp()); zombie.setInvulnerable(true); ((LivingEntity) zombie).setAI(false);});
                }else{
                    entity = (LivingEntity) copiedTrainer.getSelf().getWorld().spawnEntity(faceLoc((Creature) secondInstance, (Creature) firstInstance, true), secondInstance.getType(), CreatureSpawnEvent.SpawnReason.CUSTOM,  zombie -> {zombie.setSilent(true); zombie.setCustomNameVisible(true);
                        zombie.setCustomName(ChatColor.RED + "" + mob.getCurrentHp()); zombie.setInvulnerable(true); ((LivingEntity) zombie).setAI(false);});

                }
                copiedTrainer.setCurrentMobEnt(entity);
                mob.setTrainer(copiedTrainer);
                mob.setSelf(entity);

                copiedTrainer.setPrimaryMob(mob);
                list.remove(train);
                list.add(copiedTrainer);
                counter++;
            }
            firstInstance.remove();
            secondInstance.remove();
            Creature creatureMob = (Creature) list.get(0).getPrimaryMob().getSelf();
            Location tempLoc = creatureMob.getLocation();

            Creature opponentCreature = (Creature) list.get(1).getPrimaryMob().getSelf();

            faceLoc(creatureMob, opponentCreature);
            faceLoc(opponentCreature, creatureMob);

            List<humanClass.Trainer> realList = new ArrayList<>(list);

            mobEnums mob0 = list.get(0).getPrimaryMob();
            mobEnums mob1 = list.get(1).getPrimaryMob();

            humanClass.Trainer train0 = list.get(0);
            humanClass.Trainer train1 = list.get(1);
            mob0.setOpponentMob(mob1);
            mob1.setOpponentMob(mob0);
            mob0.setTrainer(train0);
            mob1.setTrainer(train1);
            train0.setPrimaryMob(mob0);
            train1.setPrimaryMob(mob1);
            realList.set(0, train0);
            realList.set(1, train1);
            battleClass battle = new battleClass(realList, type);
            battle.setMobs(theMap);
            battles.add(battle);
            battle.resetStatuses();
            for(humanClass.Trainer trainer : list){if(trainer.getSelf() instanceof Player){battle.playerCount++;} trainer.getSelf().teleport(trainer.getLocation()); trainer.displayOptions(battle);}
        }
    }*/

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

    public static void makeMove(moveClass move, battleClass battle, mobEnums mob) {
        if(battle.getBattleType() == battleTypes.SOLOPVE || battle.getBattleType() == battleTypes.SOLOPVP){
            int defenderHealth = entMove(mob, move.getMobTarget(), move, battle);

            boolean hasMoreMob = true;
            if(defenderHealth <= 0){
                hasMoreMob = false;
                if(move.getMobTarget().hasTrainer()) {
                    humanClass.Trainer defenderTrainer = move.getMobTarget().getTrainer();
                    for (mobEnums mob1 : defenderTrainer.getMobs()) {
                        if (mob1 != null) {
                            if (mob1.getCurrentHp() > 0) {
                                hasMoreMob = true;
                                break;
                            }
                        }

                    }
                }else{
                    for(Map.Entry<Integer, mobEnums> entry : battle.getMobs().entrySet()){
                        if(!entry.getValue().hasTrainer() && entry.getValue().getCurrentHp() > 0){
                            hasMoreMob = true; break;
                        }
                    }
                }
            }
            if(hasMoreMob){
                if(mob.getCondition()!=null) {
                    if (mob.getCondition().getCondition().getEffect() == specialConditions.effect.DAMAGE) {
                        if (randomScripts.getRandomNumber(1, 100) >= mob.getCondition().getCondition().getPercent()) {
                            int fullHp = mob.getStats()[moveClass.Stat.HIT_POINTS.getValue()];
                            int newHp = mob.getCurrentHp() - Math.round(fullHp * mob.getCondition().getCondition().getDamage());
                            mob.setCurrentHp(newHp);
                        }
                    }
                }
            }else{
                if(mob.hasTrainer() && mob.getTrainer().getSelf() instanceof Player){
                    humanClass.Trainer attackerTrainer = mob.getTrainer();
                    Thread defence = new Thread(){
                        public void run(){
                            if(move.getMobTarget().getTrainer().getSelf() instanceof Player){
                                randomScripts.actionBarMessage((Player) attackerTrainer.getSelf(), attackerTrainer.getName() + " won the match!", SaveFile.MessagePurpose.BATTLE_END);
                            }else{
                                if(move.getMobTarget().hasTrainer()) {
                                    humanClass.Trainer defender = move.getMobTarget().getTrainer();
                                    randomScripts.actionBarMessage((Player) attackerTrainer.getSelf(), attackerTrainer.getName() + " received " + defender.getCashReward() + " dollars for winning!", SaveFile.MessagePurpose.BATTLE_END);
                                }
                            }
                        }
                    };
                    defence.run();

                    SaveFile file = Torture.playerSaveFiles.get(attackerTrainer.getSelf().getUniqueId() + "");
                    if(move.getMobTarget().hasTrainer() && !(move.getMobTarget().getTrainer().getSelf() instanceof Player)){
                        humanClass.Trainer defender = move.getMobTarget().getTrainer();
                        file.addDefeatedTrainer(defender.getDexNum());
                        file.addCash(defender.getCashReward());
                    }

                }
                if(move.getMobTarget().hasTrainer() && move.getMobTarget().getTrainer().getSelf() instanceof Player){
                    humanClass.Trainer defender = move.getMobTarget().getTrainer();
                    Thread attack = new Thread(){
                        public void run(){
                            if(defender.getSelf() instanceof Player){
                                randomScripts.battleMessage(defender.getSelf(), "You are out of mobs and have lost the battle.");
                            }
                        }
                    };
                    attack.start();
                }
                battle.setBattleOver();
            }
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
            LivingEntity liv = creature;
            creature.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            creature.setInvulnerable(true);

            walker.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Walking to");
            creature.setCustomNameVisible(false);
            creature.setAI(true);
            Torture.playerSpecifics.put(player.getUniqueId() + " walkingEntityForPVE", walker);
            Torture.playerSpecifics.put(player.getUniqueId() + " hideForAllElse", walker);
            key = new NamespacedKey(Torture.getInstance(), "humanDexNum");

            walker.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, attackerEnemy.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));

            if(attackerEnemy.getUniqueId().equals(humans.getUniqueId())) {player.sendMessage(Component.text("thought so"));}
            Disguise d = DisguiseAPI.getDisguise(humans);
            FlagWatcher watcher = d.getWatcher();
            watcher.setHelmet(new ItemStack(Material.AIR));
            DisguiseAPI.disguiseEntity(walker, d);
            player.showEntity(plugin, walker);
            Location humanLoc = humans.getLocation();
            Location playerLoc = player.getLocation();
            Vector dir = playerLoc.toVector().subtract(humanLoc.toVector());
            dir = dir.normalize();
            double offset = 0.6;
            Vector offsetVec = dir.multiply(offset);
            Location targetLoc = humanLoc.clone().add(offsetVec);

            key = new NamespacedKey(Torture.plugin, "AnimationMob");
            walker.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, new int[]{targetLoc.blockX(), targetLoc.blockZ()});

            MobHandler.moveTo(walker, targetLoc);

        }
        return null;
    }
    private static int entMove(mobEnums playerMob, mobEnums opponentMob, moveClass move, battleClass battle) {
        Creature opponentCreature = (Creature) opponentMob.getSelf();

        specialConditions playerMobCondition = playerMob.getCondition();
        if(playerMobCondition != null) {
            if (playerMobCondition.getCondition().getEffect() == specialConditions.effect.STUN) {
                if (randomScripts.getRandomNumber(1, 100) >= playerMobCondition.getCondition().getPercent()) { //If the player mob is stunned
                    return -1;
                }
            }
        }

        String name = opponentMob.getSelf().getCustomName();

        name = name.substring(2);

        int health = Integer.parseInt(name);
        float defence = 0;
        int attack = move.getDamage();

        moveClass.Stat s = null;
        if(move.getMoveType().equalsIgnoreCase("special")){
            s = moveClass.Stat.RANGED_ATTACK;
        }else if(move.getMoveType().equalsIgnoreCase("physical")){
            s = moveClass.Stat.ATTACK;
        }else if(move.getMoveType().equalsIgnoreCase("targetStat")){
            s = null;
        }
        float attackerAttackStat = 0;
        if(s != null) {
            attackerAttackStat = opponentMob.calculateStat(s);
        }

        if(move.getMoveType().equalsIgnoreCase("special")){
            defence = opponentMob.calculateStat(moveClass.Stat.RANGED_DEFENCE);
        }else if(move.getMoveType().equalsIgnoreCase("physical")){
            defence = opponentMob.calculateStat(moveClass.Stat.DEFENCE);
        }

        if(move.hasEffect()){ // If the move has an EFFECT i.e. lower a stat

            int chance = randomScripts.getRandomNumber(1, 100);
            if(chance <= move.getPercent()){
                mobEnums mobToEffect = null;
                switch(move.getEffectTarget()){
                    case SELF:
                        mobToEffect = playerMob;
                        break;
                    case ENEMY:
                        mobToEffect = opponentMob;
                        break;
                }
                mobToEffect.changeStage(move.getEffectStat(), move.getEffectAmount());
            }
        }

        if(move.getConditions().size() > 0){
            List<specialConditions> conditionsList = move.getConditions();
            int totalChance = 0;
            for(specialConditions condition : conditionsList){
                totalChance = totalChance + (condition.getCondition().getPercent()/conditionsList.size());
            }
            int random = randomScripts.getRandomNumber(1, 100);
            List<specialConditions> result = conditionsList.stream().sorted((o1, o2)-> {if(o1.getCondition().getPercent() > o2.getCondition().getPercent()) return 1; return -1;}
                    ).
                    collect(Collectors.toList());
            if(random <= totalChance){
                specialConditions previous = result.get(0);
                for(specialConditions condition : result){
                    if(!((condition.getCondition().getPercent()/conditionsList.size()) >= random)){
                        opponentMob.setCondition(previous);
                    }
                    previous = condition;
                }
            }

        }

        final int originalHealth = health;
        if(move.getMoveType().equals("physical") || move.getMoveType().equals("special")){
            int damage = Math.round((float)(((((playerMob.getLevel()*1.25)/3.5)+2)*(attackerAttackStat/defence)*attack))/30);

            health = health - damage;

            opponentMob.setCurrentHp(health);
        }

        humanClass.Trainer opTrainer = opponentMob.hasTrainer() ? opponentMob.getTrainer() : null;
        final Player[] player = {null};
        int finalHealth = health;

        Thread defender = new Thread(){
            public void run() {
                if (playerMob.hasTrainer() && playerMob.getTrainer().getSelf() instanceof Player) {
                    player[0] = (Player) playerMob.getTrainer().getSelf();
                    randomScripts.battleMessage(player[0], ChatColor.RED + playerMob.getName() + " used " + move.getMoveName());
                    SaveFile file = Torture.playerSaveFiles.get(player[0].getUniqueId() + "");
                    Torture.playerSaveFiles.put(player[0].getUniqueId() + "", file);
                }
            }
        };
        Thread opponentMessage = new Thread(){
          public void run(){
              if(opTrainer != null && opTrainer.getSelf() instanceof Player){
                  player[0] = (Player) opTrainer.getSelf();
                 randomScripts.battleMessage(player[0], ChatColor.RED + "The opposing " + playerMob.getName() + " used " + move.getMoveName());
                  SaveFile file = Torture.playerSaveFiles.get(player[0].getUniqueId() + "");
                  Torture.playerSaveFiles.put(player[0].getUniqueId() + "", file);
              }
          }
        };
        defender.start();
        opponentMessage.start();
        // any attack animations right here as a new thread
        for(int health1 = originalHealth; health1 >= finalHealth; health1--){
            if(health1 < 0){ break;}
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LivingEntity ent = (LivingEntity) move.getMobTarget().getSelf();
            ent.setCustomName("§c" + health1);
        }
        if(finalHealth <= 0){
            move.getMobTarget().getSelf().playEffect(EntityEffect.ENTITY_DEATH);
            battle.entDead(move.getMobTarget());
        }


        try {
            defender.join();
            opponentMessage.join();
            // join the animation as well
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(player[0] != null){
            if(move.hasEffect()){

            }
        }



        if(health < 1){ // THOMAS: this code is very lame and should be replaced ASAP
            Bukkit.getScheduler().runTask(Torture.plugin, new Runnable() {
                @Override
                public void run() {
                    Sound s = opponentCreature.getDeathSound() != null ? opponentCreature.getDeathSound() : Sound.ENTITY_PLAYER_HURT;
                    opponentCreature.getWorld().playSound(opponentCreature.getLocation(), s, Float.MAX_VALUE, 0.5f);
                }
            });
            if(playerMob.hasTrainer() && playerMob.getTrainer() instanceof Player){
                randomScripts.battleMessage((Player) playerMob.getTrainer().getSelf(), ChatColor.RED + "The opposing " + opponentMob.getName() + " fainted!");
            }
            if(opTrainer != null && opTrainer.getSelf() instanceof Player){
                randomScripts.battleMessage((Player) opTrainer.getSelf(), ChatColor.RED + opponentMob.getName() + " fainted!");
            }
        }

        return health;
    }

    public static Comparator<mobEnums> valueComparator = new Comparator<mobEnums>(){

        @Override
        public int compare(mobEnums o1, mobEnums o2) {
            int v1 = o1.getStats()[4];
            int v2 = o2.getStats()[4];
            if(!(o1.getNextMove() instanceof moveClass)){
                if(o1.getNextMove() instanceof mobEnums){
                    v1 = v1 + 9999999;
                }
                if(o1.getNextMove() instanceof ItemClass){
                    v1 = v1 + 999999;
                }
            }else{
                if(((moveClass) o1.getNextMove()).isFirst()){
                    v1 = v1 + 99999;
                }
            }
            if(!(o2.getNextMove() instanceof moveClass)){
                if(o2.getNextMove() instanceof mobEnums){
                    v2 = v2 + 9999999;
                }
                if(o2.getNextMove() instanceof ItemClass){
                    v2 = v2 + 999999;
                }
            }else{
                if(((moveClass) o2.getNextMove()).isFirst()){
                    v2 = v2 + 99999;
                }
            }
            return Integer.compare(v2, v1);
        }
    };
    public static void showTargetMenu(Player player, battleClass battle){
        Inventory inventory = Bukkit.createInventory(player, 27, Component.text("Choose Target", NamedTextColor.GRAY));

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
                custom.setSilent(true); custom.setCustomNameVisible(true); ((Mob) custom).setAI(false); custom.setVisibleByDefault(false);
            });
            justALittle.add(m);
            placed.put(i+1, m);

            LivingEntity player = (LivingEntity) forPlace.getSelf();
            PlayerDisguise disguise = null;
            if(DisguiseAPI.isDisguised(player)){
                disguise = (PlayerDisguise) DisguiseAPI.getDisguise(player);
            }else{
                if(player instanceof Player)
                    disguise = new PlayerDisguise((Player) player);
            }
            if(disguise != null){
                disguise.setNameVisible(false);
                DisguiseAPI.disguiseEntity(m, disguise);
            }
            thrower.put(forPlace, m);
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
                m.setNextMove(m.getRandomMove());
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
