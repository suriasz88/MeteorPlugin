package pl.twojnick.meteor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("meteor") != null) {
            getCommand("meteor").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy!");
            return true;
        }

        if (!player.hasPermission("meteor.admin")) {
            player.sendMessage("§cNie masz uprawnień!");
            return true;
        }

        // Szukanie bloku, na który spogląda gracz (max 250 kratek)
        Block targetBlock = player.getTargetBlockExact(250);
        if (targetBlock == null) {
            player.sendMessage("§cPatrzysz za daleko lub w niebo! Celuj w ziemię/bloki.");
            return true;
        }

        Location targetLoc = targetBlock.getLocation().add(0.5, 1.0, 0.5);

        // Wyznaczenie punktu startowego na niebie (50 kratek wyżej i lekko przesunięty)
        Location spawnLoc = targetLoc.clone().add(-15, 50, -15);

        // Wektor kierunku z nieba w stronę wskazanego celu
        Vector direction = targetLoc.toVector().subtract(spawnLoc.toVector()).normalize().multiply(1.5);

        // Przywołanie meteoru (Fireball)
        LargeFireball meteor = player.getWorld().spawn(spawnLoc, LargeFireball.class);
        meteor.setShooter(player);
        meteor.setDirection(direction);
        meteor.setYield(0F);
        meteor.setIsIncendiary(false);

        // Efekt gęstego dymu i ognia podczas spadania
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!meteor.isValid() || meteor.isDead()) {
                    cancel();
                    return;
                }
                Location loc = meteor.getLocation();
                World world = loc.getWorld();
                if (world != null) {
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 80, 1.2, 1.2, 1.2, 0.05);
                    world.spawnParticle(Particle.SMOKE, loc, 50, 1.8, 1.8, 1.8, 0.1);
                    world.spawnParticle(Particle.FLAME, loc, 30, 0.8, 0.8, 0.8, 0.02);
                }
            }
        }.runTaskTimer(this, 0L, 1L);

        player.sendMessage("§c§l[!] Wezwano Meteor w cel!");
        return true;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof LargeFireball meteor)) return;

        Location hitLoc = event.getEntity().getLocation();
        World world = hitLoc.getWorld();
        if (world == null) return;

        world.playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 10.0f, 0.5f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 20, 2.0, 2.0, 2.0);

        // Krater 200x200 x 300 głębokości (w tle bez lagów)
        createGiantCraterAsync(hitLoc);

        // Podpalanie wokół w promieniu 400 kratek
        igniteArea(hitLoc, 400);
    }

    private void createGiantCraterAsync(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int startX = center.getBlockX() - 100;
        int endX = center.getBlockX() + 100;

        int startZ = center.getBlockZ() - 100;
        int endZ = center.getBlockZ() + 100;

        int startY = center.getBlockY();
        int endY = Math.max(world.getMinHeight(), startY - 300);

        new BukkitRunnable() {
            int currentY = startY;

            @Override
            public void run() {
                for (int i = 0; i < 5; i++) { // 5 warstw Y na tick
                    if (currentY < endY) {
                        cancel();
                        return;
                    }

                    for (int x = startX; x <= endX; x++) {
                        for (int z = startZ; z <= endZ; z++) {
                            Block block = world.getBlockAt(x, currentY, z);
                            if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                                block.setType(Material.AIR, false);
                            }
                        }
                    }
                    currentY--;
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private void igniteArea(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 3000; i++) {
                    int rx = centerX + random.nextInt(radius * 2) - radius;
                    int rz = centerZ + random.nextInt(radius * 2) - radius;
                    int ry = centerY + random.nextInt(50) - 25;

                    if (ry < world.getMinHeight() || ry > world.getMaxHeight()) continue;

                    Block target = world.getBlockAt(rx, ry, rz);
                    Block below = world.getBlockAt(rx, ry - 1, rz);

                    if (target.getType() == Material.AIR && below.getType().isSolid()) {
                        target.setType(Material.FIRE, false);
                    }
                }
            }
        }.runTaskAsynchronously(this);
    }
}