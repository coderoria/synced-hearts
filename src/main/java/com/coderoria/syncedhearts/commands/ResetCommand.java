package com.coderoria.syncedhearts.commands;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

public class ResetCommand implements CommandExecutor {

  @Override
  public boolean onCommand(
    CommandSender sender,
    Command cmd,
    String label,
    String[] args
  ) {
    if (!(sender instanceof Player)) {
      return false;
    }

    World w = ((Player) sender).getLocation().getWorld();
    Runtime.getRuntime()
      .addShutdownHook(
        new Thread() {
          @Override
          public void run() {
            try {
              String baseName = w
                .getName()
                .replace("_the_end", "")
                .replace("_nether", "");
              File[] toDelete = Bukkit.getWorldContainer()
                .listFiles((dir, name) -> name.contains(baseName));

              for (File f : toDelete) {
                FileUtils.deleteDirectory(f);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      );
    Bukkit.shutdown();
    return true;
  }
}
