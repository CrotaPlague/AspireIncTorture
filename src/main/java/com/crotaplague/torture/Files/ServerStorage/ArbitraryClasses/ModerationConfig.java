package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

import java.util.ArrayList;
import java.util.List;

public class ModerationConfig {
    private boolean enabled = true;
    private boolean fuzzyMatching = true;

    private List<String> bannedWords = new ArrayList<>();
    private List<String> whitelistWords = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public void setFuzzyMatching(boolean fuzzyMatching) {
        this.fuzzyMatching = fuzzyMatching;
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }

    public void setBannedWords(List<String> bannedWords) {
        this.bannedWords = bannedWords;
    }

    public List<String> getWhitelistWords() {
        return whitelistWords;
    }

    public void setWhitelistWords(List<String> whitelistWords) {
        this.whitelistWords = whitelistWords;
    }
}