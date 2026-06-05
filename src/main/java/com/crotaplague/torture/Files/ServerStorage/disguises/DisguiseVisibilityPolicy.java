package com.crotaplague.torture.Files.ServerStorage.disguises;

/**
 * Decides whether a viewer should see the disguise and the real entity.
 */
public interface DisguiseVisibilityPolicy {

    /**
     * Checks whether the viewer should see the disguise.
     *
     * @param view the current disguise view
     * @return true if the disguise should be shown
     */
    boolean canSeeDisguise(DisguiseView view);

    /**
     * Checks whether the viewer should see the real entity.
     *
     * @param view the current disguise view
     * @return true if the real entity should be shown
     */
    boolean canSeeRealEntity(DisguiseView view);
}