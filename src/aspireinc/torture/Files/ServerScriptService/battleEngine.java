package aspireinc.torture.Files.ServerScriptService;
import aspireinc.torture.Files.ServerStorage.ItemManager;
import aspireinc.torture.Files.ServerStorage.battleClass;
import aspireinc.torture.Files.ServerStorage.boxReturn;
import aspireinc.torture.Files.ServerStorage.boxs.boxDex;
import aspireinc.torture.Files.ServerStorage.humans.humanClass;
import aspireinc.torture.Files.ServerStorage.mobs.mobEnums;

import aspireinc.torture.Files.ServerStorage.moveClass;
import aspireinc.torture.Files.ServerStorage.specialConditions.specialConditions;
import aspireinc.torture.Torture;


import net.minecraft.world.entity.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class battleEngine {

    public static void startPveBattle(Player player, humanClass opponent, Entity attackerEnemy){
        if(!(Torture.data.getConfig().contains("Player " + player.getUniqueId() + " hotbar"))){

            NamespacedKey entityIdKey = new NamespacedKey(Torture.getInstance(), "humanDexNum");





            List<mobEnums> playerHotBar = new ArrayList<>();
            Map<String, Object> map = null;

            for(int i = 0; i<7;i++) {
                map = null;
                mobEnums thisMob;
                ConfigurationSection section = Torture.data.getConfig().getConfigurationSection("player " + player.getUniqueId() + " hotbar " + i);
                if (section == null) {

                } else {
                    map = section.getValues(false);
                }
                if (map != null){
                    thisMob = mobEnums.deserialize(map);
                    playerHotBar.add(thisMob);
                }
            }


            Torture.playerSpecifics.put(player.getUniqueId() + " currentMob", playerHotBar.get(0));
            Integer boxNum = opponent.getBoxNum();
            if(boxNum == null){
                boxNum = boxReturn.getBoxNum((Integer) Torture.data.getConfig().get(player.getUniqueId() + " route", PersistentDataType.INTEGER));
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
            Entity opponentMob = player.getWorld().spawnEntity(locationList.get(1).add(-1, 0, -3), opponentMobZero.getType());
            Creature opponentCreature = (Creature) opponentMob;
            Entity playerMob = player.getWorld().spawnEntity(locationList.get(0).add(1, 0, 3), playerHotBar.get(0).getType());
            Creature creatureMob = (Creature) playerMob;

            Location tempLoc = creatureMob.getLocation();
            Vector vec = creatureMob.getEyeLocation().toVector().subtract(opponentCreature.getEyeLocation().toVector());
            Location loc = creatureMob.getEyeLocation().setDirection(vec.multiply(-1));
            loc.setX(tempLoc.getX());
            loc.setY(tempLoc.getY());
            loc.setZ(tempLoc.getZ());
            creatureMob.teleport(loc);
            creatureMob.setAI(false);
            creatureMob.setSilent(true);
            creatureMob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100,true, false));
            creatureMob.setCustomName("§c" + playerHotBar.get(0).getStats().get(5));
            creatureMob.setCustomNameVisible(true);

            vec = opponentCreature.getEyeLocation().toVector().subtract(creatureMob.getEyeLocation().toVector());
            loc = opponentCreature.getEyeLocation().setDirection(vec.multiply(-1));
            loc.setX(opponentCreature.getLocation().getX());
            loc.setY(opponentCreature.getLocation().getY());
            loc.setZ(opponentCreature.getLocation().getZ());
            opponentCreature.teleport(loc);
            opponentCreature.setAI(false);
            opponentCreature.setSilent(true);
            opponentCreature.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100,true, false));
            opponentCreature.setCustomName("§c" + opponentMobZero.getStats().get(5));
            opponentCreature.setCustomNameVisible(true);


            player.getInventory().setItem(2, ItemManager.showMobMoves);
            player.getInventory().setItem(3, ItemManager.showSwap);
            player.getInventory().setItem(4, ItemManager.showBag);
            player.getInventory().setItem(5, ItemManager.showRun);
            Torture.playerSpecifics.put(player.getUniqueId() + " player hotbar", playerHotBar);
            Entity playerEnt = player;
            Torture.playerSpecifics.put(player.getUniqueId() + " hideForAllElse", playerEnt);

            Torture.playerSpecifics.put(player.getUniqueId() + " opponentMobs", opponent.getMobs());

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 10, true, false, false));
            Map<Entity, Entity> tempEntityMap = new HashMap<>();
            tempEntityMap.put(player, creatureMob);
            tempEntityMap.put(opponentMob, opponentCreature);

            Map<Entity, mobEnums> entitymobEnumsMap = new HashMap<>();
            entitymobEnumsMap.put(player, playerHotBar.get(0));
            entitymobEnumsMap.put(attackerEnemy, opponentMobZero);

            Map<Player, Boolean> playerBooleanMap = new HashMap<>();
            playerBooleanMap.put(player, false);
            battleClass battle = new battleClass(playerBooleanMap, entitymobEnumsMap, tempEntityMap, battleClass.battleTypes.SOLOPVE);
            Torture.battles.add(battle);

        }else{
            Torture.plugin.getLogger().log(Level.SEVERE, "[Torture] Player " + player.getDisplayName() + " has no mob to battle with");
            player.teleport(player.getWorld().getSpawnLocation());
        }

    }
    public static void makeMove(moveClass move, Player player){
        Entity playerEntity = null;
        Creature opponentCreature = null;
        battleClass battle = Torture.battles.stream().filter(mover -> mover.getCompetitors().containsKey(player)).collect(Collectors.toList()).get(0);
        for(Map.Entry<Entity, Entity> awdhawh: battle.getEntities().entrySet()){
            if(awdhawh.getKey() == player){
                playerEntity = awdhawh.getValue();
            }
        }
        mobEnums playerMob = battle.getMobs().get(player);
        if(battle.getBattleType() == battleClass.battleTypes.SOLOPVE || battle.getBattleType() == battleClass.battleTypes.SOLOPVP){
            for(Map.Entry<Entity, Entity> entry : battle.getEntities().entrySet()){ //Get the
                battle.getMobs().entrySet();
            }
            if(move.getTarget() != null)
            for(Map.Entry<Entity, mobEnums> entry : battle.getMobs().entrySet()){
                if(entry.getValue() == move.getMobTarget()){ //Is going to get the opponent in game entity
                    opponentCreature = (Creature) entry.getKey();
                }
            }
            List<Map.Entry<Entity, mobEnums>> entryList = new ArrayList<>(battle.getMobs().entrySet());
            Collections.sort(entryList, valueComparator);
            for(Map.Entry<Entity, mobEnums> map : entryList){
                Entity thisEnt = map.getKey();
                mobEnums thisMob = map.getValue();
                mobEnums thisTarget = move.getMobTarget();
                entMove(opponentCreature, playerEntity, playerMob, move.getMobTarget(), move, battle);
            }
            for(Map.Entry<Entity, mobEnums> map : entryList){
                Entity thisEnt = map.getKey();
                mobEnums thisMob = map.getValue();
                if(thisMob.getCondition().getCondition().getEffect() == specialConditions.effect.DAMAGE){
                    if(randomScripts.getRandomNumber(1, 100) >= thisMob.getCondition().getCondition().getPercent()){
                        int fullHp = thisMob.getStats().get(moveClass.stat.HIT_POINTS.getValue());
                        int newHp = thisMob.getCurrentHp() - Math.round(fullHp*thisMob.getCondition().getCondition().getDamage());
                        thisMob.setCurrentHp(newHp);
                        battle.getMobs().put(thisEnt, thisMob);
                    }
                }
            }

        }

    }
    public static void makeEntWalk(Entity attackerEnemy, Player player, Entity humans){
        player.setFoodLevel(6);
        player.setWalkSpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200, false, false));
        NamespacedKey key = new NamespacedKey(Torture.getInstance(), "hiddenName");
        if(attackerEnemy != null) {
            Entity entity = player.getWorld().spawnEntity(attackerEnemy.getLocation(), EntityType.VILLAGER);
            Villager villager = (Villager) entity;
            villager.setAdult();


            Creature creature = (Creature) entity;
            creature.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            creature.setInvulnerable(true);

            entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Walking to");
            creature.setCustomNameVisible(false);
            creature.setAI(true);
            Torture.playerSpecifics.put(player.getUniqueId() + " walkingEntityForPVE", entity);
            Torture.playerSpecifics.put(player.getUniqueId() + " hideForAllElse", entity);
            key = new NamespacedKey(Torture.getInstance(), "humanDexNum");

            entity.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, attackerEnemy.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));

            Vector vec = humans.getLocation().toVector();
            vec = player.getLocation().toVector().subtract(vec);

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
            ((EntityInsentient) ((CraftEntity) entity).getHandle()).getNavigation().a(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), f);


        }
    }
    private static int entMove(Creature opponentCreature, Entity playerEntity, mobEnums playerMob, mobEnums opponentMob, moveClass move, battleClass battle){
        specialConditions playerMobCondition = playerMob.getCondition();
        if(playerMobCondition.getCondition().getEffect() == specialConditions.effect.STUN){
            if(randomScripts.getRandomNumber(1, 100) >= playerMobCondition.getCondition().getPercent()){ //If the player mob is stunned
                return -1;
            }
        }

        String name = opponentCreature.getCustomName();
        name = name.substring(2);
        int health = Integer.parseInt(name);
        float defence = 0;
        int attack = move.getDamage();
        float attackerAttackStat = playerMob.getStats().get(0);
        if(move.getMoveType().equalsIgnoreCase("special")){
            defence = opponentMob.getStats().get(3);
        }else if(move.getMoveType().equalsIgnoreCase("physical")){
            defence = opponentMob.getStats().get(2);
        }

        if(move.hasEffect()){ // If the move has an EFFECT i.e. lower a stat
            int chance = randomScripts.getRandomNumber(1, 100);
            if(chance <= move.getPercent()){
                mobEnums mobToEffect = null;
                switch(move.getTarget()){
                    case SELF:
                        mobToEffect = playerMob;
                    case ENEMY:
                        mobToEffect = opponentMob;
                }
                List<Integer> stats = mobToEffect.getStats();
                stats.set(move.getEffectStat().getValue(), mobToEffect.getStats().get(move.getEffectStat().getValue()) - move.getEffectAmount());
                mobToEffect.setStats(stats);
            }
        }

        if(opponentMob.getCondition() == null){
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

        health = health - Math.round((float)(((((playerMob.getLevel()*1.25)/3.5)+2)*(attackerAttackStat/defence)*attack))/30);
        opponentCreature.setCustomName("§c" + health);
        opponentMob.setCurrentHp(health);

        battle.getMobs().replace(opponentCreature, opponentMob);
        Map<Entity, mobEnums> map = battle.getMobs();
        map.replace(playerEntity, playerMob);
        battle.setMobs(map);

        if(health < 1){

        }

        return health;
    }

    public static Comparator<Map.Entry<Entity, mobEnums>> valueComparator = new Comparator<Map.Entry<Entity, mobEnums>>(){

        @Override
        public int compare(Map.Entry<Entity, mobEnums> o1, Map.Entry<Entity, mobEnums> o2) {
            int v1 = o1.getValue().getStats().get(4);
            int v2 = o2.getValue().getStats().get(4);

            if(v1 < v2){
                return v2;
            }
            return v1;
        }
    };
    public static void showTargetMenu(){

    }
}
