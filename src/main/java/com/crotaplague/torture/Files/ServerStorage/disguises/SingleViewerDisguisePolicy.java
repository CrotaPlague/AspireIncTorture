package com.crotaplague.torture.Files.ServerStorage.disguises;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Only one player can see the disguise.
 * Nobody can see the real entity.
 */
public class SingleViewerDisguisePolicy implements DisguiseVisibilityPolicy {

    private final UUID allowedViewerId;

    public SingleViewerDisguisePolicy(Player allowedViewer) {
        this.allowedViewerId = allowedViewer.getUniqueId();
    }

    @Override
    public boolean canSeeDisguise(DisguiseView view) {
        return view.viewer().getUniqueId().equals(allowedViewerId);
    }

    @Override
    public boolean canSeeRealEntity(DisguiseView view) {
        return false;
    }
}