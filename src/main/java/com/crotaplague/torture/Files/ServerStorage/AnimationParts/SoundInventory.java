package com.crotaplague.torture.Files.ServerStorage.AnimationParts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SoundInventory - searchable/paged sound GUI.
 *
 * Fixes:
 * - No more ItemStack crashes from non-item materials like CAVE_VINES.
 * - Safe material lookup for icons with fallbacks.
 * - Better matching for bamboo, basalt, bubble column, lava, azalea, beehive, barrel, chorus fruit, etc.
 * - Generic icons are penalized so they stop winning randomly.
 * - Sound loading fallback now actually uses Sound.values().
 */
public class SoundInventory implements Listener, CommandExecutor {

    private static final int GUI_SIZE = 54;
    private static final int PAGE_ITEM_SLOTS = 45; // 0-44
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int STOP_SLOT = 50;
    private static final int NEXT_SLOT = 53;

    private final JavaPlugin plugin;
    private final NamespacedKey pdcKey;

    private final List<Sound> allSounds = new ArrayList<>();
    private final Map<String, Sound> soundLookup = new HashMap<>();
    private final Map<UUID, Sound> lastPlayedSound = new HashMap<>();
    private final List<Material> iconCandidates;
    private final Map<String, String[]> exactIconOverrides = new HashMap<>();
    private final List<IconRule> iconRules = new ArrayList<>();

    private static final Set<String> GENERIC_WORDS = Set.of(
            "minecraft", "block", "entity", "item", "ambient", "music", "record",
            "ui", "weather", "random", "sound", "sounds",
            "hit", "step", "place", "break", "fall", "swim", "click", "attack",
            "use", "open", "close", "drag", "pick", "pickup", "equip", "shoot",
            "consume", "craft", "portal", "in", "out", "underwater", "under", "above"
    );

    public SoundInventory(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pdcKey = new NamespacedKey(plugin, "sound_key");

        // IMPORTANT: only real item materials can ever become ItemStacks.
        this.iconCandidates = Arrays.stream(Material.values())
                .filter(m -> !m.isLegacy())
                .filter(m -> !m.isAir())
                .filter(Material::isItem)
                .collect(Collectors.toList());

        buildExactOverrides();
        buildIconRules();
        loadAllSounds();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        PluginCommand cmd = plugin.getCommand("soundgui");
        if (cmd != null) {
            cmd.setExecutor(this);
        }
    }

    private void loadAllSounds() {
        Set<Sound> loaded = new LinkedHashSet<>();

        try {
            for (Sound s : Registry.SOUNDS) {
                loaded.add(s);
            }
            plugin.getLogger().info("SoundInventory: loaded " + loaded.size() + " sounds from Registry.SOUNDS");
        } catch (Throwable t) {
            plugin.getLogger().warning("SoundInventory: Registry.SOUNDS load failed: " + t.getMessage());
        }

        allSounds.clear();
        allSounds.addAll(loaded);
        allSounds.sort(Comparator.comparing(this::getSoundKeyStringSafe, String.CASE_INSENSITIVE_ORDER));

        soundLookup.clear();
        for (Sound s : allSounds) {
            soundLookup.put(normalizeKey(getSoundKeyStringSafe(s)), s);
        }
    }

    public void openSoundGui(Player player, String filter, int page) {
        List<Sound> matched = findMatchedSounds(filter);

        int total = matched.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_ITEM_SLOTS));
        page = Math.max(1, Math.min(page, pages));

        String displayFilter = (filter == null || filter.isBlank()) ? "<all>" : filter;
        String title = ChatColor.DARK_AQUA + "Sounds: " + ChatColor.WHITE + displayFilter
                + ChatColor.GRAY + " (" + page + "/" + pages + ")";

        Inventory inv = Bukkit.createInventory(new SoundGuiHolder(filter, page), GUI_SIZE, title);

        int start = (page - 1) * PAGE_ITEM_SLOTS;
        int end = Math.min(start + PAGE_ITEM_SLOTS, total);

        if (total == 0) {
            ItemStack none = new ItemStack(Material.BARRIER);
            ItemMeta meta = none.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "No sounds found");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Try a different search term.",
                        ChatColor.DARK_GRAY + "Examples: azalea, lava, chorus, bamboo"
                ));
                none.setItemMeta(meta);
            }
            inv.setItem(22, none);
        } else {
            for (int slot = 0, i = start; i < end && slot < PAGE_ITEM_SLOTS; i++, slot++) {
                Sound s = matched.get(i);
                String keyString = getSoundKeyStringSafe(s);

                Material icon = safeDisplayMaterial(chooseIconFor(s));
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + formatDisplayNameFromKey(keyString));
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Sound: " + ChatColor.WHITE + keyString,
                            ChatColor.GRAY + "Click to play"
                    ));
                    meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, keyString);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            }
        }

        decorateNavigationRow(inv, filter, page, pages, total);
        player.openInventory(inv);
    }

    private void decorateNavigationRow(Inventory inv, String filter, int page, int pages, int total) {
        for (int slot = 45; slot < 54; slot++) {
            inv.setItem(slot, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        inv.setItem(PREV_SLOT, page > 1
                ? createNavItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", List.of(ChatColor.GRAY + "Page " + (page - 1)))
                : createNavItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "Previous Page", List.of(ChatColor.GRAY + "Unavailable")));

        inv.setItem(INFO_SLOT, createNavItem(
                Material.PAPER,
                ChatColor.AQUA + "Filter Info",
                Arrays.asList(
                        ChatColor.GRAY + "Filter: " + ChatColor.WHITE + ((filter == null || filter.isBlank()) ? "<all>" : filter),
                        ChatColor.GRAY + "Page: " + ChatColor.WHITE + page + "/" + pages,
                        ChatColor.GRAY + "Results: " + ChatColor.WHITE + total
                )
        ));

        inv.setItem(STOP_SLOT, createNavItem(
                Material.BARRIER,
                ChatColor.RED + "Stop Music",
                List.of(ChatColor.GRAY + "Stops the last played sound")
        ));

        inv.setItem(NEXT_SLOT, page < pages
                ? createNavItem(Material.ARROW, ChatColor.YELLOW + "Next Page", List.of(ChatColor.GRAY + "Page " + (page + 1)))
                : createNavItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "Next Page", List.of(ChatColor.GRAY + "Unavailable")));
    }

    private ItemStack createNavItem(Material material, String name, List<String> lore) {
        Material safe = safeDisplayMaterial(material);
        ItemStack item = new ItemStack(safe);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof SoundGuiHolder holder)) return;

        e.setCancelled(true);

        int rawSlot = e.getRawSlot();
        if (rawSlot < 0 || rawSlot >= e.getView().getTopInventory().getSize()) return;

        int slot = rawSlot;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }

        if (slot == STOP_SLOT) {
            Sound last = lastPlayedSound.remove(p.getUniqueId());
            if (last != null) {
                p.stopSound(last);
            }
            return;
        }

        if (slot == NEXT_SLOT) {
            openSoundGui(p, holder.filter, holder.page + 1);
            return;
        }

        if (slot == PREV_SLOT) {
            openSoundGui(p, holder.filter, holder.page - 1);
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        if (meta.getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {
            String keyString = meta.getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING);
            if (keyString == null) return;

            Sound s = soundLookup.get(normalizeKey(keyString));
            if (s != null) {
                lastPlayedSound.put(p.getUniqueId(), s);
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            } else {
                p.sendMessage(ChatColor.RED + "Sound not available: " + keyString);
            }
        }
    }

    private List<Sound> findMatchedSounds(String filter) {
        String normalized = normalizeKey(filter);
        if (normalized.isBlank()) {
            return new ArrayList<>(allSounds);
        }

        String[] terms = normalized.split(" ");
        List<ScoredSound> scored = new ArrayList<>();

        for (Sound s : allSounds) {
            String key = normalizeKey(getSoundKeyStringSafe(s));
            int score = scoreSoundKey(key, terms);
            if (score > 0) {
                scored.add(new ScoredSound(s, score, key));
            }
        }

        scored.sort(Comparator
                .comparingInt(ScoredSound::score).reversed()
                .thenComparing(ScoredSound::key));

        return scored.stream().map(ScoredSound::sound).collect(Collectors.toList());
    }

    private int scoreSoundKey(String soundKey, String[] terms) {
        int score = 0;
        Set<String> tokens = new HashSet<>(Arrays.asList(soundKey.split(" ")));

        for (String term : terms) {
            if (term == null || term.isBlank()) continue;

            if (soundKey.equals(term)) {
                score += 250;
                continue;
            }
            if (tokens.contains(term)) {
                score += 150;
                continue;
            }
            if (soundKey.startsWith(term + " ")) {
                score += 110;
                continue;
            }
            if (soundKey.contains(" " + term + " ")) {
                score += 90;
                continue;
            }
            if (soundKey.contains(term)) {
                score += Math.min(60, 10 + term.length() * 2);
            }
        }

        return score;
    }

    public Material chooseIconFor(Sound sound) {
        String soundKey = normalizeKey(getSoundKeyStringSafe(sound));

        Material exact = exactOverride(soundKey);
        if (exact != null) {
            return exact;
        }

        Material special = chooseSpecialFamilyMaterial(soundKey);
        if (special != null) {
            return special;
        }

        IconMatch bestRule = null;
        for (IconRule rule : iconRules) {
            int score = rule.score(soundKey);
            if (score > 0 && (bestRule == null || score > bestRule.score)) {
                bestRule = new IconMatch(rule, score);
            }
        }

        if (bestRule != null && bestRule.score >= 220) {
            Material preferred = bestRule.rule.resolvePreferred();
            if (preferred != null) {
                return preferred;
            }
        }

        Material auto = chooseBestMaterialBySimilarity(soundKey);
        if (auto != null) {
            return auto;
        }

        return Material.PAPER;
    }


    private Material chooseSpecialFamilyMaterial(String soundKey) {
        if (soundKey == null || soundKey.isBlank()) return null;

        if (containsAny(soundKey, "music") && !containsAny(soundKey, "disc", "record", "jukebox")) {
            Material mat = resolveFirstItem("JUKEBOX", "NOTE_BLOCK");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "music disc", "record")) {
            Material disc = chooseMusicDiscMaterial(soundKey);
            if (disc != null) return disc;
        }

        Material woodVariant = chooseWoodVariantMaterial(soundKey);
        if (woodVariant != null) return woodVariant;

        if (containsAll(soundKey, "chain") && !containsAny(soundKey, "command", "block")) {
            Material mat = resolveFirstItem("CHAIN");
            if (mat != null) return mat;
        }

        if (containsAll(soundKey, "deepslate")) {
            Material mat = resolveFirstItem("DEEPSLATE", "POLISHED_DEEPSLATE", "DEEPSLATE_TILES", "DEEPSLATE_BRICKS", "CRACKED_DEEPSLATE_BRICKS", "CRACKED_DEEPSLATE_TILES", "CHISELED_DEEPSLATE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "anvil")) {
            Material mat = resolveFirstItem("ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "candle")) {
            Material mat = resolveFirstItem("CANDLE", "WHITE_CANDLE", "ORANGE_CANDLE", "MAGENTA_CANDLE", "LIGHT_BLUE_CANDLE", "YELLOW_CANDLE", "LIME_CANDLE", "PINK_CANDLE", "GRAY_CANDLE", "LIGHT_GRAY_CANDLE", "CYAN_CANDLE", "PURPLE_CANDLE", "BLUE_CANDLE", "BROWN_CANDLE", "GREEN_CANDLE", "RED_CANDLE", "BLACK_CANDLE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "comparator")) {
            Material mat = resolveFirstItem("COMPARATOR");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "cobweb", "web")) {
            Material mat = resolveFirstItem("COBWEB");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "conduit")) {
            Material mat = resolveFirstItem("CONDUIT");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "composter")) {
            Material mat = resolveFirstItem("COMPOSTER");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "dispenser")) {
            Material mat = resolveFirstItem("DISPENSER");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "dropper")) {
            Material mat = resolveFirstItem("DROPPER");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "hopper")) {
            Material mat = resolveFirstItem("HOPPER");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "lantern")) {
            Material mat = resolveFirstItem("LANTERN", "SOUL_LANTERN");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "eyeblossom")) {
            Material mat = resolveFirstItem("EYEBLOSSOM", "OPEN_EYEBLOSSOM", "CLOSED_EYEBLOSSOM");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "nether bricks", "nether brick")) {
            Material mat = resolveFirstItem("NETHER_BRICKS", "CHISELED_NETHER_BRICKS", "CRACKED_NETHER_BRICKS", "RED_NETHER_BRICKS");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "note block", "noteblock")) {
            Material mat = resolveFirstItem("NOTE_BLOCK");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "pressure plate")) {
            Material mat = resolveFirstItem("STONE_PRESSURE_PLATE", "OAK_PRESSURE_PLATE", "SPRUCE_PRESSURE_PLATE", "BIRCH_PRESSURE_PLATE", "JUNGLE_PRESSURE_PLATE", "ACACIA_PRESSURE_PLATE", "DARK_OAK_PRESSURE_PLATE", "MANGROVE_PRESSURE_PLATE", "CHERRY_PRESSURE_PLATE", "BAMBOO_PRESSURE_PLATE", "CRIMSON_PRESSURE_PLATE", "WARPED_PRESSURE_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "shelf", "shelves", "bookshelf")) {
            Material mat = resolveFirstItem("CHISELED_BOOKSHELF", "BOOKSHELF");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "sand", "sandy")) {
            Material mat = resolveFirstItem("SAND", "RED_SAND");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "sponge")) {
            Material mat = resolveFirstItem("SPONGE", "WET_SPONGE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "shroomlight")) {
            Material mat = resolveFirstItem("SHROOMLIGHT");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "slime")) {
            Material mat = resolveFirstItem("SLIME_BLOCK", "SLIME_BALL");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "smoker")) {
            Material mat = resolveFirstItem("SMOKER");
            if (mat != null) return mat;
        }

        if (containsAll(soundKey, "smithing", "table")) {
            Material mat = resolveFirstItem("SMITHING_TABLE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "resin")) {
            Material mat = resolveFirstItem("RESIN_BLOCK", "RESIN_BRICKS", "RESIN_CLUMP");
            if (mat != null) return mat;
        }

        if (containsAll(soundKey, "obsidian") || containsAll(soundKey, "portal")) {
            Material mat = resolveFirstItem("CRYING_OBSIDIAN", "OBSIDIAN");
            if (mat != null) return mat;
        }

        if (containsAll(soundKey, "respawn", "anchor")) {
            Material mat = resolveFirstItem("RESPAWN_ANCHOR");
            if (mat != null) return mat;
        }

        return null;
    }

    private Material chooseWoodVariantMaterial(String soundKey) {
        String family = detectWoodFamily(soundKey);
        if (family == null) return null;

        if (containsAny(soundKey, "hanging sign")) {
            Material mat = resolveFirstItem(family + "_HANGING_SIGN");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "sign")) {
            Material mat = resolveFirstItem(family + "_SIGN", family + "_HANGING_SIGN");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "button")) {
            Material mat = resolveFirstItem(family + "_BUTTON");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "door")) {
            Material mat = resolveFirstItem(family + "_DOOR");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "trapdoor")) {
            Material mat = resolveFirstItem(family + "_TRAPDOOR");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "fence gate")) {
            Material mat = resolveFirstItem(family + "_FENCE_GATE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "fence")) {
            Material mat = resolveFirstItem(family + "_FENCE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "pressure plate")) {
            Material mat = resolveFirstItem(family + "_PRESSURE_PLATE");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "slab")) {
            Material mat = resolveFirstItem(family + "_SLAB");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "stairs")) {
            Material mat = resolveFirstItem(family + "_STAIRS");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "boat")) {
            Material mat = resolveFirstItem(family + "_BOAT", family + "_CHEST_BOAT");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "mosaic")) {
            Material mat = resolveFirstItem(family + "_MOSAIC");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "plank")) {
            Material mat = resolveFirstItem(family + "_PLANKS");
            if (mat != null) return mat;
        }

        if (containsAny(soundKey, "wood", "log", "stem", "hyphae")) {
            Material mat = resolveFirstItem(family + "_WOOD", family + "_LOG", family + "_STEM", family + "_HYPHAE", family + "_PLANKS", family + "_BLOCK");
            if (mat != null) return mat;
        }

        return null;
    }

    private String detectWoodFamily(String soundKey) {
        if (containsAll(soundKey, "dark", "oak")) return "DARK_OAK";
        if (containsAll(soundKey, "pale", "oak")) return "PALE_OAK";
        if (containsAny(soundKey, "cherry")) return "CHERRY";
        if (containsAny(soundKey, "mangrove")) return "MANGROVE";
        if (containsAny(soundKey, "bamboo")) return "BAMBOO";
        if (containsAny(soundKey, "warped")) return "WARPED";
        if (containsAny(soundKey, "crimson")) return "CRIMSON";
        if (containsAny(soundKey, "spruce")) return "SPRUCE";
        if (containsAny(soundKey, "birch")) return "BIRCH";
        if (containsAny(soundKey, "jungle")) return "JUNGLE";
        if (containsAny(soundKey, "acacia")) return "ACACIA";
        if (containsAny(soundKey, "oak")) return "OAK";
        if (containsAll(soundKey, "nether", "wood")) return "WARPED";
        if (containsAll(soundKey, "nether", "door")) return "WARPED";
        if (containsAll(soundKey, "nether", "trapdoor")) return "WARPED";
        if (containsAll(soundKey, "nether", "sign")) return "WARPED";
        if (containsAll(soundKey, "nether", "button")) return "WARPED";
        if (containsAll(soundKey, "nether", "fence")) return "WARPED";
        if (containsAll(soundKey, "nether", "plank")) return "WARPED";
        return null;
    }

    private Material chooseMusicDiscMaterial(String soundKey) {
        List<Material> discs = iconCandidates.stream()
                .filter(m -> normalizeKey(m.name()).startsWith("music disc"))
                .collect(Collectors.toList());

        if (discs.isEmpty()) {
            return null;
        }

        Set<String> soundTokens = tokensOf(soundKey);
        Material best = null;
        int bestScore = 0;

        for (Material material : discs) {
            String materialKey = normalizeKey(material.name());
            Set<String> matTokens = tokensOf(materialKey);

            int score = 40;

            for (String token : matTokens) {
                if (token.isBlank() || GENERIC_WORDS.contains(token)) continue;
                if (soundTokens.contains(token)) {
                    score += 260;
                } else if (soundKey.contains(token)) {
                    score += 120;
                }
            }

            String suffix = materialKey.replace("music disc", "").trim();
            if (!suffix.isBlank() && soundKey.contains(suffix)) {
                score += 400;
            }

            if (materialKey.endsWith("cat") && !soundTokens.contains("cat")) {
                score -= 220;
            }

            if (score > bestScore) {
                bestScore = score;
                best = material;
            }
        }

        return best;
    }

    private boolean containsAny(String soundKey, String... terms) {
        for (String term : terms) {
            if (term == null || term.isBlank()) continue;
            if (soundKey.contains(normalizeKey(term))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAll(String soundKey, String... terms) {
        for (String term : terms) {
            if (term == null || term.isBlank()) continue;
            if (!soundKey.contains(normalizeKey(term))) {
                return false;
            }
        }
        return true;
    }

    private Material exactOverride(String soundKey) {
        String[] names = exactIconOverrides.get(soundKey);
        if (names == null) return null;
        return resolveFirstItem(names);
    }

    private Material chooseBestMaterialBySimilarity(String soundKey) {
        Set<String> soundTokens = tokensOf(soundKey);

        Material best = null;
        int bestScore = 0;

        for (Material material : iconCandidates) {
            String matKey = normalizeKey(material.name());
            Set<String> matTokens = tokensOf(matKey);
            int score = compareTokens(soundTokens, matTokens, soundKey, matKey);

            if (score > bestScore) {
                bestScore = score;
                best = material;
            }
        }

        return best;
    }

    private int compareTokens(Set<String> soundTokens, Set<String> materialTokens, String soundKey, String materialKey) {
        int score = 0;

        if (soundKey.equals(materialKey)) {
            return 2000;
        }

        for (String token : soundTokens) {
            if (token.isBlank() || GENERIC_WORDS.contains(token)) continue;

            if (materialTokens.contains(token)) {
                score += 180;
            } else if (materialKey.contains(token)) {
                score += 95;
            }
        }

        // Strong exact-family boosts for the stuff you called out.
        if (soundTokens.contains("azalea") && (materialTokens.contains("azalea") || materialKey.contains("azalea"))) score += 250;
        if (soundTokens.contains("bamboo") && (materialTokens.contains("bamboo") || materialKey.contains("bamboo"))) score += 260;
        if (soundTokens.contains("basalt") && (materialTokens.contains("basalt") || materialKey.contains("basalt"))) score += 260;
        if (soundTokens.contains("lava") && (materialTokens.contains("lava") || materialKey.contains("lava"))) score += 260;
        if (soundTokens.contains("bubble") || soundTokens.contains("column")) {
            if (materialKey.contains("water") || materialKey.contains("magma")) score += 220;
        }
        if (soundTokens.contains("beehive") || soundTokens.contains("bee")) {
            if (materialKey.contains("bee") || materialKey.contains("honey")) score += 220;
        }
        if (soundTokens.contains("barrel") && materialKey.contains("barrel")) score += 260;
        if (soundTokens.contains("chorus") && (materialKey.contains("chorus") || materialKey.contains("end"))) score += 220;
        if ((soundTokens.contains("warped") || soundTokens.contains("crimson")) && (materialKey.contains("warped") || materialKey.contains("crimson"))) score += 220;
        if ((soundTokens.contains("pressure") || soundTokens.contains("plate")) && materialKey.contains("pressure")) score += 220;
        if (soundTokens.contains("beacon") && materialKey.contains("beacon")) score += 260;
        if (soundTokens.contains("cave") && materialKey.contains("cave")) score += 200;
        if ((soundTokens.contains("vine") || soundTokens.contains("vines")) && (materialKey.contains("vine") || materialKey.contains("berries"))) score += 210;
        if ((soundTokens.contains("music") && soundTokens.contains("disc")) || soundTokens.contains("record")) {
            if (materialKey.contains("music disc")) score += 260;
            if (materialKey.endsWith("cat") && !soundTokens.contains("cat")) score -= 220;
        }
        if (soundTokens.contains("chain")) {
            if (materialKey.equals("chain")) score += 320;
            if (materialKey.contains("command block")) score -= 220;
        }
        if (soundTokens.contains("deepslate")) {
            if (materialKey.contains("deepslate")) score += 280;
            if (materialKey.equals("stone")) score -= 180;
        }
        if (soundTokens.contains("trapdoor")) {
            if (materialKey.contains("trapdoor")) score += 140;
            if (materialKey.contains("log")) score -= 80;
        }
        if (soundTokens.contains("door")) {
            if (materialKey.contains("door")) score += 120;
            if (materialKey.contains("log")) score -= 80;
        }
        if (soundTokens.contains("wood")) {
            if (materialKey.contains("wood") || materialKey.contains("planks") || materialKey.contains("stem") || materialKey.contains("hyphae")) score += 120;
            if (materialKey.equals("oak log") || materialKey.equals("oak wood") || materialKey.contains("oak log")) score -= 60;
        }

        // Penalize generic, overused icons unless they actually fit.
        if (materialKey.equals("shield")) {
            if (!soundTokens.contains("shield") && !soundTokens.contains("guard")) {
                score -= 140;
            }
        }
        if (materialKey.equals("stone")) {
            if (!soundTokens.contains("stone") && !soundTokens.contains("rock") && !soundTokens.contains("cobble") && !soundTokens.contains("deepslate")) {
                score -= 120;
            }
        }
        if (materialKey.equals("campfire")) {
            if (!soundTokens.contains("campfire") && !soundTokens.contains("fire") && !soundTokens.contains("smoke")) {
                score -= 120;
            }
        }
        if (materialKey.equals("oak_log") || materialKey.equals("oak wood") || materialKey.contains("oak log")) {
            if (!soundTokens.contains("oak") && !soundTokens.contains("wood") && !soundTokens.contains("log")) {
                score -= 80;
            }
        }

        return score;
    }

    private Set<String> tokensOf(String raw) {
        String normalized = normalizeKey(raw);
        if (normalized.isBlank()) return Collections.emptySet();

        return Arrays.stream(normalized.split(" "))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String getSoundKeyStringSafe(Sound s) {
        try {
            NamespacedKey key = Registry.SOUNDS.getKey(s);
            if (key != null) {
                return key.toString();
            }
        } catch (Throwable ignored) {
        }
        return s.toString();
    }

    private String formatDisplayNameFromKey(String keyString) {
        String normalized = normalizeKey(keyString);
        if (normalized.isBlank()) return "Unknown";

        String[] parts = normalized.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private String normalizeKey(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim();

        int colon = s.indexOf(':');
        if (colon >= 0) {
            s = s.substring(colon + 1);
        }

        return s.replace('/', ' ')
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void buildExactOverrides() {
        // The safest rule is: exact names only, resolved lazily.
        putExact("azalea", "AZALEA", "AZALEA_LEAVES", "FLOWERING_AZALEA_LEAVES");
        putExact("lava", "LAVA_BUCKET");
        putExact("beehive", "BEEHIVE", "BEE_NEST", "HONEYCOMB");
        putExact("beacon", "BEACON");
        putExact("barrel", "BARREL");
        putExact("chorus fruit", "CHORUS_FRUIT");
        putExact("cave vines", "GLOW_BERRIES", "VINE", "MOSS_CARPET");
        putExact("warped forest", "WARPED_NYLIUM", "WARPED_STEM", "WARPED_WART_BLOCK");
        putExact("crimson forest", "CRIMSON_NYLIUM", "CRIMSON_STEM", "CRIMSON_FUNGUS");
        putExact("bamboo", "BAMBOO", "BAMBOO_BLOCK", "BAMBOO_PLANKS", "BAMBOO_MOSAIC");
        putExact("basalt", "BASALT", "POLISHED_BASALT");
        putExact("bubble column", "WATER_BUCKET", "MAGMA_CREAM");
        putExact("bubble", "WATER_BUCKET", "MAGMA_CREAM");
        putExact("column", "WATER_BUCKET", "MAGMA_CREAM");
        putExact("pressure plate", "STONE_PRESSURE_PLATE", "OAK_PRESSURE_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE");
        putExact("campfire", "CAMPFIRE", "SOUL_CAMPFIRE");
        putExact("soul campfire", "SOUL_CAMPFIRE");
        putExact("magma", "MAGMA_BLOCK", "MAGMA_CREAM");
        putExact("sculk", "SCULK", "SCULK_SENSOR", "SCULK_SHRIEKER");
        putExact("trial spawner", "TRIAL_SPAWNER");
        putExact("vault", "VAULT");
        putExact("breeze", "BREEZE_ROD");
        putExact("sniffer", "SNIFFER_EGG");
        putExact("bee", "HONEYCOMB", "BEE_NEST", "BEEHIVE");
        putExact("honey", "HONEY_BOTTLE", "HONEYCOMB", "HONEY_BLOCK");
        putExact("dripstone", "DRIPSTONE_BLOCK", "POINTED_DRIPSTONE");
        putExact("pointed dripstone", "POINTED_DRIPSTONE");
        putExact("amethyst", "AMETHYST_SHARD", "AMETHYST_BLOCK", "AMETHYST_CLUSTER");
        putExact("chorus plant", "CHORUS_PLANT");
        putExact("chain", "CHAIN");
        putExact("deepslate", "DEEPSLATE", "POLISHED_DEEPSLATE", "DEEPSLATE_TILES", "DEEPSLATE_BRICKS");
        putExact("bamboo trapdoor", "BAMBOO_TRAPDOOR");
        putExact("cherry trapdoor", "CHERRY_TRAPDOOR");
        putExact("cherry wood trapdoor", "CHERRY_TRAPDOOR");
        putExact("warped trapdoor", "WARPED_TRAPDOOR");
        putExact("crimson trapdoor", "CRIMSON_TRAPDOOR");
        putExact("warped door", "WARPED_DOOR");
        putExact("crimson door", "CRIMSON_DOOR");
        putExact("warped wood", "WARPED_HYPHAE", "WARPED_STEM");
        putExact("crimson wood", "CRIMSON_HYPHAE", "CRIMSON_STEM");
        putExact("end rod", "END_ROD");
        putExact("end stone", "END_STONE");
        putExact("ender pearl", "ENDER_PEARL");
        putExact("echo shard", "ECHO_SHARD");
    }

    private void putExact(String key, String... materials) {
        exactIconOverrides.put(normalizeKey(key), materials);
    }

    private void buildIconRules() {
        // High-confidence rules first.
        iconRules.add(rule(220, "AZALEA", "azalea", "flowering azalea", "azalea leaves"));
        iconRules.add(rule(220, "LAVA_BUCKET", "lava", "molten", "magma", "burning lava"));
        iconRules.add(rule(220, "BEEHIVE", "beehive", "bee nest", "hive"));
        iconRules.add(rule(220, "BEACON", "beacon", "beam", "signal"));
        iconRules.add(rule(220, "BARREL", "barrel", "container"));
        iconRules.add(rule(220, "CHORUS_FRUIT", "chorus fruit", "chorus"));
        iconRules.add(rule(220, "GLOW_BERRIES", "cave vines", "vine", "vines", "glow berries"));
        iconRules.add(rule(220, "BAMBOO", "bamboo"));
        iconRules.add(rule(220, "BAMBOO_BLOCK", "bamboo block", "bamboo wood"));
        iconRules.add(rule(220, "BAMBOO_PLANKS", "bamboo planks"));
        iconRules.add(rule(220, "BAMBOO_MOSAIC", "bamboo mosaic"));
        iconRules.add(rule(220, "BASALT", "basalt", "polished basalt"));
        iconRules.add(rule(220, "STONE_PRESSURE_PLATE", "pressure plate", "stone pressure plate"));
        iconRules.add(rule(220, "OAK_PRESSURE_PLATE", "wooden pressure plate", "oak pressure plate"));
        iconRules.add(rule(220, "SOUL_CAMPFIRE", "soul campfire"));
        iconRules.add(rule(220, "CAMPFIRE", "campfire"));
        iconRules.add(rule(220, "WARPED_NYLIUM", "warped forest", "warped", "warped nylium"));
        iconRules.add(rule(220, "CRIMSON_NYLIUM", "crimson forest", "crimson", "crimson nylium"));
        iconRules.add(rule(220, "SCULK_SENSOR", "sculk sensor", "sculk"));
        iconRules.add(rule(220, "TRIAL_SPAWNER", "trial spawner", "trial"));
        iconRules.add(rule(220, "VAULT", "vault", "ominous vault"));
        iconRules.add(rule(220, "BREEZE_ROD", "breeze"));
        iconRules.add(rule(220, "SNIFFER_EGG", "sniffer"));
        iconRules.add(rule(220, "ECHO_SHARD", "echo shard"));
        iconRules.add(rule(220, "DRIPSTONE_BLOCK", "dripstone", "pointed dripstone"));
        iconRules.add(rule(220, "WATER_BUCKET", "bubble column", "bubble", "column", "water", "splash"));
        iconRules.add(rule(220, "MAGMA_CREAM", "bubble column", "bubble", "column", "lava bubble"));

        // Strong family rules.
        iconRules.add(rule(180, "LAVA_BUCKET", "lava", "magma"));
        iconRules.add(rule(180, "MAGMA_BLOCK", "magma block", "magma"));
        iconRules.add(rule(180, "HONEYCOMB", "bee", "honey", "hive"));
        iconRules.add(rule(180, "BEACON", "beam", "beacon", "signal"));
        iconRules.add(rule(180, "BARREL", "barrel", "container"));
        iconRules.add(rule(180, "SHULKER_BOX", "shulker", "box", "container"));
        iconRules.add(rule(180, "CHEST", "chest", "loot"));
        iconRules.add(rule(180, "AMETHYST_CLUSTER", "amethyst", "crystal", "shard"));
        iconRules.add(rule(180, "GLOW_BERRIES", "berries", "vine", "cave vine"));
        iconRules.add(rule(180, "PITCHER_POD", "pitcher", "plant", "flower"));
        iconRules.add(rule(180, "TORCHFLOWER", "flower", "torchflower"));
        iconRules.add(rule(180, "COPPER_BLOCK", "copper", "oxid", "metal"));
        iconRules.add(rule(180, "OAK_LOG", "wood", "wooden", "log", "bark"));
        iconRules.add(rule(180, "STONE", "stone", "rock", "cobble", "deepslate"));
        iconRules.add(rule(180, "END_STONE", "end", "chorus", "ender"));
        iconRules.add(rule(180, "SOUL_TORCH", "soul", "spirit", "ghost"));
        iconRules.add(rule(180, "SEA_LANTERN", "ocean", "sea", "water", "marine"));
        iconRules.add(rule(180, "FIRE_CHARGE", "fire", "flame", "burn"));
        iconRules.add(rule(180, "REDSTONE", "redstone", "signal", "power"));
        iconRules.add(rule(180, "LEVER", "lever", "switch", "toggle"));

        // Useful fallback categories.
        iconRules.add(rule(150, "TNT", "explosion", "blast", "boom"));
        iconRules.add(rule(150, "ENDER_PEARL", "teleport", "ender", "enderman"));
        iconRules.add(rule(150, "BOW", "bow", "shoot", "arrow"));
        iconRules.add(rule(150, "IRON_SWORD", "attack", "hit", "damage", "sword"));
        iconRules.add(rule(150, "SHIELD", "shield", "guard"));
        iconRules.add(rule(150, "NOTE_BLOCK", "note", "instrument", "melody"));
        iconRules.add(rule(150, "FURNACE", "furnace", "smelt", "cook"));
        iconRules.add(rule(150, "BREWING_STAND", "brew", "potion", "alchemy"));
        iconRules.add(rule(150, "MINECART", "minecart", "rail", "track"));
    }

    private IconRule rule(int baseScore, String preferredMaterialName, String... terms) {
        return new IconRule(baseScore, preferredMaterialName, terms);
    }

    private Material resolveFirstItem(String... materialNames) {
        for (String name : materialNames) {
            if (name == null || name.isBlank()) continue;
            Material mat = Material.matchMaterial(name);
            if (mat != null && mat.isItem()) {
                return mat;
            }
        }
        return null;
    }

    private Material safeDisplayMaterial(Material material) {
        if (material != null && material.isItem()) {
            return material;
        }
        return Material.PAPER;
    }

    private static final class IconRule {
        final int baseScore;
        final String preferredMaterialName;
        final Set<String> terms;

        IconRule(int baseScore, String preferredMaterialName, String... terms) {
            this.baseScore = baseScore;
            this.preferredMaterialName = preferredMaterialName;
            this.terms = Arrays.stream(terms)
                    .map(s -> s == null ? "" : s.toLowerCase(Locale.ROOT).trim())
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        int score(String soundKey) {
            Set<String> soundTokens = Arrays.stream(soundKey.split(" "))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

            int score = 0;
            int matches = 0;

            for (String term : terms) {
                if (soundKey.equals(term)) {
                    score += 250;
                    matches++;
                    continue;
                }
                if (soundTokens.contains(term)) {
                    score += 150;
                    matches++;
                    continue;
                }
                if (soundKey.contains(term)) {
                    score += Math.min(80, 15 + term.length() * 3);
                    matches++;
                }
            }

            if (matches == 0) return 0;
            return baseScore + score + (matches * 20);
        }

        Material resolvePreferred() {
            return resolveFirstItemStatic(preferredMaterialName);
        }
    }

    // Static helper so IconRule can resolve safely.
    private static Material resolveFirstItemStatic(String... materialNames) {
        for (String name : materialNames) {
            if (name == null || name.isBlank()) continue;
            Material mat = Material.matchMaterial(name);
            if (mat != null && mat.isItem()) {
                return mat;
            }
        }
        return null;
    }

    private static final class IconMatch {
        final IconRule rule;
        final int score;

        IconMatch(IconRule rule, int score) {
            this.rule = rule;
            this.score = score;
        }
    }

    private static final class ScoredSound {
        private final Sound sound;
        private final int score;
        private final String key;

        ScoredSound(Sound sound, int score, String key) {
            this.sound = sound;
            this.score = score;
            this.key = key;
        }

        Sound sound() {
            return sound;
        }

        int score() {
            return score;
        }

        String key() {
            return key;
        }
    }

    private static final class SoundGuiHolder implements InventoryHolder {
        final String filter;
        final int page;

        SoundGuiHolder(String filter, int page) {
            this.filter = filter;
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        if (args.length == 0) {
            openSoundGui(p, "", 1);
            return true;
        }

        String filter;
        int page = 1;

        if (args.length > 1 && isInteger(args[args.length - 1])) {
            page = Integer.parseInt(args[args.length - 1]);
            filter = String.join(" ", Arrays.copyOf(args, args.length - 1));
        } else {
            filter = String.join(" ", args);
        }

        openSoundGui(p, filter, page);
        return true;
    }

    private boolean isInteger(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}