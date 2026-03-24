package com.crotaplague.torture.Files.ServerStorage.boxs;

import com.crotaplague.torture.Files.ServerStorage.Pair;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArenaBox {
    private final List<Location> mobLocations;
    private final List<Location> playerLocations;
    private final List<Location> hordeLocations;
    private Pair<List<Location>, List<Location>> displayChoices;

    public ArenaBox(List<Location> mobLocations, List<Location> playerLocations) {
        this.mobLocations = mobLocations;
        this.playerLocations = playerLocations;
        this.hordeLocations = new ArrayList<>();
    }
    public ArenaBox(List<Location> mobLocations, List<Location> playerLocations, List<Location> hordeLocations) {
        this.mobLocations = mobLocations;
        this.playerLocations = playerLocations;
        this.hordeLocations = hordeLocations;
    }
    public ArenaBox(List<Location> mobLocations, List<Location> playerLocations, Location... hordeLocations) {
        this.mobLocations = mobLocations;
        this.playerLocations = playerLocations;
        this.hordeLocations = List.of(hordeLocations);
    }
    public List<Location> getMobLocations() {
        return mobLocations;
    }

    public void setDisplayChoices(List<Location> a, List<Location> b){displayChoices = new Pair<>(a,b);}

    public List<Location> getChoicesA(){return displayChoices.getLeft();}

    public List<Location> getChoicesB(){return displayChoices.getRight();}

    public List<Location> getPlayerLocations() {return this.playerLocations;}

    /**
     * Returns a list containing the first two mob locations.
     *
     * @return a List of the first two Location objects in mobLocations.
     * @throws IndexOutOfBoundsException if there are fewer than 2 locations in mobLocations.
     */
    public List<Location> getSingleLocations() {
        if (mobLocations.size() < 2) {
            throw new IndexOutOfBoundsException("Not enough locations to get single locations (need at least 2).");
        }
        return mobLocations.subList(0, 2);
    }
    /**
     * Returns a list containing four mob locations starting from the third element.
     *
     * @return a List of Location objects from index 2 to 5 in mobLocations.
     * @throws IndexOutOfBoundsException if there are fewer than 6 locations in mobLocations.
     */
    public List<Location> getDoubleLocations() {
        if (mobLocations.size() < 6) {
            throw new IndexOutOfBoundsException("Not enough locations to get double locations (need at least 6).");
        }
        return mobLocations.subList(2, 6);
    }

    public List<Location> getHordeLocations() {
        return hordeLocations;
    }

}
