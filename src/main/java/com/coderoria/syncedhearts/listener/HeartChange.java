package com.coderoria.syncedhearts.listener;

import com.coderoria.syncedhearts.Main;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class HeartChange implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRegeneration(EntityRegainHealthEvent ev) {
    if (!(ev.getEntity() instanceof Player)) {
      return;
    }

    Player p = (Player) ev.getEntity();
    if (p.isOp()) {
      return;
    }

    /* Bukkit.broadcast(
      Component.text("+" + ev.getAmount())
        .color(TextColor.color(0, 255, 0))
        .appendSpace()
        .append(Component.text("by " + p.getName()))
        .appendSpace()
        .append(Component.text(new Date().getTime()))
    ); */

    for (Player p2 : Bukkit.getOnlinePlayers()) {
      if (p2.equals(p) || p2.isOp()) continue;
      final double finalHealth = p.getHealth() + ev.getAmount();
      p2.setHealth(finalHealth > 20.0 ? 20.0 : finalHealth);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDamage(EntityDamageEvent ev) {
    if (!(ev.getEntity() instanceof Player)) {
      return;
    }

    Player p = (Player) ev.getEntity();
    if (p.isOp()) {
      return;
    }

    /* Bukkit.broadcast(
      Component.text("-" + ev.getFinalDamage())
        .color(TextColor.color(255, 0, 0))
        .appendSpace()
        .append(Component.text("by " + p.getName()))
        .appendSpace()
        .append(Component.text(new Date().getTime()))
    ); */

    for (Player p2 : Bukkit.getOnlinePlayers()) {
      if (p2.equals(p) || p2.isOp()) continue;
      final double finalHealth = p.getHealth() - ev.getFinalDamage();
      p2.setHealth(finalHealth < 0.0 ? 0.0 : finalHealth);
      p2.playSound(
        Sound.sound(Key.key("entity.player.hurt"), Sound.Source.PLAYER, 1f, 1f)
      );
      PacketContainer container = new PacketContainer(
        PacketType.Play.Server.HURT_ANIMATION
      );
      container.getIntegers().write(0, p2.getEntityId());
      container.getFloat().write(0, 0F);

      Main.plugin.protocolManager.sendServerPacket(p2, container);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    Player lPlayer = null;
    double lHealth = 20.0;
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.isOp()) continue;
      if (lPlayer == null || p.getHealth() < lHealth) {
        lPlayer = p;
        lHealth = p.getHealth();
      }
    }

    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.isOp()) continue;
      p.setHealth(lHealth);
    }
  }
}
