package com.crotaplague.torture.Files.ServerScriptService;

import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.jetbrains.annotations.NotNull;

public class Render extends MapRenderer {
    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        MapCursorCollection collect = mapCanvas.getCursors();
        byte x = 5;
        byte y = 25;
        byte d = 7;
        MapCursor cur = new MapCursor(x, y, d, MapCursor.Type.BANNER_BLACK, true);
        collect.addCursor(cur);
        mapCanvas.setCursors(collect);
    }

}
