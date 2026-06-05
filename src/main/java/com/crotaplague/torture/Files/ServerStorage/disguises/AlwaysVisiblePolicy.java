package com.crotaplague.torture.Files.ServerStorage.disguises;

public final class AlwaysVisiblePolicy implements DisguiseVisibilityPolicy {
    @Override
    public boolean canSeeDisguise(DisguiseView view) {
        return true;
    }

    @Override
    public boolean canSeeRealEntity(DisguiseView view) {
        return true;
    }
}
