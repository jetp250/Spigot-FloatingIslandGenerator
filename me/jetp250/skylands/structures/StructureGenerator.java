package me.jetp250.skylands.structures;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import me.jetp250.skylands.utility.FastRandom;
import me.jetp250.skylands.utility.MathHelper;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.World;

public class StructureGenerator {

	private static final Map<Structure, BlockPosition> LOCATIONS;

	private final Structure structure;
	private final Random random;
	private final float chance;

	public StructureGenerator(final Structure structure) {
		this(structure, 0.001F);
	}

	public StructureGenerator(final Structure structure, final float chance) {
		this(structure, new FastRandom(), chance);
	}

	public StructureGenerator(final Structure structure, final Random random, final float chance) {
		this.structure = structure;
		this.random = random;
		this.chance = chance;
	}

	public StructureGenerator(final File file) {
		this(file, 0.001F);
	}

	public StructureGenerator(final File file, final float chance) {
		this(file, new FastRandom(), chance);
	}

	public StructureGenerator(final File file, final Random random, final float chance) {
		this(Structure.fromFile(file), random, chance);
	}

	public Random getRandom() {
		return this.random;
	}

	public Structure getStructure() {
		return this.structure;
	}

	public float getChance() {
		return this.chance;
	}

	public void generate(final World world, int x, final int y, int z) {
		x <<= 4;
		z <<= 4;
		generate(world.getChunkAt(x, z), x, y, z);
	}

	public void generate(final Chunk chunk, final int x, final int y, final int z) {
		if (random.nextFloat() > this.chance) {
			return;
		}
	}

	@Nullable
	public BlockPosition findNearestMapFeature(final World world, String name, final BlockPosition center) {
		name = name.toLowerCase();
		BlockPosition nearest = new BlockPosition(3000000, 3000000, 3000000);
		int distance = Integer.MAX_VALUE;
		final Set<Entry<Structure, BlockPosition>> entries = StructureGenerator.LOCATIONS.entrySet();
		for (Entry<Structure, BlockPosition> entry : entries) {
			if (name != entry.getKey().getStructureName()) {
				continue;
			}
			final BlockPosition pos = entry.getValue();
			final int dSq = MathHelper.intDistanceSquared(pos.getX(), pos.getY(), pos.getZ(), nearest.getX(),
					nearest.getY(), nearest.getZ());
			if (dSq < distance) {
				nearest = pos;
				distance = dSq;
			}
		}
		return nearest == center ? null : nearest;
	}

	static {
		LOCATIONS = new HashMap<>();
	}
}
