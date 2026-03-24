package com.crotaplague.torture.Files.ServerStorage;



import com.crotaplague.torture.Files.DataManager;
import com.crotaplague.torture.Files.ServerStorage.humans.humanClass;
import com.crotaplague.torture.Files.ServerStorage.items.BagClass;
import com.crotaplague.torture.Files.ServerStorage.items.ItemClass;
import com.crotaplague.torture.Files.ServerStorage.mobs.mobEnums;
import com.crotaplague.torture.Torture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import java.util.*;

public class SaveFile implements ConfigurationSerializable {

    private Map<Integer, mobEnums> PlayerMobs = new HashMap<>();
    private Location playerLoc;
    private Map<Integer, Map<Integer, mobEnums>> mobBoxes = new HashMap<>();
    private int routeNum = 0;
    private BitSet defeatedTrainers = new BitSet();
    private BagClass playerBag = new BagClass();
    private Player player;
    public List<Pair<String, MessagePurpose>> messageQueue = new ArrayList<>();
    private humanClass.aiTrainer opponent;
    private int cash;
    private List<ArmorStand> armorStandList = new ArrayList<>();
    private boolean inBattle = false;
    private float timeOnServer = 0;
    private String username;
    private boolean autoSave = true;
    int autoDelay = 60;
    private String message = "";
    private Runnable messageTask;
    int messageCounter = 0;
    private Location preBattleLocation;
    private BitSet collectedItems = new BitSet();
    private DataManager dataFile;
    private Location lastHealLoc;


    public SaveFile(){mobBoxes.put(0, new HashMap<Integer, mobEnums>()); playerBag = new BagClass(); cash = 0;}
    public SaveFile(Map<Integer, mobEnums> mobs){this.PlayerMobs = mobs; mobBoxes.put(0, new HashMap<Integer, mobEnums>());}

    public void setPlayerMobs(Map<Integer, mobEnums> mobs){this.PlayerMobs = mobs;}
    public Map<Integer, mobEnums> getPlayerMobs(){return this.PlayerMobs;}
    public void setPlayerMobs(@Nonnull List<mobEnums> mobs){
        int counter = 0;
        this.PlayerMobs = new HashMap<>();
        for(mobEnums mob : mobs){
            this.setPlayerMob(counter, mob);
            counter++;
        }
    }
    public void setSpecificBox(Integer num, @Nonnull List<mobEnums> mobs){
        int counter = 0;
        setSpecificBox(num, new HashMap<>());
        Map<Integer, mobEnums> thisMap = new HashMap<>();
        for(mobEnums mob : mobs){
            thisMap.put(counter, mob);
            counter++;
        }
        this.mobBoxes.put(num, thisMap);
    }
    public Location getLastHealLoc(){return this.lastHealLoc;}
    public void setLastHealLoc(Location l){this.lastHealLoc = l;}
    public void claimItem(int i){collectedItems.set(i);}
    public boolean hasClaimedItem(int i){return collectedItems.get(i);}
    public void setCollectedItems(BitSet s){this.collectedItems = s;}
    public void setPreBattleLocation(Location l){this.preBattleLocation = l;}
    public Location getPreBattleLocation(){return this.preBattleLocation;}
    public void setPlayerMob(int num, mobEnums mob){this.PlayerMobs.put(num, mob);}
    public void setPlayerLoc(Location newLoc){this.playerLoc = newLoc;}
    public Location getPlayerLoc(){return this.playerLoc;}
    public void setMobBox(Map<Integer, Map<Integer, mobEnums>> newBox){this.mobBoxes = newBox;}
    public void setSpecificBox(int boxNum, Map<Integer, mobEnums> box){mobBoxes.put(boxNum, box);}
    public Map<Integer, Map<Integer, mobEnums>> getMobBoxes(){return this.mobBoxes;}
    public int getRouteNum(){return this.routeNum;}
    public void setRouteNum(int routeNum){this.routeNum = routeNum;}
    public void addDefeatedTrainer(int num){this.defeatedTrainers.set(num);}
    public BitSet getDefeatedTrainers(){return this.defeatedTrainers;}
    public void setDefeatedTrainers(BitSet set){this.defeatedTrainers = set;}
    public boolean hasDefeatedTrainer(int num){return this.defeatedTrainers.get(num);}
    public void instantiateTrainers(){this.defeatedTrainers = new BitSet();}
    public BagClass getPlayerBag(){return this.playerBag;}
    public void setPlayerBag(BagClass bag){this.playerBag = bag;}
    public void setCash(int cash){this.cash = cash;}
    public int getCash(){return this.cash;}
    public void addCash(int cash){this.cash += cash;}
    public void setInBattle(boolean trueFalse){this.inBattle = trueFalse;}
    public boolean getInBattle(){return this.inBattle;}
    public void tickWentBy(){this.timeOnServer += ((double) 1)/((double) 72000); this.autoDelay--; messageCounter++; if(messageTask != null && messageCounter%2 == 0) Bukkit.getScheduler().runTask(Torture.plugin, messageTask);}
    public void setTicks(float time){this.timeOnServer = time;}
    public float getTimeOnServer(){return this.timeOnServer;}
    public String getName(){return this.username;}
    public void setName(String a){this.username = a;}
    public boolean autoSaveEnabled(){return this.autoSave;}
    public void setAutoSave(boolean c){this.autoSave = c;}
    public void resetAutoDelay(){this.autoDelay = 60;};
    public int getAutoDelay(){return this.autoDelay;}
    public void claimMobs(){
        for(mobEnums m : PlayerMobs.values()){
            m.setTrainer(this.createTrainer());
        }
    }
    public void addMobToAvailable(mobEnums mob){
        if(PlayerMobs.size() < 6){
            for(int i = 1; i < 7; i++){
                if(!PlayerMobs.containsKey(i)){
                    PlayerMobs.put(i, mob);
                    break;
                }
            }
        }else{
            for(Map.Entry<Integer, Map<Integer, mobEnums>> entry : mobBoxes.entrySet()){
                if(entry.getValue().size() < 57){
                    entry.getValue().put(entry.getValue().size() + 1, mob);
                    mobBoxes.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("playerLoc", this.playerLoc.serialize());
        result.put("username", this.username);
        result.put("routeNum", this.routeNum);
        result.put("timeOnServer", this.timeOnServer);
        result.put("PlayerBag", this.playerBag.serialize());

        for (Map.Entry<Integer, Map<Integer, mobEnums>> mapEntry : this.mobBoxes.entrySet()) {
            List<mobEnums> mobs = new ArrayList<>();
            for (Map.Entry<Integer, mobEnums> entry : mapEntry.getValue().entrySet()) {
                mobs.add(entry.getValue().setSlotInPc(entry.getKey()));
            }
            if (!mobs.isEmpty()) {
                Bukkit.getPlayer("CrotaPlague").sendMessage(mobs.get(0).getName()); // Optional debug
            }
            result.put("PCBox " + mapEntry.getKey(), mobs);
        }

        this.PlayerMobs.forEach((index, mobE) -> {
            result.put("MobSlot " + index, mobE.serialize());
        });

        byte[] bytes = collectedItems.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        result.put("collectedItems", base64);

        bytes = defeatedTrainers.toByteArray();
        base64 = Base64.getEncoder().encodeToString(bytes);
        result.put("defeatedTrainers", base64);

        if(lastHealLoc != null){
            result.put("healLoc", lastHealLoc.serialize());
        }

        return result;
    }

    public static SaveFile deserialize(MemorySection section){
        return deserialize(section.getValues(false));
    }

    @SuppressWarnings("unchecked")
    public static SaveFile deserialize(Map<String, Object> map) {
        SaveFile playerSaveFile = new SaveFile();

        if (map.containsKey("username"))
            playerSaveFile.setName((String) map.get("username"));

        if (map.containsKey("timeOnServer"))
            playerSaveFile.setTicks(((Number) map.get("timeOnServer")).floatValue());

        if (map.containsKey("routeNum"))
            playerSaveFile.setRouteNum((Integer) map.get("routeNum"));

        if(map.containsKey("healLoc")){
            Object locObj = map.get("healLoc");
            if (locObj instanceof Map) {
                playerSaveFile.setLastHealLoc(Location.deserialize((Map<String, Object>) locObj));
            }else if(locObj instanceof MemorySection sec){
                playerSaveFile.setLastHealLoc(Location.deserialize(sec.getValues(false)));
            }
        }

        if (map.containsKey("playerLoc")) {
            Object locObj = map.get("playerLoc");
            if (locObj instanceof Map) {
                playerSaveFile.setPlayerLoc(Location.deserialize((Map<String, Object>) locObj));
            }else if(locObj instanceof MemorySection sec){
                playerSaveFile.setPlayerLoc(Location.deserialize(sec.getValues(false)));
            }
        }

        if (map.containsKey("PlayerBag")) {
            BagClass bag = BagClass.deserialize(((MemorySection)map.get("PlayerBag")));
            playerSaveFile.setPlayerBag(bag);
        }

        for (String path : map.keySet()) {
            if (path.startsWith("PCBox ")) {
                String[] split = path.split(" ");
                try {
                    int boxId = Integer.parseInt(split[1]);
                    List<?> rawList = (List<?>) map.get(path);
                    Map<Integer, mobEnums> box = new HashMap<>();

                    for (Object obj : rawList) {
                        if (obj instanceof Map rawMob) {
                            mobEnums mob = mobEnums.deserialize(rawMob);
                            box.put(mob.getSlotInPc(), mob);
                        }
                    }

                    playerSaveFile.setSpecificBox(boxId, box);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (map.containsKey("collectedItems")) {
            try {
                String base64 = (String) map.get("collectedItems");
                byte[] bytes = Base64.getDecoder().decode(base64);
                playerSaveFile.setCollectedItems(BitSet.valueOf(bytes));
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to decode collectedItems BitSet!");
            }
        }

        if(map.containsKey("defeatedTrainers")){
            try{
                String base64 = (String) map.get("defeatedTrainers");
                byte[] bytes = Base64.getDecoder().decode(base64);
                playerSaveFile.setDefeatedTrainers(BitSet.valueOf(bytes));
            }catch (Exception e){
                Marker marker = MarkerFactory.getMarker("SAVE_FAILURE");
                Torture.plugin.getComponentLogger().debug(marker, Component.text("Failed to decode defeated trainers BitSet!", NamedTextColor.RED));
            }
        }
        for (int i = 0; i < 7; i++) {
            String key = "MobSlot " + i;
            if (map.containsKey(key)) {
                Object mobObj = map.get(key);
                mobEnums m = null;
                if (mobObj instanceof MemorySection section) {
                    m = mobEnums.deserialize(section.getValues(true));
                } else if (mobObj instanceof Map) {
                    m = mobEnums.deserialize((Map<String, Object>) mobObj);
                }
                if (m != null) {
                    m.setTrainer(playerSaveFile.createTrainer());
                    playerSaveFile.setPlayerMob(i, m);
                }

            }
        }
        playerSaveFile.claimMobs();

        return playerSaveFile;
    }
    public static class MobBox{
        private Map<Integer, mobEnums> inTheBox;
        private Map<Integer, mobEnums> inTheHotbar;

        public MobBox(Map<Integer, mobEnums> box){
            this.inTheBox = box;
        }
        public MobBox(Map<Integer, mobEnums> box, Map<Integer, mobEnums> hotbar){
            this.inTheBox = box;
            this.inTheHotbar = hotbar;
        }
        public Map<Integer, mobEnums> getBox(){return this.inTheBox;}
        public Map<Integer, mobEnums> getHotbar(){return this.inTheHotbar;}
        public List<mobEnums> getAllMobs(){
            List<mobEnums> total = new ArrayList<>();
            total.addAll(new ArrayList<>(this.inTheBox.values()));
            total.addAll(this.inTheHotbar.values());
            return total;
        }
    }
    // player detailing
    public void setPlayer(Player player){this.player = player; this.username = player.getName(); claimMobs(); dataFile = new DataManager(player);}
    public Player getPlayer(){return this.player;}
    public DataManager getDataFile(){return this.dataFile;}
    public humanClass.Trainer createTrainer(){
        Collection<mobEnums> co = PlayerMobs.values();
        List<mobEnums> hotBar = new ArrayList<>(co);
        return new humanClass.Trainer(player, hotBar, username);
    }
    public void addToQueue(String adder){this.messageQueue.add(new Pair<>(adder, MessagePurpose.NONE));}
    public void addToQueue(String adder, MessagePurpose purpose){this.messageQueue.add(new Pair<>(adder, purpose));}
    public List<Pair<String, MessagePurpose>> messageList(){return this.messageQueue;}
    public Map<String, MessagePurpose> messageQueue(){
        Map<String, MessagePurpose> map = new HashMap<>();
        for(Pair<String, MessagePurpose> pair : this.messageQueue){
            map.put(pair.getLeft(), pair.getRight());
        }
        return map;
    }
    public humanClass.Trainer.aiTrainer getOpponent(){return this.opponent;}
    public void setOpponent(humanClass.aiTrainer op){this.opponent = op;}
    public void setMessagePurpose(MessagePurpose purpose){this.purpose = purpose;}
    public MessagePurpose getPurpose(){return this.purpose;}
    private MessagePurpose purpose;
    public static enum MessagePurpose{
        NONE, TRAINER, BATTLE_END, DAMAGE, KILL_MOB, ANIMATION;
    }
    public List<ArmorStand> getArmorStands(){return this.armorStandList;}
    public List<ArmorStand> addArmorStand(ArmorStand stand){this.armorStandList.add(stand); return this.armorStandList;}
    public void hideArmorStands(){this.armorStandList.forEach(s -> s.setCustomNameVisible(false));}
    public String getMessage(){return this.message;}
    public void setMessage(String message){this.message = message;}
    public void setMessageTask(Runnable run){this.messageTask = run; messageCounter = 0;}
}
