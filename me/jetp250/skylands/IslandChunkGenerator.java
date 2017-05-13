package me.jetp250.skylands;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.craftbukkit.v1_11_R1.generator.InternalChunkGenerator;
import org.bukkit.generator.BlockPopulator;

import me.jetp250.skylands.structures.StructureGenerator;
import me.jetp250.skylands.structures.Structures;
import me.jetp250.skylands.utility.FastRandom;
import me.jetp250.skylands.utility.MathHelper;
import me.jetp250.skylands.utility.OctaveNoiseGenerator;
import net.minecraft.server.v1_11_R1.BiomeBase;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Blocks;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.ChunkSection;
import net.minecraft.server.v1_11_R1.EnumCreatureType;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.World;
import net.minecraft.server.v1_11_R1.WorldGenStronghold;
import net.minecraft.server.v1_11_R1.WorldServer;

@SuppressWarnings("deprecation")
public class IslandChunkGenerator extends InternalChunkGenerator {

	private static final IBlockData[] STONE;

	private final WorldServer world;
	private final FastRandom random;
	private final WorldGenStronghold strongholdGen;
	private final StructureGenerator structureGen;
	private final OctaveNoiseGenerator noise;
	private final OctaveNoiseGenerator noise_2;

	public IslandChunkGenerator(final WorldServer world, final FastRandom random) {
		this.strongholdGen = new WorldGenStronghold();
		this.structureGen = new StructureGenerator(Structures.END_PORTAL);
		this.world = world;
		this.random = random;
		this.noise = new OctaveNoiseGenerator(random, 6, 1 / 48.0F); // random, 6, 1 / 48.0F;
		this.noise_2 = new OctaveNoiseGenerator(random, 12, 1 / 128.0F); // random, 6, 1/64.0F
		this.world.keepSpawnInMemory = false;
	}

	static {
		STONE = new IBlockData[] { Blocks.STONE.getBlockData(), Blocks.STONE.fromLegacyData(5),
				Blocks.COBBLESTONE.getBlockData() };
	}
	
	@Override
	public Chunk getOrCreateChunk(final int chunkX, final int chunkZ) {
		this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		final Chunk chunk = new Chunk(this.world, chunkX, chunkZ);
		final ChunkSection[] sections = chunk.getSections();
		final int rX = chunkX * 16;
		final int rZ = chunkZ * 16;
		for (int x = 0; x < 16; ++x) {
			final int realX = rX + x;
			for (int z = 0; z < 16; ++z) {
				final int realZ = rZ + z;
				final float bottom = noise_2.noise(realX, realZ, 1.4F, 0.81F) * 32 + 100;
				float top = noise.noise(realX, realZ, 0.2F, 0.1F) * 8 + 90;
				final float sub = top - bottom;
				if (sub < 15) {
					top -= (20 - sub) * 0.245F;
				}
				if (top - bottom >= 3) {
					int y = top >= 255 ? 255 : MathHelper.ceil(top);
					int sec;
					ChunkSection section;
					if ((section = sections[sec = y >> 4]) == null)
						section = sections[sec] = new ChunkSection(sec << 4, true);
					section.getBlocks().setBlock(x, y-- & 0xF, z, Blocks.GRASS.getBlockData());
					final int decorationHeight = y - random.nextInt(1) - 2;
					for (; y > decorationHeight; --y) {
						if ((section = sections[sec = y >> 4]) == null)
							section = sections[sec] = new ChunkSection(sec << 4, true);
						section.getBlocks().setBlock(x, y & 0xF, z, Blocks.DIRT.getBlockData());
					}
					for (; y > bottom; --y) {
						if ((section = sections[sec = y >> 4]) == null)
							section = sections[sec] = new ChunkSection(sec << 4, true);
						section.getBlocks().setBlock(x, y & 0xF, z, STONE[random.nextInt(STONE.length)]);
					}
				}
			}
		}
		structureGen.generate(chunk, random.nextInt(16), random.nextInt(80, 120), random.nextInt(16));
		chunk.initLighting();
		return chunk;
	}

	@Override
	public boolean a(final Chunk chunk, final int i, final int i1) {
		return false;
	}

	@Override
	public byte[] generate(final org.bukkit.World world, final Random random, final int x, final int z) {
		return null;
	}

	@Override
	public byte[][] generateBlockSections(final org.bukkit.World world, final Random random, final int x, final int z,
			final BiomeGrid biomes) {
		return null;
	}

	@Override
	public short[][] generateExtBlockSections(final org.bukkit.World world, final Random random, final int x,
			final int z, final BiomeGrid biomes) {
		return null;
	}

	public Chunk getChunkAt(final int x, final int z) {
		return this.getOrCreateChunk(x, z);
	}

	@Override
	public boolean canSpawn(final org.bukkit.World world, final int x, final int z) {
		final int xShift = x >> 4;
		final int zShift = z >> 4;
		if (!world.isChunkLoaded(xShift, zShift)) {
			world.loadChunk(xShift, zShift);
		}
		return this.world.getHighestBlockYAt(new BlockPosition(x, 0, z)).getY() > 0;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(final org.bukkit.World world) {
		return Collections.emptyList();
	}

	@Override
	public List<BiomeBase.BiomeMeta> getMobsFor(final EnumCreatureType type, final BlockPosition position) {
		final BiomeBase biomebase = this.world.getBiome(position);
		return (biomebase == null) ? null : biomebase.getMobs(type);
	}

	@Override
	public BlockPosition findNearestMapFeature(final World world, final String type, final BlockPosition position,
			final boolean flag) {
		return ("Stronghold".equals(type) && this.strongholdGen != null)
				? this.strongholdGen.getNearestGeneratedFeature(world, position, flag)
				: this.structureGen.findNearestMapFeature(world, type, position);
	}

	@Override
	public void recreateStructures(final int i, final int j) {
	}

	@Override
	public void recreateStructures(final Chunk chunk, final int i, final int j) {
		this.strongholdGen.a(this.world, i, j, null);
	}
}
