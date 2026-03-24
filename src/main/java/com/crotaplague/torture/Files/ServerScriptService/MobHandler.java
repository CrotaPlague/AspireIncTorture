package com.crotaplague.torture.Files.ServerScriptService;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMob;

public class MobHandler {
    public static void moveTo(org.bukkit.entity.Mob mob, double x, double y, double z){
        moveTo(mob, x, y, z, 1);
    }
    public static void moveTo(org.bukkit.entity.Mob mob, double x, double y, double z, double speed){
        Mob m = ((CraftMob) mob).getHandle();
        m.getNavigation().moveTo(x, y, z, speed);
    }
    public static void moveTo(org.bukkit.entity.Mob mob, org.bukkit.Location loc){
        moveTo(mob, loc.getX(), loc.getY(), loc.getZ());
    }
}
