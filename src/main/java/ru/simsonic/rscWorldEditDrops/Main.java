package ru.simsonic.rscWorldEditDrops;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static final String CHAT_PREFIX = "§8[rscWorldEditDrops] §7";

    private static final int GRANULATION = 10;

    private Logger logger = getLogger();

    @Override
    public void onLoad() {
        logger.info("[rscWorldEditDrops] Plugin has been loaded.");
    }

    @Override
    public void onEnable() {
        // Done
        logger.info("[rscWorldEditDrops] Plugin has been successfully enabled.");
    }

    @Override
    public void onDisable() {
        logger.info("[rscWorldEditDrops] Plugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "rscwed":
                processCommandHub(sender, args);
                return true;
            case "/drop":
                if (sender instanceof Player) {
                    if (sender.hasPermission("rscwed.drop")) {
                        processWorldEditDrop((Player) sender);
                    } else {
                        sender.sendMessage(CHAT_PREFIX + "§cNot enough permissions.");
                    }
                } else {
                    sender.sendMessage(CHAT_PREFIX + "§cCommand cannot be ran from console.");
                }
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Command: /rscwed ...
     */
    private void processCommandHub(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(CHAT_PREFIX + "rscWorldEditDrops " + getDescription().getVersion());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("rscwed.admin")) {
                    reloadConfig();
                    getPluginLoader().disablePlugin(this);
                    getPluginLoader().enablePlugin(this);
                    sender.sendMessage(CHAT_PREFIX + "Done.");
                }
                sender.sendMessage("§cNot enough permissions.");
                return;
            case "help":
            default:
                Arrays.stream(new String[]{
                        "Usage:",
                        "§6//drop §7— drop all blocks in your current WE selection.",
                        "§6/rscwed help §7— show this help.",
                        "§6/rscwed reload §7— disable and enable plugin again.",
                }).forEach(s -> sender.sendMessage(CHAT_PREFIX + s));
                break;
        }
    }

    /**
     * Command: //drop
     */
    private void processWorldEditDrop(Player player) {
        Plugin pluginWE = getServer().getPluginManager().getPlugin("WorldEdit");
        if (pluginWE instanceof WorldEditPlugin && pluginWE.isEnabled()) {
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) pluginWE;
            Selection selection = worldEditPlugin.getSelection(player);
            if (selection != null) {
                int yLen = dropBlocksFromSelection(pluginWE, selection);
                player.sendMessage("§aOperation will be finished in ~" + yLen / GRANULATION + " seconds");
            } else {
                player.sendMessage("§cWorldEdit selection is empty.");
            }
        } else {
            player.sendMessage("§cWorldEdit is absent or disabled.");
        }
    }

    private int dropBlocksFromSelection(Plugin pluginWE, Selection selection) {
        Location pos1 = selection.getMinimumPoint();
        Location pos2 = selection.getMaximumPoint();
        int xLen = pos2.getBlockX() - pos1.getBlockX() + 1;
        int yLen = pos2.getBlockY() - pos1.getBlockY() + 1;
        int zLen = pos2.getBlockZ() - pos1.getBlockZ() + 1;
        for (int y = 0; y < yLen; y += 1) {
            Location startLoc = pos1.clone().add(0, y, 0);
            getServer().getScheduler().runTaskLater(pluginWE, () -> {
                for (int z = 0; z < zLen; z += 1) {
                    for (int x = 0; x < xLen; x += 1) {
                        startLoc.clone().add(x, 0, z).getBlock().breakNaturally();
                    }
                }
            }, GRANULATION * y);
        }
        return yLen;
    }
}
