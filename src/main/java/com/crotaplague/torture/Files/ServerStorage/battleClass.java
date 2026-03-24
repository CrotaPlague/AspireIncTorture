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
import com.crotaplague.torture.Files.ServerStorage.specialConditions.specialConditions;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public void doRound(){
        setInRound(true);
        List<mobEnums> order = this.mobs.values().stream().sorted(battleEngine.valueComparator).collect(Collectors.toList());
        for(Map.Entry<Integer, mobEnums> entry : mobs.entrySet()){
            Bukkit.getPlayer("CrotaPlague").sendMessage("yep here ya go: " + entry.getValue() + "and then " + entry.getKey());
        }
        if(battleOver) return;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                for (mobEnums mob : order) {
                    if (mob.getNextMove() instanceof mobEnums) {
                        humanClass.Trainer trainer = mob.getTrainer();
                        trainer = this.getTrainer(trainer.getSelf());
                        trainer.setStupid(mob.getSelf().getUniqueId());
                        trainer.setAlsoStupid(mob.getOpponentMob().getSelf().getUniqueId());

                        this.setTrainer(trainer);
                        final humanClass.Trainer finalTrainer = trainer;
                        this.switchMob.offer(mob);
                        List<ChainTask> tasks = runTask();
                        for(ChainTask t : tasks){
                            runAndWait(t);
                        }

                        waitCounter = 0;
                        List<Thread> allOthers = new ArrayList<>();

                        Thread swapInT = new Thread() {
                            public void run() {
                                randomScripts.battleMessage((LivingEntity) finalTrainer.getSelf(), "You send in " + ((mobEnums) mob.getNextMove()).getName());
                            }
                        };
                        for (Entity ents : entityList) {
                            allOthers.add(new Thread() {
                                public void run() {
                                    if (ents.getUniqueId() != finalTrainer.getSelf().getUniqueId())
                                        randomScripts.battleMessage((LivingEntity) ents, finalTrainer.getName() + " sent in " + ((mobEnums) mob.getNextMove()).getName());
                                }
                            });
                        }
                        swapInT.start();
                        allOthers.forEach(breh -> {
                            breh.start();
                        });
                        int spot = mob.getBattleSpot();
                        trainer.setCurrentSelecting(mobs.get(spot));
                    }
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                this.wait(2000L);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    if (mob.getCurrentHp() > 0) {
                        if (mob.getNextMove() instanceof moveClass) {
                            moveClass move = (moveClass) mob.getNextMove();
                            randomScripts.genMoveTarget(this, move, mob);
                            int battleSpot = move.getMobTarget().getBattleSpot();
                            mobEnums tar = this.mobs.get(battleSpot);
                            move.setMobTarget(tar);

                            battleEngine.makeMove(move, this, mob);
                        }
                        if (mob.getNextMove() instanceof ItemClass) {
                            ItemClass item = (ItemClass) mob.getNextMove();
                            if (item instanceof ShulkerItem) {
                                ShulkerItem shulker = (ShulkerItem) item;
                                shulker.catchMob(((ShulkerItem) mob.getNextMove()).getPossibleCatch());
                            }
                            if (item.getType() == ItemClass.TItemType.HEALING) {
                                HealItem heal = (HealItem) item;
                                mobEnums m = heal.getTarget();
                                int amount = heal.getHealAmount();
                                List<specialConditions> conditions = heal.getConditions();
                                if (!conditions.isEmpty()) {
                                    for (specialConditions e : conditions) {
                                        if (m.getCondition() == e) {
                                            m.setCondition(null);
                                        }
                                    }
                                }
                                int originalHealth = m.getCurrentHp();
                                if (m.getCurrentHp() + amount > m.getStats()[moveClass.Stat.HIT_POINTS.getValue()]) {
                                    m.setCurrentHp(m.getStats()[moveClass.Stat.HIT_POINTS.getValue()]);
                                } else {
                                    m.setCurrentHp(m.getCurrentHp() + amount);
                                }
                                final int finalHealth = m.getCurrentHp();
                                if (mob.hasTrainer() && mob.getTrainer().isInPlay(m)) {
                                    Thread healThread = new Thread() {
                                        public void run() {
                                            for (int health1 = originalHealth; health1 <= finalHealth; health1++) {
                                                if (health1 < 0) break;
                                                try {
                                                    Thread.sleep(62);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                LivingEntity ent = (LivingEntity) m.getSelf();
                                                ent.customName(Component.text(health1, NamedTextColor.RED));
                                            }
                                        }
                                    };

                                    synchronized (healThread) {
                                        healThread.run();
                                        try {
                                            healThread.join();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                final humanClass.Trainer finalTrainer1 = mob.getTrainer();
                                Thread swapInT = new Thread() {
                                    public void run() {
                                        randomScripts.battleMessage((LivingEntity) finalTrainer1.getSelf(), "You use a " + ((ItemClass) mob.getNextMove()).getDisplayName());
                                    }
                                };
                                List<Thread> allOthers = new ArrayList<>();
                                for (Entity ents : entityList) {
                                    allOthers.add(new Thread() {
                                        public void run() {
                                            if (ents.getUniqueId() != finalTrainer1.getSelf().getUniqueId())
                                                randomScripts.battleMessage((LivingEntity) ents, finalTrainer1.getName() + " uses a  " + ((ItemClass) mob.getNextMove()).getDisplayName());
                                        }
                                    });
                                }
                                swapInT.start();
                                allOthers.forEach(breh -> {
                                    breh.start();
                                });
                            }
                        }
                    }
                    synchronized (t) {
                        t.run();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.resetStatuses();
                List<humanClass.Trainer> myList = new ArrayList<>();
                int numDead = 0;
                for (humanClass.Trainer track : this.getCompetitors()) {
                    track.clearMobMoves();
                    if (track.hasDeadActiveMob() && !track.isOutOfMobs()) {
                        myList.add(track);
                        numDead += track.numDeadActive();
                        SaveFile file = Torture.playerSaveFiles.get(track.getSelf().getUniqueId() + "");
                        for (ArmorStand armorStand : file.getArmorStands()) {
                            armorStand.setCustomNameVisible(true);
                        }
                    }
                }
                List<mobEnums> toReplace = new ArrayList<>();
                int completed = 0;
                while (completed < numDead) {
                    Thread newThread = new Thread() {
                        public void run() {
                            try {
                                this.wait(25L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    completed = 0;
                    for (humanClass.Trainer t : myList) {
                        t = this.getTrainer(t.getSelf());
                        int swaps = t.nextMoveSwaps();
                        toReplace.addAll(t.getDeadMobs());
                        completed += swaps;
                    }

                    synchronized (newThread) {
                        newThread.run();
                        try {
                            newThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (mobEnums mob : toReplace) {
                    ChainTask task = (toComplete) -> {
                        humanClass.Trainer t = mob.getTrainer();
                        t.setStupid(mob.getSelf().getUniqueId());
                        t.setAlsoStupid(mob.getOpponentMob().getSelf().getUniqueId());

                        this.setTrainer(t);
                        final humanClass.Trainer thisT = t;
                        switchMob.offer(mob);
                        List<ChainTask> tasks = runTask();
                        for(ChainTask tasked : tasks){
                            runAndWait(tasked);
                        }
                        mob.getTrainer().setCurrentSelecting(mob);
                        waitCounter = 0;
                        List<Thread> allOthers = new ArrayList<>();

                        Thread swapInT = new Thread() {
                            public void run() {
                                randomScripts.battleMessage((LivingEntity) thisT.getSelf(), "You send in " + mob.getName());
                            }
                        };
                        for (Entity ents : entityList) {
                            allOthers.add(new Thread() {
                                public void run() {
                                    if (ents.getUniqueId() != thisT.getSelf().getUniqueId())
                                        randomScripts.battleMessage((LivingEntity) ents, thisT.getName() + " sent in " + mob.getName());
                                }
                            });
                        }
                        swapInT.start();
                        allOthers.forEach(breh -> {
                            breh.start();
                        });

                        Thread t2 = new Thread() {
                            public void run() {
                                try {
                                    this.wait(1000L);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        synchronized (t2) {
                            t2.run();
                            try {
                                t2.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                }
                for (humanClass.Trainer track : this.getCompetitors()) {
                    if (track.isOutOfMobs()) continue;
                    if (track.getSelf() instanceof Player) {
                        SaveFile file = Torture.playerSaveFiles.get(track.getSelf().getUniqueId() + "");
                        for (ArmorStand armorStand : file.getArmorStands()) {
                            armorStand.setCustomNameVisible(true);
                        }
                    } else {
                        for (mobEnums m : track.getCurrentMobs()) {
                            m.setNextMove(m.getRandomMove());
                            setMobStatus(m);
                        }
                    }
                }
                setInRound(false);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
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

    public List<ChainTask> runTask(){
        List<ChainTask> tasks = new ArrayList<>();
        if(!switchMob.isEmpty()){
            while(!switchMob.isEmpty()){
                mobEnums mob = switchMob.poll();
                ChainTask task = (onComplete) -> {
                    events.swapMob((mobEnums) mob.getNextMove(), mob.getTrainer(), this, onComplete);
                    subOutMob(mob, (mobEnums) mob.getNextMove());
                    this.incCounter();
                };

                tasks.add(task);
            }
        }
        return tasks;
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

    public static void runAndWait(ChainTask task) {
        CountDownLatch latch = new CountDownLatch(1);
        task.run(latch::countDown);
        try {
            latch.await(); // Blocks until onComplete is called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
