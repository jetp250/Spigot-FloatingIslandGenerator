package me.jetp250.skylands.utility;

public class OctaveNoiseGenerator {

	private final float scale;
	private final NoiseGenerator[] octaves;

	public OctaveNoiseGenerator(final FastRandom rand, final int octaves, final float scale) {
		this.scale = scale;
		this.octaves = new NoiseGenerator[octaves];
		for (int i = 0; i < octaves; ++i)
			this.octaves[i] = new NoiseGenerator(rand);
	}

	public final float noise(float x, float y, final float frequency, final float amplitude) {
		float result = 0.0F;
		float freq = 1.0F;
		float amp = 1.0F;
		x *= scale;
		y *= scale;
		for (int i = 0; i < octaves.length; ++i) {
			result += octaves[i].noise(x * freq, y * freq) * amp;
			freq *= frequency;
			amp *= amplitude;
		}
		return result;
	}

	public static class NoiseGenerator {

		protected static final int[][] GRAD_3;
		protected static final int[] PERM;
		protected static final float F2;
		protected static final float G2;
		protected static final float G22;

		private final float offsetX;
		private final float offsetY;

		protected NoiseGenerator(final FastRandom random) {
			this.offsetX = random.nextFloat(256.0F);
			this.offsetY = random.nextFloat(256.0F);
		}

		protected static int floor(final float x) {
			return (x >= 0.0) ? ((int) x) : ((int) x - 1);
		}

		protected static float dot(final int[] g, final float x, final float y) {
			return g[0] * x + g[1] * y;
		}

		public float noise(float xin, float yin) {
			xin += this.offsetX;
			yin += this.offsetY;
			final float s = (xin + yin) * NoiseGenerator.F2;
			final int i = NoiseGenerator.floor(xin + s);
			final int j = NoiseGenerator.floor(yin + s);
			final float t = (i + j) * NoiseGenerator.G2;
			final float x0 = xin - (i - t);
			final float y0 = yin - (j - t);
			int i2;
			int j2;
			if (x0 > y0) {
				i2 = 1;
				j2 = 0;
			} else {
				i2 = 0;
				j2 = 1;
			}
			final float x2 = x0 - i2 + NoiseGenerator.G2;
			final float y2 = y0 - j2 + NoiseGenerator.G2;
			final float x3 = x0 + NoiseGenerator.G22;
			final float y3 = y0 + NoiseGenerator.G22;
			final int ii = i & 0xFF;
			final int jj = j & 0xFF;
			float t2 = 0.5F - x0 * x0 - y0 * y0;
			float n0;
			if (t2 < 0.0F) {
				n0 = 0.0F;
			} else {
				t2 *= t2;
				n0 = t2 * t2;
				n0 *= dot(NoiseGenerator.GRAD_3[NoiseGenerator.PERM[ii + NoiseGenerator.PERM[jj]] % 12], x0, y0);
			}
			float t3 = 0.5F - x2 * x2 - y2 * y2;
			float n2;
			if (t3 < 0.0F) {
				n2 = 0.0F;
			} else {
				t3 *= t3;
				n2 = t3 * t3;
				n2 *= dot(NoiseGenerator.GRAD_3[NoiseGenerator.PERM[ii + i2 + NoiseGenerator.PERM[jj + j2]] % 12], x2,
						y2);
			}
			float t4 = 0.5F - x3 * x3 - y3 * y3;
			float n3;
			if (t4 < 0.0F) {
				n3 = 0.0F;
			} else {
				t4 *= t4;
				n3 = t4 * t4 * dot(
						NoiseGenerator.GRAD_3[NoiseGenerator.PERM[ii + 1 + NoiseGenerator.PERM[jj + 1]] % 12], x3, y3);
			}
			return 70.0F * (n0 + n2 + n3);
		}

		static {
			GRAD_3 = new int[][] { { 1, 1, 0 }, { -1, 1, 0 }, { 1, -1, 0 }, { -1, -1, 0 }, { 1, 0, 1 }, { -1, 0, 1 },
					{ 1, 0, -1 }, { -1, 0, -1 }, { 0, 1, 1 }, { 0, -1, 1 }, { 0, 1, -1 }, { 0, -1, -1 } };
			PERM = new int[512];
			final int[] p = { 151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30,
					69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219,
					203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74,
					165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105,
					92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
					89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217,
					226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17,
					182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167,
					43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97,
					228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
					49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138,
					236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180 };
			for (int i = 0; i < 512; ++i) {
				NoiseGenerator.PERM[i] = p[i & 0xFF];
			}
			final float sqrt_3 = (float) Math.sqrt(3.0F);
			F2 = 0.5F * (sqrt_3 - 1.0F);
			G2 = (3.0F - sqrt_3) * 0.16666666666666666F;
			G22 = NoiseGenerator.G2 * 2.0F - 1.0F;
		}
	}
}