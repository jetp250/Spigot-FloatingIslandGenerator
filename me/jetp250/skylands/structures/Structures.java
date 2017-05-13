package me.jetp250.skylands.structures;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import me.jetp250.skylands.Skylands;

public final class Structures {
	
	public static Map<String, Structure> LOADED_STRUCTURES;
	
	private Structures() {
	}
	
	public static final File DEFAULT_STRUCTURE_DIRECTORY;
	public static final Structure END_PORTAL;
	
	static {
		final Skylands plugin = (Skylands) Bukkit.getPluginManager().getPlugin("Skylands");
		if (plugin == null || !plugin.isEnabled()) {
			throw new IllegalStateException("Accessed structures before plugin was enabled!");
		}
		DEFAULT_STRUCTURE_DIRECTORY = new File(plugin.getDataFolder() + "/structures");
		if (!DEFAULT_STRUCTURE_DIRECTORY.exists()) {
			DEFAULT_STRUCTURE_DIRECTORY.mkdirs();
		}
//		Structure endPortal = Structure.fromFile(new File(DEFAULT_STRUCTURE_DIRECTORY, "EndPortal.structure"));
//		if (endPortal == null) {
//			final InputStream stream = plugin.getResource("EndPortal.structure");
//			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
//				
//			} catch (final IOException e) {
//				
//			}
//		}
//		END_PORTAL = endPortal;
		END_PORTAL = null;
		LOADED_STRUCTURES = new HashMap<>();
	}
	
}
