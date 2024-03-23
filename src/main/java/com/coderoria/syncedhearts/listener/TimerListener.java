package com.coderoria.syncedhearts.listener;

import com.coderoria.syncedhearts.Main;
import java.util.Date;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class TimerListener implements Listener {

  MovementListener listener = null;
  BukkitTask timerTask = null;
  long startTime = 0;
  long endTime = 0;
  boolean ended = false;
  NamespacedKey timerKey = new NamespacedKey(Main.plugin, "speedrunTime");
  DeathListener deathListener = new DeathListener();

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    if (listener != null) {
      return;
    }
    //TODO: do not count OP players
    if (Bukkit.getOnlinePlayers().size() < 2) {
      return;
    }
    listener = new MovementListener();
    Bukkit.getPluginManager().registerEvents(listener, Main.plugin);
    Bukkit.getPluginManager().registerEvents(deathListener, Main.plugin);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    //TODO: do not count OP players
    if (Bukkit.getOnlinePlayers().size() - 1 >= 2) {
      return;
    }

    long passedTime = getSpeedrunTime() + (new Date().getTime() - startTime);
    saveSpeedrunTime(passedTime);

    if (timerTask != null) {
      timerTask.cancel();
      timerTask = null;
    }
    listener = null;
    HandlerList.unregisterAll(deathListener);
  }

  long getSpeedrunTime() {
    return Bukkit.getWorlds()
      .get(0)
      .getPersistentDataContainer()
      .getOrDefault(timerKey, PersistentDataType.LONG, 0L);
  }

  void saveSpeedrunTime(long value) {
    Bukkit.getWorlds()
      .get(0)
      .getPersistentDataContainer()
      .set(timerKey, PersistentDataType.LONG, value);
  }

  public class MovementListener implements Listener {

    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent ev) {
      HandlerList.unregisterAll(this);
      startTime = new Date().getTime();

      if (timerTask != null) {
        return;
      }
      final long oldSpeedrunTime = getSpeedrunTime();
      timerTask = Bukkit.getScheduler()
        .runTaskTimer(
          Main.plugin,
          () -> {
            long passedTime = new Date().getTime() - startTime;
            for (Player p : Bukkit.getOnlinePlayers()) {
              if (!ended) {
                p.sendActionBar(
                  Component.text(
                    DurationFormatUtils.formatDuration(
                      passedTime + oldSpeedrunTime,
                      "HH:mm:ss",
                      true
                    )
                  ).color(TextColor.color(255, 0, 217))
                );
              } else {
                p.sendActionBar(
                  Component.text(
                    DurationFormatUtils.formatDurationHMS(endTime)
                  ).color(TextColor.color(0, 76, 255))
                );
              }
            }
          },
          0,
          10
        );
    }
  }

  public class DeathListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent ev) {
      if (
        ev.getEntityType() != EntityType.ENDER_DRAGON &&
        ev.getEntityType() != EntityType.PLAYER
      ) {
        return;
      }

      if (ev.getEntityType() == EntityType.PLAYER) {
        Player p = (Player) ev.getEntity();
        //TODO: Check if OP
        /* if(p.isOp()) {
          return;
        } */
      }

      endTime = getSpeedrunTime() + (new Date().getTime() - startTime);
      ended = true;
    }
  }
}
