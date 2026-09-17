package com.theouterworld.block;

import com.theouterworld.mixin.LivingEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Quicksand-like cloud: no solid collision, a slow sink, and Space climbs back out.
 * Gravity is handled separately so Highworld's 2.5g does not get baked into velocity.
 */
public class AerogelCloudBlock extends Block {
	private final float horizontalDrag;
	private final double sinkSpeed;
	private final double climbSpeed;
	private final int nauseaAmplifier;

	public AerogelCloudBlock(
		Properties properties,
		float horizontalDrag,
		double sinkSpeed,
		double climbSpeed,
		int nauseaAmplifier
	) {
		super(properties);
		this.horizontalDrag = horizontalDrag;
		this.sinkSpeed = sinkSpeed;
		this.climbSpeed = climbSpeed;
		this.nauseaAmplifier = nauseaAmplifier;
	}

	public static boolean isInCloud(Entity entity) {
		return entity.getInBlockState().getBlock() instanceof AerogelCloudBlock;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
		return neighborState.is(this) || super.skipRendering(state, neighborState, direction);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		return Shapes.block();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return true;
	}

	@Override
	protected void entityInside(
		BlockState state,
		Level level,
		BlockPos pos,
		Entity entity,
		InsideBlockEffectApplier effectApplier,
		boolean isPrecise
	) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}

		entity.resetFallDistance();

		if (!level.isClientSide()) {
			BlockPos eyePos = BlockPos.containing(living.getX(), living.getEyeY(), living.getZ());
			if (level.getBlockState(eyePos).is(this)) {
				living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, this.nauseaAmplifier, false, true, true));
			}
		}

		if (!living.getInBlockState().is(this)) {
			return;
		}

		if (com.theouterworld.item.AerostatBalloonItem.isActive(living)) {
			// Balloon owns all motion; clouds must not drag or override lift.
			return;
		}

		BlockPos body = BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() * 0.25, entity.getZ());
		if (!body.equals(pos)) {
			return;
		}

		Vec3 motion = entity.getDeltaMovement();
		boolean climbing = ((LivingEntityAccessor) living).theouterworlds$isJumping();
		double nextY = climbing ? this.climbSpeed : -this.sinkSpeed;
		entity.setDeltaMovement(motion.x * this.horizontalDrag, nextY, motion.z * this.horizontalDrag);

		if (level.isClientSide()) {
			RandomSource random = level.getRandom();
			if (random.nextInt(48) == 0) {
				level.addParticle(
					ParticleTypes.CLOUD,
					entity.getX() + (random.nextDouble() - 0.5) * 0.5,
					entity.getY() + random.nextDouble() * 0.6,
					entity.getZ() + (random.nextDouble() - 0.5) * 0.5,
					0.0,
					0.01,
					0.0
				);
			}
		}
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		return List.of();
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
	}
}
