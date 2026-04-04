package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

import org.jline.utils.Levenshtein;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public final class ChatModerationService {
    private static final Pattern WORD_SPLIT = Pattern.compile("\\s+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{N}]+");

    private static final Map<Character, Character> LEET_MAP = buildLeetMap();

    private final Set<String> bannedExact = new HashSet<>();
    private final Set<String> whitelist = new HashSet<>();
    private final Map<String, Pattern> separatedPatterns = new HashMap<>();

    private boolean enabled;
    private boolean fuzzyMatching;

    public ChatModerationService(ModerationConfig config) {
        reload(config);
    }

    public void reload(ModerationConfig config) {
        this.enabled = config != null && config.isEnabled();
        this.fuzzyMatching = config == null || config.isFuzzyMatching();

        bannedExact.clear();
        whitelist.clear();
        separatedPatterns.clear();

        if (config != null) {
            for (String banned : config.getBannedWords()) {
                String norm = normalizeWord(banned);
                if (!norm.isEmpty()) {
                    bannedExact.add(norm);
                    separatedPatterns.put(norm, buildSeparatedPattern(norm));
                }
            }

            for (String word : config.getWhitelistWords()) {
                String norm = normalizeWord(word);
                if (!norm.isEmpty()) {
                    whitelist.add(norm);
                }
            }
        }

        for (String word : defaultWhitelist()) {
            String norm = normalizeWord(word);
            if (!norm.isEmpty()) {
                whitelist.add(norm);
            }
        }
    }

    public ModerationVerdict verify(String rawMessage) {
        if (!enabled) return allow();
        if (rawMessage == null || rawMessage.isBlank()) return allow();

        String normalized = normalizeMessage(rawMessage);
        if (normalized.isBlank()) return allow();

        String compactMessage = compact(normalized);
        if (compactMessage.isBlank()) return allow();

        for (String banned : bannedExact) {
            if (whitelist.contains(banned)) continue;

            Pattern p = separatedPatterns.get(banned);
            if (p != null && p.matcher(normalized).find()) {
                return block(banned, "separated-obfuscation", 95);
            }
        }

        String[] tokens = WORD_SPLIT.split(normalized);
        for (String token : tokens) {
            if (token.isBlank()) continue;

            String t = compact(token);
            if (t.isEmpty()) continue;
            if (whitelist.contains(t)) continue;

            if (bannedExact.contains(t)) {
                return block(t, "exact-token", 100);
            }

            if (!fuzzyMatching) continue;

            for (String banned : bannedExact) {
                if (whitelist.contains(banned)) continue;

                int score = matchScore(t, banned);
                if (score >= 80) {
                    return block(banned, "fuzzy", score);
                }
            }
        }

        return allow();
    }

    private ModerationVerdict allow() {
        return new ModerationVerdict(false, "", "", 0);
    }

    private ModerationVerdict block(String matched, String reason, int score) {
        return new ModerationVerdict(true, matched, reason, score);
    }

    private int matchScore(String token, String banned) {
        if (token.equals(banned)) return 100;

        int bl = banned.length();
        int tl = token.length();

        if (bl < 4) return 0;
        if (Math.abs(tl - bl) > 2) return 0;

        int dist = Levenshtein.distance(token, banned);

        if (dist == 0) return 100;
        if (dist == 1) return 82;

        // Allow one extra edit only for longer terms, to catch obvious evasions
        if (dist == 2 && bl >= 9) return 80;

        if (hasAdjacentSwap(token, banned)) return 84;

        if (bl >= 5 && tl <= bl + 1 && token.contains(banned)) {
            return 81;
        }

        return 0;
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

    private static List<String> defaultWhitelist() {
        return Arrays.asList(
                "assistant", "assistance", "assist", "assisted", "assisting", "assistantship",
                "class", "classic", "classics", "classroom", "classmate", "classmates", "classwork", "classy",
                "glass", "glasses", "glassware", "glasshouse", "grass", "grassland", "grasshopper", "grassy",
                "pass", "passed", "passing", "passage", "passages", "passenger", "passengers",
                "passion", "passionate", "passively", "passive", "passport", "password", "passcode", "passporting",
                "compass", "compasses", "compassion", "compassionate", "compassionately", "compassioner",
                "mass", "masses", "massive", "massage", "massager", "mess", "message", "messages", "messenger",
                "dismiss", "dismissal", "dismissed", "dismisses", "dismissive",
                "session", "sessions", "mission", "missions", "missionary", "permission", "permissions", "permissible",
                "emission", "emissions", "remission", "admission", "admissions", "submission", "submissions",
                "commission", "commissions", "commissioner",
                "fashion", "fashions", "fashionable", "fashioning",
                "station", "stations", "nation", "nations", "national", "nationality", "nationalities",
                "transition", "transitions", "transitional", "transitioning",
                "transit", "transits", "transient", "transience",
                "translator", "translators", "translation", "translations", "transcribe", "transcription",
                "transport", "transports", "transportation", "transported", "transporting",
                "transaction", "transactions", "transmission", "transmissions",
                "maintenance", "maintain", "maintained", "maintaining", "maintainer",
                "sustain", "sustained", "sustaining", "sustainable", "sustainability",
                "raccoon", "cocoon", "chinkapin", "chinook",
                "niger", "nigeria", "nigerian", "nigerians",
                "singer", "singers", "finger", "fingers", "ringer", "ringers",
                "scunthorpe", "shiitake", "shitake",
                "analyst", "analysis", "analog", "analogous", "analogue",
                "assignment", "assign", "assigned", "assigning",
                "account", "accounts", "accounting", "accountant",
                "apple", "apply", "applied", "application", "applications",
                "banana", "bandana", "cabana",
                "cancellation", "cancel", "cancelled", "canceled",
                "capacity", "capital", "capitol", "captain",
                "calendar", "calibrate", "calibration",
                "candidate", "candle", "candid", "canal",
                "channel", "chancellor", "character", "characteristic", "characteristics",
                "computer", "computing", "compilation", "compiler", "compose", "composite",
                "congress", "congressional", "congratulate", "congratulations",
                "discussion", "discuss", "discussion", "discretion", "discreet",
                "expression", "express", "expressive", "expressway",
                "foundation", "foundational", "function", "functional", "functionality",
                "generation", "generous", "general", "generalize",
                "important", "importance", "impartial", "impression", "impressive",
                "instructor", "instruction", "instructions", "instrument", "integration",
                "interesting", "interest", "interested", "interstate", "international",
                "language", "languages", "linguistic", "literature",
                "operation", "operator", "optical", "optimistic",
                "organization", "organize", "organized", "organizing",
                "presentation", "present", "presented", "presenting",
                "question", "questions", "questionnaire",
                "registration", "register", "registered", "registering",
                "representation", "represent", "representative", "representatives",
                "resource", "resources", "response", "responsive", "responsible",
                "security", "secure", "secured", "securing",
                "simulation", "simulate", "simulated",
                "statistics", "statistical", "statistician", "standard", "standardize",
                "strategy", "strategic", "structure", "structured",
                "student", "students", "study", "studies", "studying",
                "support", "supporter", "supportive", "supported", "supporting",
                "technology", "technician", "technical", "technically",
                "transportation", "transparency", "transparent",
                "university", "universal", "universe", "utility", "utilities",
                "value", "valuable", "variety", "variable", "variables",
                "welcome", "wellness", "workshop", "worksheet", "workflow"
        );
    }
}
