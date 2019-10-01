package cn.wode490390.nukkit.sneakattack;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import com.google.common.collect.Sets;
import java.util.Set;

public class SneakAttack extends PluginBase implements Listener {

    private final Set<Player> invisibles = Sets.newHashSet();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        int tick;
        String node = "food-level-reduce-every-x-ticks";
        try {
            tick = this.getConfig().getInt(node, 50);
        } catch (Exception ex) {
            tick = 50;
            this.logLoadException(node, ex);
        }
        if (tick > 0) {
            new NukkitRunnable() {
                @Override
                public void run() {
                    Sets.newHashSet(invisibles).parallelStream().map(player -> player.getFoodData()).forEach(data -> {
                        int level = data.getLevel();
                        if (level > 0) {
                            data.setLevel(--level);
                        }
                    });
                }
            }.runTaskTimerAsynchronously(this, 0, tick);
        }
        try {
            new MetricsLite(this);
        } catch (Exception ignore) {

        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("sneakattack.invisible")) {
            return;
        }
        if (event.isSneaking()) {
            if (!player.hasPermission("sneakattack.unlimited")) {
                this.invisibles.add(player);
            }
            player.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true);
            player.setNameTagVisible(false);
        } else {
            this.invisibles.remove(player);
            player.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, false);
            player.setNameTagVisible(true);
        }
    }

    private void logLoadException(String node, Exception ex) {
        this.getLogger().alert("An error occurred while reading the configuration '" + node + "'. Use the default value.", ex);
    }
}
