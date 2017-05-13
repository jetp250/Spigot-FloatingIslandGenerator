package me.jetp250.skylands.structures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;

import me.jetp250.skylands.utility.MathHelper;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.ChunkSection;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.World;

public class Structure {

	private final OffsetBlock[] data;
	private final int[] region;
	private final String name;
	private final String lookupName;

	protected Structure(final OffsetBlock[] data, final String name, final int[] region) {
		this.data = data;
		this.region = region;
		this.name = name;
		this.lookupName = name.toLowerCase();

	}

	public String getName() {
		return this.name;
	}

	public String getStructureName() {
		return this.lookupName;
	}

	public void placeAt(final World world, final int x, final int y, final int z, final boolean centered) {
		int prX = 0;
		int prY = 0;
		int prZ = 0;
		Chunk chunk = null;
		ChunkSection section = null;
		if (centered) {
			for (int i = 0; i < data.length; ++i) {
				final OffsetBlock block = data[i];
				final int rx = (x + block.cx) >> 4;
				final int ry = (y + block.cy) >> 4;
				final int rz = (z + block.cz) >> 4;
				if (rx != prX || rz != prZ || chunk == null) {
					chunk = world.getChunkAt(rx, rz);
					section = chunk.getSections()[ry];
				} else if (prY != prZ || section == null) {
					section = chunk.getSections()[ry];
				}
				section.setType(rx, ry, rz, block.type);
			}
		} else {
			for (int i = 0; i < data.length; ++i) {
				final OffsetBlock block = data[i];
				final int rx = (x + block.x) >> 4;
				final int ry = (y + block.y) >> 4;
				final int rz = (z + block.z) >> 4;
				Bukkit.broadcastMessage(String.format("%d, %d, %d", rx, ry, rz));
				if (rx != prX || rz != prZ || chunk == null) {
					chunk = world.getChunkAt(rx, rz);
					section = chunk.getSections()[ry];
					if (section == null) {
						section = chunk.getSections()[ry] = new ChunkSection(y >> 4 << 4, true);
					}
				} else if (prY != prZ || section == null) {
					section = chunk.getSections()[ry];
					if (section == null) {
						section = chunk.getSections()[ry] = new ChunkSection(y >> 4 << 4, true);
					}
				}
				section.setType(rx, ry, rz, block.type);
			}
		}
	}

	public int saveTo(final File directory) {
		final File file = new File(directory, this.getName() + ".structure");
		try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(getStructureName() + '\n');
			writer.write(String.format("%d|%d|%d|%d%n", data.length, region[0], region[1], region[2]));
			for (int i = 0; i < this.data.length; ++i) {
				final OffsetBlock block = data[i];
				if (block == null) {
					continue;
				}
				writer.write(String.format("%d|%d|%d|%d|%d%n", block.x, block.y, block.z,
						Block.getId(block.type.getBlock()), block.type.getBlock().toLegacyData(block.type)));
			}
			writer.close();
			return this.data.length;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Nullable
	public static Structure fromFile(final File file) {
		final OffsetBlock[] data;
		final String name;
		final int[] len;
		try (final BufferedReader bf = new BufferedReader(new FileReader(file))) {
			name = bf.readLine();
			String line = bf.readLine();
			int[] dimensions = MathHelper.parseIntegers(line.toCharArray(), '|', 4);
			data = new OffsetBlock[dimensions[0]];
			len = new int[] { dimensions[1], dimensions[2], dimensions[3] };
			int index = 0;
			while ((line = bf.readLine()) != null && !line.isEmpty()) {
				final int[] set = MathHelper.parseIntegers(line.toCharArray(), '|', 5);
				data[index++] = new OffsetBlock(Block.getById(set[3]).fromLegacyData(set[4]), set[0], set[1], set[2]);
			}
		} catch (final IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load structure from file '" + file.getName() + "'!", e);
			return null;
		}
		return new Structure(data, name, len);
	}

	@SuppressWarnings("deprecation")
	public static Structure fromClipboard(final Clipboard clipboard, final String name) {
		final List<OffsetBlock> tempList = new ArrayList<>();
		final Vector dim = clipboard.getDimensions();
		Bukkit.broadcastMessage(dim.toString());
		final int[] len = new int[] { dim.getBlockX(), dim.getBlockY(), dim.getBlockZ() };
		clipboard.getRegion().forEach(bv -> {
			final BaseBlock block = clipboard.getBlock(bv);
			if (block.getType() != 0) {
				final IBlockData data = Block.getById(block.getType()).fromLegacyData(block.getData());
				if (data != null) {
					tempList.add(new OffsetBlock(data, bv.getBlockX(), bv.getBlockY(), bv.getBlockZ()));
				}
			}
		});
		return new Structure(tempList.toArray(new OffsetBlock[tempList.size()]), name, len);
	}

	public Clipboard toClipboard(final org.bukkit.util.Vector center, final com.sk89q.worldedit.world.World world) {
		final CuboidRegion region = new CuboidRegion(world, new Vector(0, 0, 0),
				new Vector(this.region[0], this.region[1], this.region[2]));
		final Clipboard clipboard = new BlockArrayClipboard(region);
		try {
			for (int i = 0; i < data.length; ++i) {
				final OffsetBlock block = data[i];
				clipboard.setBlock(new Vector(block.x, block.y, block.z), new BaseBlock(block.typeId, block.data));
			}
		} catch (final WorldEditException e) {
			Bukkit.getLogger().log(Level.WARNING, "failed to load structure to clipboard", e);
			return null;
		}
		return clipboard;
	}

	protected static class OffsetBlock {

		private final IBlockData type;
		private final byte typeId;
		private final byte data;
		private final int x, y, z;
		private final int cx, cy, cz;

		public OffsetBlock(final IBlockData type, final int x, final int y, final int z) {
			this.type = type;
			this.typeId = (byte) Block.getId(type.getBlock());
			this.data = (byte) type.getBlock().toLegacyData(type);
			this.x = x;
			this.y = y;
			this.z = z;
			this.cx = MathHelper.center(x);
			this.cy = MathHelper.center(y);
			this.cz = MathHelper.center(z);
		}
	}
}
