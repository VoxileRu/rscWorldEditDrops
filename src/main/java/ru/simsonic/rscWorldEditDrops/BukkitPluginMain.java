package ru.simsonic.rscWorldEditDrops;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;
import ru.simsonic.utilities.CommandAnswerException;
import ru.simsonic.utilities.LanguageUtility;

public final class BukkitPluginMain extends JavaPlugin
{
	public  static final Logger consoleLog = Bukkit.getLogger();
	private static final String chatPrefix = "{_DS}[rscWorldEditDrops] {_LS}";
	private static final int granulation = 10;
	private MetricsLite metrics = null;
	@Override
	public void onLoad()
	{
		consoleLog.log(Level.INFO, "[rscWorldEditDrops] Plugin has been loaded.");
	}
	@Override
	public void onEnable()
	{
		// Metrics
		try
		{
			metrics = new MetricsLite(this);
			metrics.start();
			consoleLog.info("[rscWorldEditDrops] Metrics enabled.");
		} catch(IOException ex) {
			consoleLog.log(Level.INFO, "[rscWorldEditDrops][Metrics] Exception:\n{0}", ex.getLocalizedMessage());
		}
		// Done
		consoleLog.log(Level.INFO, "[rscWorldEditDrops] Plugin has been successfully enabled.");
	}
	@Override
	public void onDisable()
	{
		metrics = null;
		consoleLog.info("[rscWorldEditDrops] Plugin has been disabled.");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		try
		{
			switch(label.toLowerCase())
			{
				case "rscwed":
					processCommandHub(sender, args);
					break;
				case "/drop":
					if(sender instanceof Player)
					{
						if(sender.hasPermission("rscwed.drop"))
							processWorldEditDrop((Player)sender);
						else
							throw new CommandAnswerException("{_LR}Not enough permissions.");
					} else
						throw new CommandAnswerException("{_LR}Command cannot be ran from console.");
					break;
			}
		} catch(CommandAnswerException ex) {
			for(String answer : ex.getMessageArray())
				sender.sendMessage(LanguageUtility.processStringStatic(chatPrefix + answer));
		}
		return true;
	}
	private void processCommandHub(CommandSender sender, String[] args) throws CommandAnswerException
	{
		if(args.length == 0)
			throw new CommandAnswerException("{_LG}rscWorldEditDrops {_DG}" + getDescription().getVersion() + "{_LG} by SimSonic.");
		switch(args[0].toLowerCase())
		{
			case "reload":
				if(sender.hasPermission("rscwed.admin"))
				{
					reloadConfig();
					getPluginLoader().disablePlugin(this);
					getPluginLoader().enablePlugin(this);
					throw new CommandAnswerException("Done.");
				}
				throw new CommandAnswerException("{_LR}Not enough permissions.");
			case "help":
			default:
				throw new CommandAnswerException(new String[]
				{
					"Usage:",
					"{GOLD}//drop {_LS}— drop all blocks in your current WE selection.",
					"{GOLD}/rscwed help {_LS}— show this help.",
					"{GOLD}/rscwed reload {_LS}— disable and enable plugin again.",
				});
		}
	}
	private void processWorldEditDrop(Player player) throws CommandAnswerException
	{
		final org.bukkit.plugin.Plugin pluginWE = getServer().getPluginManager().getPlugin("WorldEdit");
		if(pluginWE != null && pluginWE instanceof WorldEditPlugin)
		{
			if(pluginWE.isEnabled())
			{
				final WorldEditPlugin worldedit = (WorldEditPlugin)pluginWE;
				final Selection selection = worldedit.getSelection(player);
				if(selection != null)
				{
					final Location pos1 = selection.getMinimumPoint();
					final Location pos2 = selection.getMaximumPoint();
					final int xLen = pos2.getBlockX() - pos1.getBlockX() + 1;
					final int yLen = pos2.getBlockY() - pos1.getBlockY() + 1;
					final int zLen = pos2.getBlockZ() - pos1.getBlockZ() + 1;
					for(int y = 0; y < yLen; y += 1)
					{
						final Location startLoc = pos1.clone().add(0, y, 0);
						getServer().getScheduler().runTaskLater(pluginWE, new Runnable()
						{
							@Override
							public void run()
							{
								for(int z = 0; z < zLen; z += 1)
									for(int x = 0; x < xLen; x += 1)
										startLoc.clone().add(x, 0, z).getBlock().breakNaturally();
							}
						}, granulation * y);
					}
					throw new CommandAnswerException("{_LG}Operation will be finished in ~" + (yLen / granulation) + " seconds");
				} else
					throw new CommandAnswerException("{_LR}WorldEdit selection is empty.");
			} else
				throw new CommandAnswerException("{_LR}WorldEdit currently is disabled.");
		} else
			throw new CommandAnswerException("{_LR}WorldEdit was not found.");
	}
}
