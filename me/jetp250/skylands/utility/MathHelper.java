package me.jetp250.skylands.utility;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class MathHelper {

	public static final FastRandom RANDOM = new FastRandom();
	private static final float[] SIN;

	public static float sin(final float n) {
		return MathHelper.SIN[(int) (n * 10430.378f) & 0xFFFF];
	}

	public static float cos(final float n) {
		return MathHelper.SIN[(int) (n * 10430.378f + 16384.0f) & 0xFFFF];
	}

	public static final int round(final float f) {
		return (int) (f > 0 ? f + 0.5F : f - 0.5F);
	}

	public static final int floor(final float f) {
		final int i = (int) f;
		return i > f ? i - 1 : i;
	}

	public static final int ceil(final float f) {
		final int i = (int) f;
		return i > f ? i : i - 1;
	}

	public static int distanceAbs(final int a, final int b) {
		return a > b ? a - b : b - a;
	}

	public static float distanceAbs(final float a, final float b) {
		return a > b ? a - b : b - a;
	}

	public static int intDistanceSquared(int x, int y, int z, final int x2, final int y2, final int z2) {
		x -= x2;
		y -= y2;
		z -= z2;
		return x * x + y * y + z * z;
	}

	public static int center(final int input) {
		return floor(input - Math.abs(input * 0.5F));
	}

	public static float center(final float input) {
		return input - Math.abs(input * 0.5F);
	}

	public static final Vector rotatedDirection(final Location location, final float xRot, final float yRot) {
		location.setPitch(location.getPitch() + yRot);
		location.setYaw(location.getYaw() + xRot);
		final double rotY = Math.toRadians(location.getPitch());
		final double rotX = Math.toRadians(location.getYaw());
		final double zx = Math.cos(rotY);
		return new Vector(-zx * Math.sin(rotX), -Math.sin(rotY), zx * Math.cos(rotX));
	}

	public static final Vector rotatedDirection(final float pitch_, final float yaw_, final float xRot,
			final float yRot) {
		final double pitch = Math.toRadians(pitch_ + 90 + xRot);
		final double yaw = Math.toRadians(yaw_ + 90 + yRot);
		final double z_axis = Math.sin(pitch);
		return new Vector(z_axis * Math.cos(yaw), Math.cos(pitch), z_axis * Math.sin(yaw));
	}

	public static int[] parseIntegers(final char[] input, final char separator, final int expLen) {
		final int[] res = new int[expLen];
		int temp = 0;
		int index = 0;
		for (int i = 0; i < input.length; ++i) {
			final char c = input[i];
			if (c == separator) {
				res[index++] = temp;
				temp = 0;
				continue;
			}
			temp *= 10;
			temp += c - '0';
		}
		return res;
	}

	public static int fastParseInt(final String input) {
		final char[] charArray = input.toCharArray();
		int res = 0;
		for (int i = 0; i < charArray.length; ++i) {
			final char c = charArray[i];
			if (!Character.isDigit(c)) {
				return res == 0 ? -1 : res;
			}
			res *= 10;
			res += c - '0';
		}
		return res;
	}

	static {
		SIN = new float[65536];
		for (int i = 0; i < 65536; ++i) {
			MathHelper.SIN[i] = (float) Math.sin(i * 3.141592653589793 * 2.0 / 65536.0);
		}
	}
}
