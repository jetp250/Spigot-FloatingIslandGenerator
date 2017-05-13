package me.jetp250.skylands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import me.jetp250.skylands.commands.StructureCreator;
import me.jetp250.skylands.commands.WorldGenCommandExecutor;
import me.jetp250.skylands.utility.FastRandom;
import net.minecraft.server.v1_11_R1.MinecraftServer;

public class Skylands extends JavaPlugin {

	public static final FastRandom SHARED_RANDOM;
	protected static final MinecraftServer MC_SERVER;
	protected static final CraftServer CB_SERVER;

	@Override
	public void onEnable() {
		this.getCommand("generateworld").setExecutor(new WorldGenCommandExecutor());
		this.getCommand("structure").setExecutor(new StructureCreator(this));
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
		final World world;
		if ((world = Bukkit.getWorld(worldName)) == null) {
			return null;
		}
		return new IslandChunkGenerator(((CraftWorld) world).getHandle(), Skylands.SHARED_RANDOM);
	}

	public static MinecraftServer getMinecraftServer() {
		return Skylands.MC_SERVER;
	}

	public static CraftServer getCraftServer() {
		return Skylands.CB_SERVER;
	}

	static {
		SHARED_RANDOM = new FastRandom();
		CB_SERVER = (CraftServer) Bukkit.getServer();
		MC_SERVER = CB_SERVER.getServer();
	}
}
