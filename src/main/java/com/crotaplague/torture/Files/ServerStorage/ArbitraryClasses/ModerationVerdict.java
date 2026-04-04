package com.crotaplague.torture.Files.ServerStorage.ArbitraryClasses;

public record ModerationVerdict(boolean blocked, String matched, String reason, int score) {}