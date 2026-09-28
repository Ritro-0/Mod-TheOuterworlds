package com.theouterworld.entity.ai;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * A straight climb from open sky, then a steered fall onto the plate.
 * Horizontal speed waits until they are descending, so they do not drift
 * under the plate and hit it on the way up.
 */
public final class WeaverHomeLeap {
	public static final int APPROACH_XZ = 15;
	/** Feet this far above the plate before the fall turns back toward it. */
	public static final int APEX_ABOVE_PLATE = 36;
	private static final double MIN_RISE = 28.0;
	private static final double HORIZONTAL_DRAG = 0.91;
	private static final double VERTICAL_DRAG = 0.98;
	private static final int MAX_TICKS = 400;

	private WeaverHomeLeap() {
	}

	public static Vec3 launchVelocity(WeaverEntity weaver, BlockPos plate) {
		double gravity = gravity(weaver);
		double startY = weaver.getY();
		double rise = Math.max(MIN_RISE, plate.getY() + APEX_ABOVE_PLATE - startY);
		double vy = velocityForApex(rise, gravity);
		return new Vec3(0.0, vy, 0.0);
	}

	/** Climb straight. Once falling, retune horizontal speed toward the plate. */
	public static Vec3 steer(WeaverEntity weaver, BlockPos plate) {
		Vec3 velocity = weaver.getDeltaMovement();
		if (velocity.y > 0.08) {
			return new Vec3(0.0, velocity.y, 0.0);
		}
		int ticks = Math.max(1, ticksUntilLanding(weaver.getY(), velocity.y, gravity(weaver), plate.getY() + 1.0));
		return new Vec3(
			horizontal(plate.getX() + 0.5 - weaver.getX(), ticks),
			velocity.y,
			horizontal(plate.getZ() + 0.5 - weaver.getZ(), ticks)
		);
	}

	private static double gravity(WeaverEntity weaver) {
		return Math.max(0.005, weaver.getGravity());
	}

	private static double velocityForApex(double rise, double gravity) {
		double lo = 0.0;
		double hi = 0.42;
		while (apexRise(hi, gravity) < rise && hi < 8.0) {
			hi *= 1.4;
		}
		for (int i = 0; i < 18; i++) {
			double mid = (lo + hi) * 0.5;
			if (apexRise(mid, gravity) < rise) {
				lo = mid;
			} else {
				hi = mid;
			}
		}
		return hi;
	}

	private static double apexRise(double vy, double gravity) {
		double y = 0.0;
		for (int tick = 0; tick < MAX_TICKS && vy > 0.0; tick++) {
			y += vy;
			vy = (vy - gravity) * VERTICAL_DRAG;
		}
		return y;
	}

	private static int ticksUntilLanding(double y, double vy, double gravity, double landingY) {
		boolean falling = vy <= 0.0;
		for (int tick = 1; tick <= MAX_TICKS; tick++) {
			y += vy;
			if (vy <= 0.0) {
				falling = true;
			}
			vy = (vy - gravity) * VERTICAL_DRAG;
			if (falling && y <= landingY) {
				return tick;
			}
		}
		return MAX_TICKS;
	}

	private static double horizontal(double distance, int ticks) {
		double retained = 1.0 - Math.pow(HORIZONTAL_DRAG, Math.max(1, ticks));
		if (retained < 1.0e-4) {
			return 0.0;
		}
		return distance * (1.0 - HORIZONTAL_DRAG) / retained;
	}
}
