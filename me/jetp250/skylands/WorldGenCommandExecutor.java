package me.jetp250.skylands.commands;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import me.jetp250.skylands.IslandChunkGenerator;
import me.jetp250.skylands.Skylands;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.ChunkProviderServer;
import net.minecraft.server.v1_11_R1.EntityTracker;
import net.minecraft.server.v1_11_R1.EnumDifficulty;
import net.minecraft.server.v1_11_R1.EnumGamemode;
import net.minecraft.server.v1_11_R1.IChunkLoader;
import net.minecraft.server.v1_11_R1.IChunkProvider;
import net.minecraft.server.v1_11_R1.IDataManager;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.ServerNBTManager;
import net.minecraft.server.v1_11_R1.WorldData;
import net.minecraft.server.v1_11_R1.WorldManager;
import net.minecraft.server.v1_11_R1.WorldServer;
import net.minecraft.server.v1_11_R1.WorldSettings;
import net.minecraft.server.v1_11_R1.WorldType;

public class WorldGenCommandExecutor implements CommandExecutor {
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label,
			final String[] args) {
		if (!sender.hasPermission("skylands.generate")) {
			sender.sendMessage("\u00a7cYou don't have permission to do that.");
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage("Usage: /" + label + " <name>");
			return false;
		}
		final String name = args[0];
		sender.sendMessage("Attempting to create the world '" + name + "'...");
		final MinecraftServer console = Skylands.getMinecraftServer();
		final CraftServer craftServer = Skylands.getCraftServer();
		final File folder = new File(craftServer.getWorldContainer(), name);
		final World world = craftServer.getWorld(name);
		if (world != null) {
			sender.sendMessage("World already exists, loading");
			if (sender instanceof Player) {
				((Player) sender).teleport(world.getSpawnLocation());
			}
			return false;
		}
		final WorldType type = WorldType.NORMAL;
		final boolean generateStructures = false; /*!*/
		if (folder.exists() && !folder.isDirectory()) {
			throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
		}
		int dimension = 10 + console.worlds.size();
		boolean used = false;
		do {
			for (final WorldServer server : console.worlds) {
				used = (server.dimension == dimension);
				if (used) {
					++dimension;
					break;
				}
			}
		} while (used);
		final boolean hardcore = false;
		final IDataManager sdm = new ServerNBTManager(craftServer.getWorldContainer(), name, true,
				craftServer.getHandle().getServer().getDataConverterManager());
		WorldData worlddata = sdm.getWorldData();
		WorldSettings worldSettings = null;
		if (worlddata == null) {
			worldSettings = new WorldSettings(Skylands.SHARED_RANDOM.nextLong(),
					EnumGamemode.getById(craftServer.getDefaultGameMode().getValue()), generateStructures, hardcore,
					type);
			worlddata = new WorldData(worldSettings, name);
		}
		worlddata.checkName(name);
		final WorldServer internal = new WorldServer(console, sdm, worlddata, dimension, console.methodProfiler,
				Environment.NORMAL, null) {
			@Override
			protected IChunkProvider n() {
				final IChunkLoader ichunkloader = this.dataManager.createChunkLoader(this.worldProvider);
				return new ChunkProviderServer(this, ichunkloader,
						/*new TimedChunkGenerator(this, */new IslandChunkGenerator(this,
								Skylands.SHARED_RANDOM)/*)*/);
			}
		};
		internal.generator = new IslandChunkGenerator(internal, Skylands.SHARED_RANDOM);
		internal.b();
		if (craftServer.getWorld(name.toLowerCase(Locale.ENGLISH)) == null) {
			sender.sendMessage("Failed to create the world!");
			return false;
		}
		if (worldSettings != null) {
			internal.a(worldSettings);
		}
		internal.scoreboard = craftServer.getScoreboardManager().getMainScoreboard().getHandle();
		internal.tracker = new EntityTracker(internal);
		internal.addIWorldAccess(new WorldManager(console, internal));
		internal.worldData.setDifficulty(EnumDifficulty.EASY);
		internal.setSpawnFlags(true, true);
		console.worlds.add(internal);
		craftServer.getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
		sender.sendMessage("Preparing start region for " + name + " (level " + (console.worlds.size() - 1) + ", Seed: "
				+ internal.getSeed() + ")");
		final long t = System.nanoTime();
		if (internal.getWorld().getKeepSpawnInMemory()) {
			final short short1 = internal.paperConfig.keepLoadedRange;
			long i = System.currentTimeMillis();
			for (int j = -short1; j <= short1; j += 16) {
				for (int k = -short1; k <= short1; k += 16) {
					final long l = System.currentTimeMillis();
					if (l < i) {
						i = l;
					}
					if (l > i + 1000L) {
						final int i2 = (short1 * 2 + 1) * (short1 * 2 + 1);
						final int j2 = (j + short1) * (short1 * 2 + 1) + k + 1;
						sender.sendMessage("Preparing spawn area for " + name + ", " + j2 * 100 / i2 + "%");
						i = l;
					}
					final BlockPosition chunkcoordinates = internal.getSpawn();
					internal.getChunkProviderServer().getChunkAt(chunkcoordinates.getX() + j >> 4,
							chunkcoordinates.getZ() + k >> 4);
				}
			}
		}
		craftServer.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
		sender.sendMessage("Done! Time taken: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t) + "ms");
		if (sender instanceof Player) {
			((Player) sender).teleport(internal.getWorld().getSpawnLocation());
			sender.sendMessage("Teleporting..");
		}
		return true;
	}
	
}
