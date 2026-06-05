package com.crotaplague.torture.Files.ServerStorage.disguises;

public class OnlyDisguisePolicy implements DisguiseVisibilityPolicy{
    @Override
    public boolean canSeeDisguise(DisguiseView view) {
        return true;
    }

    @Override
    public boolean canSeeRealEntity(DisguiseView view) {
        return false;
    }
}
