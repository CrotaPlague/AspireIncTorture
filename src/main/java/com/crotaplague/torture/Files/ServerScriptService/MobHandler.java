package com.crotaplague.torture.Files.ServerScriptService;
import com.crotaplague.torture.Torture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MobHandler {
    public static BukkitTask moveTo(org.bukkit.entity.Mob mob, double x, double y, double z) {
        return moveTo(mob, x, y, z, 0.6, 0.1);
    }

    public static BukkitTask moveTo(org.bukkit.entity.Mob mob, double x, double y, double z, double speed) {
        return moveTo(mob, x, y, z, speed, 0.1);
    }

    public static BukkitTask moveTo(org.bukkit.entity.Mob mob, double x, double y, double z, double speed, double stopDistance) {
        Mob m = ((CraftMob) mob).getHandle();
        Location target = new Location(mob.getWorld(), x, y, z);

        return new BukkitRunnable() {
            @Override
            public void run() {
                // Keep pushing navigation
                m.getNavigation().moveTo(x, y, z, speed);

                // Stop condition
                if (mob.getLocation().distance(target) <= stopDistance) {
                    m.getNavigation().stop(); // stop pathfinding
                    m.setNoAi(true);
                    cancel();
                }

                // Safety: mob died or got removed
                if (!mob.isValid() || mob.isDead()) {
                    cancel();
                }
            }
        }.runTaskTimer(Torture.plugin, 1L, 1L);
    }

    public static BukkitTask moveTo(org.bukkit.entity.Mob mob, org.bukkit.Location loc) {
        return moveTo(mob, loc.getX(), loc.getY(), loc.getZ());
    }
}
