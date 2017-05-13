package me.jetp250.skylands.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.jetp250.skylands.Skylands;
import me.jetp250.skylands.structures.Structure;
import me.jetp250.skylands.structures.Structures;

public class StructureCreator implements CommandExecutor {

	public static final WorldEditPlugin WORLDEDIT;

	private final Skylands plugin;

	public StructureCreator(final Skylands main) {
		this.plugin = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You need to be a player to use this command.");
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage("\u00a7cUsage: /" + label + " <save|load|paste|copy> [name]");
		}
		final String subcmd = args[0].toLowerCase();
		final Player player = (Player) sender;
		if ("save".equals(subcmd)) {
			final String name = args.length >= 2 ? args[1] : null;
			if (name == null) {
				sender.sendMessage("\u00a7cUsage: /" + label + " save <name>");
			} else {
				try {
					final LocalSession session = StructureCreator.WORLDEDIT.getSession(player);
					final Clipboard clipboard = session.getClipboard().getClipboard();
					final Structure structure = Structure.fromClipboard(clipboard, name);
					final int saved = structure.saveTo(Structures.DEFAULT_STRUCTURE_DIRECTORY);
					sender.sendMessage(String.format("%d blocks saved!", saved));
				} catch (EmptyClipboardException e) {
					sender.sendMessage("\u00a7cYour clipboard is empty. Use //copy first.");
				} catch (Exception e) {
					sender.sendMessage("Save failed.");
					e.printStackTrace();
				}
			}
		} else if ("load".equals(subcmd)) {
			final String name = args.length >= 2 ? args[1] : null;
			if (name == null) {
				sender.sendMessage("\u00a7cUsage: /" + label + " load <name>");
			} else {
				final BukkitPlayer bpl = StructureCreator.WORLDEDIT.wrapPlayer(player);
				new BukkitRunnable() {
					@Override
					public void run() {
						Structure structure = Structures.LOADED_STRUCTURES.get(name);
						if (structure == null) {
							structure = Structure
									.fromFile(new File(Structures.DEFAULT_STRUCTURE_DIRECTORY, name + ".structure"));
						}
						if (structure == null) {
							sender.sendMessage("\u00a7cFile '" + name + ".structure' not found.");
							return;
						}
						final LocalSession session = StructureCreator.WORLDEDIT.getSession(player);
						final Clipboard clipboard = structure.toClipboard(player.getLocation().toVector(),
								bpl.getWorld());
						session.setClipboard(new ClipboardHolder(clipboard, bpl.getWorld().getWorldData()));
						sender.sendMessage("\u00a7d" + structure.getName() + " loaded. Paste it with //paste");
					}
				}.runTaskAsynchronously(plugin);
			}
		} else if ("paste".equals(subcmd)) {
			final String name = args.length >= 2 ? args[1] : null;
			if (name == null) {
				sender.sendMessage("\u00a7cUsage: /" + label + " paste <name>");
			} else {
				Structure structure = Structures.LOADED_STRUCTURES.get(name);
				if (structure == null) {
					structure = Structure
							.fromFile(new File(Structures.DEFAULT_STRUCTURE_DIRECTORY, name + ".structure"));
				}
				if (structure == null) {
					sender.sendMessage("\u00a7cFile '" + name + ".structure' not found.");
					return false;
				}
				final Vector location = player.getLocation().toVector();
				structure.placeAt(((CraftWorld) player.getWorld()).getHandle(), location.getBlockX(),
						location.getBlockY(), location.getBlockZ(), false);
			}
		}
		return true;
	}

	static {
		final Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (plugin != null && plugin instanceof WorldEditPlugin) {
			WORLDEDIT = (WorldEditPlugin) plugin;
		} else {
			throw new IllegalStateException("worledit not installed!");
		}
	}

}
