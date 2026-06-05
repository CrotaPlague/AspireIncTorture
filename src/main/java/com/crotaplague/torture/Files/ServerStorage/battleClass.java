package com.crotaplague.torture.Files.ServerStorage;

import com.crotaplague.torture.Events.events;
import com.crotaplague.torture.Files.ServerScriptService.battleEngine;
import com.crotaplague.torture.Files.ServerScriptService.randomScripts;
import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.CQueue;
import com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses.ChainTask;
import com.crotaplague.torture.Files.ServerStorage.boxs.ArenaBox;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.items.HealItem;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.items.ShulkerItem;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Files.ServerStorage.specialConditions.SpecialConditions;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.crotaplague.torture.Torture.runSequential;
import static java.util.List.*;

public class battleClass {

    private CQueue<mobEnums> dead = new CQueue<>();
    private boolean inRound = false;
    private Map<Integer, mobEnums> mobs = new HashMap<>();
    private CQueue<mobEnums> switchMob = new CQueue<>();
    public int playerCount = 0;
    private boolean battleOver;
    private Boolean battling = true;
    private List<humanClass.Trainer> competitors;
    private final battleTypes battleType;
    private List<Entity> entityList = new ArrayList<>();
    private int boxNum;
    private Map<mobEnums, Boolean> entityActions = new HashMap<>();
    private final boolean trainerBattle = false;
    public UUID thisUUID = UUID.randomUUID();
    private boolean wildBattle = false;
    private int waitCounter = 0;
    private ArenaBox box = null;

    public battleClass(List<humanClass.Trainer> competitors, battleTypes battleType){
        this.competitors = competitors;
        this.battleType = battleType;
        for(humanClass.Trainer t : competitors){
            entityList.add(t.getSelf());
            for(mobEnums m : t.getCurrentMobs()){
                entityActions.put(m, false);
            }
        }
    }

    public List<humanClass.Trainer> getCompetitors(){return this.competitors;}
    public void defaultMobStatus(Iterable<mobEnums> ms){for(mobEnums m : ms){entityActions.put(m, false);}}
    public void viewEntity(Entity e){
        for(humanClass.Trainer t : competitors){
            if(t.getSelf() instanceof Player){
                ((Player) t.getSelf()).showEntity(Torture.plugin, e);
            }
        }
    }
    public void setTrainer(humanClass.Trainer trainer){
        List<humanClass.Trainer> ogList = copyOf(this.getCompetitors());
        ogList.forEach(trainer1 -> {if(trainer1.getSelf().getUniqueId() == trainer.getSelf().getUniqueId()){
            competitors.remove(trainer1);}});
        competitors.add(trainer);
    }
    public Boolean entityIsInBattle(Entity entity){
        Boolean bool = false;
        for(humanClass.Trainer trainer : this.competitors){
            if(trainer.getCurrentMobEnt().getUniqueId() == entity.getUniqueId() || trainer.getSelf().getUniqueId() == entity.getUniqueId()) bool = true;

        }
        return bool;
    }

    public mobEnums getOpposingMob(mobEnums mob){
        int spot = mob.getBattleSpot();
        int opSpot;
        if(spot % 2 == 0){
            opSpot = spot - 1;
        }else{
            opSpot = spot + 1;
        }
        if(mobs.containsKey(opSpot)){
            return mobs.get(opSpot);
        }
        return null;
    }
    public ArenaBox getBox(){return this.box;}
    public void setBox(ArenaBox box){this.box = box;}
    public CQueue<mobEnums> getSwitchMob(){return this.switchMob;}
    public void resetSwitchMob(){this.switchMob = new CQueue<>();}
    public void setBattleOver(){this.battleOver = true;}
    public boolean isOver(){return this.battleOver;}
    public boolean inRound(){return this.inRound;}
    public void setInRound(boolean set){this.inRound = set;}
    public battleTypes getBattleType(){return this.battleType;}
    public List<Entity> getEntityCompetitors(){return this.entityList;}
    public Boolean isBattling(){return this.battling;}
    public void setBattling(Boolean status){this.battling = status;}
    public void setBoxNum(int box){this.boxNum = box;}
    public int getBoxNum(){return this.boxNum;}
    public void setMobStatus(mobEnums mob){this.entityActions.put(mob, true);}
    public Map<Integer, mobEnums> getMobs(){return this.mobs;}
    public void setMobs(Map<Integer, mobEnums> breh){this.mobs = breh;}
    public void setMobInPosition(int spot, mobEnums mob){this.mobs.put(spot, mob);}
    public mobEnums getMobInPosition(int spot){return this.mobs.get(spot);}
    public boolean isWild(){return wildBattle;}
    public void setWild(){this.wildBattle = true;}
    public CQueue<mobEnums> deadMobs(){return this.dead;}
    public void resetDead(){this.dead = new CQueue<>();}
    public void incCounter(){waitCounter++;}
    public int getBattleSlot(mobEnums mob){
        for(Map.Entry<Integer, mobEnums> entry : mobs.entrySet()){
            if(entry.getValue().equals(mob)){
                return entry.getKey();
            }
        }
        return -1;
    }
    public mobEnums nextUnready(humanClass.Trainer t){
        for(mobEnums m : t.getCurrentMobs()){
            if(m != null){
                if(!isReady(m)) return m;
            }
        }
        return null;
    }
    public boolean isReady(mobEnums m){
        return entityActions.get(m);
    }
    public void subOutMob(mobEnums old, mobEnums n){
        entityActions.remove(old);
        if(!entityActions.containsKey(n)){
            entityActions.put(n, false);
        }
        Iterator<Map.Entry<Integer, mobEnums>> it = mobs.entrySet().iterator();
        int slotToReplace = -1;
        while (it.hasNext()) {
            Map.Entry<Integer, mobEnums> ent = it.next();
            if (ent.getValue().equals(old)) {
                slotToReplace = ent.getKey();
                it.remove(); // safe removal
            }
        }
        if(slotToReplace != -1){
            mobs.put(slotToReplace, n);
        }
    }
    public void resetStatuses(){
        for(Map.Entry<mobEnums, Boolean> map : this.entityActions.entrySet()){
            entityActions.put(map.getKey(), false);
        }
    }
    public Boolean allReady(){
        for(Map.Entry<mobEnums, Boolean> entry : this.entityActions.entrySet()){
            if(!entry.getValue()){
                return false;
            }
        }
        return true;
    }
    public Map<mobEnums, Boolean> getMobStatus(){return entityActions;}

    @Nullable
    public humanClass.Trainer getTrainer(Entity entity){
        AtomicReference<humanClass.Trainer> trainer = new AtomicReference<humanClass.Trainer>();
        this.getCompetitors().forEach(trainer1 -> {if(trainer1.getSelf().getUniqueId() == entity.getUniqueId()) trainer.set(trainer1);});
        return trainer.get();
    }
    @Nullable
    public humanClass.Trainer getTrainerByMob(Entity entity){
        AtomicReference<humanClass.Trainer> trainer = new AtomicReference<humanClass.Trainer>();
        this.getCompetitors().forEach(trainer1 -> {if(trainer1.isMobOf(entity)){ trainer.set(trainer1);} else{};});
        return trainer.get();
    }

    public void doRound() {
        if (battleOver) return;
        setInRound(true);

        PriorityQueue<mobEnums> order = new PriorityQueue<>((a, b) -> {
            NextMove aMove = a.getNextMove();
            NextMove bMove = b.getNextMove();
            if (aMove == null && bMove == null) return 0;
            if (aMove == null) return 1;
            if (bMove == null) return -1;
            return aMove.compareTo(bMove);
        });
        order.addAll(this.mobs.values());

        List<ChainTask> roundTasks = new ArrayList<>();

        while (!order.isEmpty()) {
            mobEnums mob = order.poll();
            NextMove nextMove = mob.getNextMove();
            if (nextMove == null) continue;

            roundTasks.add((onComplete) -> {
                if (mob.getCurrentHp() <= 0) {
                    onComplete.run();
                    return;
                }

                if (nextMove.isSwap()) {
                    handleSwap(mob, nextMove.getSwapTarget(), onComplete);
                } else if (nextMove.isItem()) {
                    handleItem(mob, nextMove.getItem(), onComplete);
                } else if (nextMove.isMove()) {
                    handleMove(mob, nextMove.getMove(), onComplete);
                } else {
                    onComplete.run();
                }
            });
        }

        // Handle fainted mobs at the end of the round
        roundTasks.add(this::handleFaintedAndReplacements);

        // Finalize round
        roundTasks.add((onComplete) -> {
            resetStatuses();
            setInRound(false);
            for (humanClass.Trainer t : competitors) {
                if (t.getSelf() instanceof Player) {
                    t.displayOptions(this);
                } else {
                    for (mobEnums m : t.getCurrentMobs()) {
                        m.setNextMove(NextMove.fromMove(m.getRandomMove(), m));
                        setMobStatus(m);
                    }
                }
            }
            onComplete.run();
        });

        Torture.runSequential(roundTasks);
    }

    private void handleSwap(mobEnums mob, mobEnums target, Runnable onComplete) {
        humanClass.Trainer trainer = mob.getTrainer();
        randomScripts.battleMessage((LivingEntity) trainer.getSelf(), "You send in " + target.getName());
        for (Entity ent : entityList) {
            if (!ent.getUniqueId().equals(trainer.getSelf().getUniqueId())) {
                randomScripts.battleMessage((LivingEntity) ent, trainer.getName() + " sent in " + target.getName());
            }
        }

        events.swapMob(target, trainer, this, () -> {
            subOutMob(mob, target);
            trainer.setCurrentSelecting(target);
            Bukkit.getScheduler().runTaskLater(Torture.plugin, onComplete, 20L);
        });
    }

    private void handleItem(mobEnums mob, ItemClass item, Runnable onComplete) {
        humanClass.Trainer trainer = mob.getTrainer();
        randomScripts.battleMessage((LivingEntity) trainer.getSelf(), "You use a " + item.getDisplayName());
        for (Entity ent : entityList) {
            if (!ent.getUniqueId().equals(trainer.getSelf().getUniqueId())) {
                randomScripts.battleMessage((LivingEntity) ent, trainer.getName() + " uses a " + item.getDisplayName());
            }
        }

        if (item.getType() == ItemClass.TItemType.HEALING && item instanceof HealItem heal) {
            mobEnums target = heal.getTarget();
            int originalHealth = target.getCurrentHp();
            int maxHp = target.getStats()[moveClass.Stat.HIT_POINTS.getValue()];
            target.setCurrentHp(Math.min(maxHp, target.getCurrentHp() + heal.getHealAmount()));

            if (target.getCondition() != null && heal.getConditions().contains(target.getCondition())) {
                target.setCondition(null);
            }

            // Animation for healing (re-using logic from entMove but for healing)
            int finalHealth = target.getCurrentHp();
            AtomicInteger current = new AtomicInteger(originalHealth);
            AtomicInteger taskId = new AtomicInteger();
            taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(Torture.plugin, () -> {
                int val = current.incrementAndGet();
                if (target.getSelf() != null) {
                    ((LivingEntity) target.getSelf()).customName(Component.text(val, NamedTextColor.GREEN));
                }
                if (val >= finalHealth) {
                    Bukkit.getScheduler().cancelTask(taskId.get());
                    onComplete.run();
                }
            }, 0L, 2L));
        } else if (item instanceof ShulkerItem shulker) {
            shulker.catchMob(shulker.getPossibleCatch());
            onComplete.run();
        } else {
            onComplete.run();
        }
    }

    private void handleMove(mobEnums mob, moveClass move, Runnable onComplete) {
        randomScripts.genMoveTarget(this, move, mob);
        int battleSpot = move.getMobTarget().getBattleSpot();
        mobEnums actualTarget = this.mobs.get(battleSpot);
        move.setMobTarget(actualTarget);

        battleEngine.makeMove(move, this, mob, () -> {
            Bukkit.getScheduler().runTaskLater(Torture.plugin, onComplete, 10L);
        });
    }

    private void handleFaintedAndReplacements(Runnable onComplete) {
        List<humanClass.Trainer> trainersWithDeadMobs = new ArrayList<>();
        for (humanClass.Trainer t : competitors) {
            if (t.hasDeadActiveMob() && !t.isOutOfMobs()) {
                trainersWithDeadMobs.add(t);
            }
        }

        if (trainersWithDeadMobs.isEmpty()) {
            onComplete.run();
            return;
        }

        // This is tricky because we need to wait for multiple players.
        // For simplicity in this refactor, let's process them one by one or wait for all.
        AtomicInteger pendingReplacements = new AtomicInteger(trainersWithDeadMobs.size());

        for (humanClass.Trainer t : trainersWithDeadMobs) {
            if (t.getSelf() instanceof Player player) {
                // Open replacement GUI. 
                player.sendMessage(ChatColor.YELLOW + "One of your mobs fainted! Pick a replacement.");
                
                checkReplacement(t, () -> {
                    if (pendingReplacements.decrementAndGet() == 0) {
                        onComplete.run();
                    }
                });
            } else {
                // AI picks automatically
                Set<mobEnums> deadActiveMobs = t.getDeadMobs();
                mobEnums deadMob = deadActiveMobs.isEmpty() ? null : deadActiveMobs.iterator().next();
                mobEnums replacement = t.getFirstMobAlive();
                if (deadMob != null && replacement != null) {
                    handleSwap(deadMob, replacement, () -> {
                        if (pendingReplacements.decrementAndGet() == 0) {
                            onComplete.run();
                        }
                    });
                } else {
                    if (pendingReplacements.decrementAndGet() == 0) {
                        onComplete.run();
                    }
                }
            }
        }
    }

    private void checkReplacement(humanClass.Trainer trainer, Runnable onComplete) {
        Bukkit.getScheduler().runTaskLater(Torture.plugin, () -> {
            if (!trainer.hasDeadActiveMob() || trainer.isOutOfMobs()) {
                onComplete.run();
            } else {
                checkReplacement(trainer, onComplete);
            }
        }, 10L);
    }
    public void entDead(mobEnums mob){
        this.dead.offer(mob);
    }

    public boolean isAiBattle(){
        for(humanClass.Trainer t : competitors){
            if(!(t.getSelf() instanceof Player)) return true;
        }
        for(mobEnums mo : mobs.values()){
            if(mo.getTrainer() == null) return true;
        }
        return false;
    }


    @Override
    public boolean equals(Object o){
        if(!(o instanceof battleClass)) return false;
        battleClass other = (battleClass) o;
        Map<Integer, mobEnums> oMobs = other.getMobs();
        for(int i = 0; i < mobs.size(); i++){
            if(!oMobs.containsKey(i)) return false;
            mobEnums m = mobs.get(i);
            mobEnums m2 = other.getMobs().get(i);
            if(!Objects.equals(m, m2)){
                return false;
            }
        }
        return other.boxNum == boxNum && playerCount == other.playerCount;
    }



}
