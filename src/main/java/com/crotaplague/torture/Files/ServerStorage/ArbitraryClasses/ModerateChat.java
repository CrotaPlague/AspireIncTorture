package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

import com.crotaplague.torture.Torture;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerChatEvent;
import org.jline.utils.Levenshtein;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;



public class ModerateChat {
    private static final Pattern WORD_SPLIT = Pattern.compile("\\s+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{N}]+");

    private static final Map<Character, Character> LEET_MAP = ModerateChat.buildLeetMap();
    private final Set<String> bannedExact;
    private final Set<String> whitelist;
    private final Map<String, Pattern> separatedPatterns;
    private final Config<ModerationConfig> config;
    public ModerateChat(Config<ModerationConfig> config) {
        this.config = config;

        this.bannedExact = new HashSet<>();
        this.whitelist = new HashSet<>();
        this.separatedPatterns = new HashMap<>();

        for (String banned : config.get().getBannedWords()) {
            String norm = normalizeWord(banned);
            if (!norm.isEmpty()) {
                bannedExact.add(norm);
                separatedPatterns.put(norm, buildSeparatedPattern(norm));
            }
        }

        for (String w : defaultWhitelist()) {
            String norm = normalizeWord(w);
            if (!norm.isEmpty()) {
                whitelist.add(norm);
            }
        }
    }

    private void block(PlayerChatEvent event, String matched) {
        Torture.LOGGER.log(Level.INFO, "Blocked chat content match: " + matched);
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Message contained blocked text."));
    }

    private boolean looksSuspicious(String token, String banned) {
        if (token.equals(banned)) return true;

        int bl = banned.length();
        int tl = token.length();

        if (bl < 4) return false;

        // Keep fuzzy matching conservative to avoid normal chat false positives.
        if (Math.abs(tl - bl) > 2) return false;

        int threshold;
        if (bl <= 5) threshold = 0;
        else if (bl <= 8) threshold = 1;
        else threshold = 2;

        if (Levenshtein.distance(token, banned) <= threshold) return true;
        if (hasAdjacentSwap(token, banned)) return true;

        // Only allow a very tight substring match, and only for longer banned terms.
        if (bl >= 5 && tl <= bl + 1 && token.contains(banned)) return true;

        return false;
    }

    private static String normalizeMessage(String input) {
        if (input == null) return "";

        String s = Normalizer.normalize(input, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");

        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            Character mapped = LEET_MAP.get(c);
            if (mapped != null) {
                out.append(mapped);
            } else if (Character.isLetterOrDigit(c)) {
                out.append(c);
            } else {
                out.append(' ');
            }
        }

        return out.toString()
                .replaceAll("([\\p{L}\\p{N}])\\1{2,}", "$1$1")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalizeWord(String input) {
        if (input == null) return "";

        String s = Normalizer.normalize(input, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");

        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            Character mapped = LEET_MAP.get(c);
            if (mapped != null) {
                out.append(mapped);
            } else if (Character.isLetterOrDigit(c)) {
                out.append(c);
            }
        }

        return compact(out.toString());
    }

    private static String compact(String s) {
        if (s == null || s.isEmpty()) return "";
        return NON_ALNUM.matcher(s).replaceAll("");
    }

    private static Pattern buildSeparatedPattern(String banned) {
        StringBuilder regex = new StringBuilder();
        regex.append("(?<![\\p{L}\\p{N}])");
        for (int i = 0; i < banned.length(); i++) {
            regex.append(Pattern.quote(String.valueOf(banned.charAt(i))));
            if (i < banned.length() - 1) {
                regex.append("[\\p{Punct}\\s_]*");
            }
        }
        regex.append("(?![\\p{L}\\p{N}])");
        return Pattern.compile(regex.toString());
    }

    private static boolean hasAdjacentSwap(String a, String b) {
        if (a.length() != b.length()) return false;

        int first = -1;
        int second = -1;
        int diffs = 0;

        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                if (diffs == 0) first = i;
                else if (diffs == 1) second = i;
                diffs++;
                if (diffs > 2) return false;
            }
        }

        if (diffs != 2) return false;
        return a.charAt(first) == b.charAt(second) && a.charAt(second) == b.charAt(first);
    }

    private static Map<Character, Character> buildLeetMap() {
        Map<Character, Character> m = new HashMap<>();
        m.put('4', 'a');
        m.put('@', 'a');
        m.put('3', 'e');
        m.put('€', 'e');
        m.put('1', 'i');
        m.put('!', 'i');
        m.put('|', 'i');
        m.put('0', 'o');
        m.put('5', 's');
        m.put('$', 's');
        m.put('7', 't');
        m.put('+', 't');
        m.put('8', 'b');
        m.put('9', 'g');
        m.put('2', 'z');
        return m;
    }

    private static Set<String> defaultWhitelist() {
        return Set.of(
                "assistant", "assistance", "assist", "class", "classic", "classroom",
                "glass", "grass", "pass", "passage", "passion", "passive",
                "compass", "compassion", "compassionate",
                "mass", "message", "massive",
                "session", "mission", "permission", "emission", "dismiss",
                "fashion", "station", "nation", "transition", "transitioning",
                "action", "faction", "reaction", "fraction",
                "raccoon", "cocoon",
                "niger", "nigeria", "nigerian", "nigerians",
                "singer", "finger", "minger", "ringer",
                "chinkapin", "chinook",
                "transit", "transmission", "transaction", "transport", "translator", "transient",
                "maintenance", "sustain", "sustainable",
                "scunthorpe", "shiitake"
        );
    }
}


class Config<T> {

    private final File file;
    private final Class<T> type;
    private final ObjectMapper mapper;

    private T data;

    public Config(File file, Class<T> type) {
        this.file = file;
        this.type = type;
        this.mapper = new ObjectMapper();
    }

    /**
     * Loads config from disk.
     * If file doesn't exist, creates default instance.
     */
    public void load() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();

                // create default instance
                data = type.getDeclaredConstructor().newInstance();
                save();
                return;
            }

            data = mapper.readValue(file, type);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + file.getName(), e);
        }
    }

    /**
     * Saves current config to disk.
     */
    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + file.getName(), e);
        }
    }

    /**
     * Reloads from disk (overwrites current memory).
     */
    public void reload() {
        load();
    }

    /**
     * Gets the live config object.
     */
    public T get() {
        return data;
    }

    /**
     * Replaces config in memory (not auto-saved).
     */
    public void set(T data) {
        this.data = data;
    }

    public File getFile() {
        return file;
    }
}