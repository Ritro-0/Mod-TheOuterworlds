package com.theouterworld.block;

import com.mojang.serialization.MapCodec;
import com.theouterworld.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RiftBlock extends BaseEntityBlock {
	public static final MapCodec<RiftBlock> CODEC = simpleCodec(RiftBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	private static final ParticleOptions[] UNSTABLE_PARTICLES = {
		ParticleTypes.PORTAL,
		ParticleTypes.REVERSE_PORTAL,
		ParticleTypes.WITCH,
		ParticleTypes.ELECTRIC_SPARK,
		ParticleTypes.END_ROD,
		ParticleTypes.SOUL,
		ParticleTypes.GLOW,
		ParticleTypes.ENCHANT,
		ParticleTypes.NAUTILUS
	};

	private static final VoxelShape SHAPE_NS = Block.box(0.0, 0.0, 7.5, 16.0, 16.0, 8.5);
	private static final VoxelShape SHAPE_EW = Block.box(7.5, 0.0, 0.0, 8.5, 16.0, 16.0);

	private static final int TELEPORT_COOLDOWN_TICKS = 80;
	private static final Map<Entity, Long> TELEPORT_COOLDOWNS = new WeakHashMap<>();

	public RiftBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RiftBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		Level world = ctx.getLevel();
		if (pos.getY() < world.getMaxY() && world.getBlockState(pos.above()).canBeReplaced(ctx)) {
			return this.defaultBlockState()
				.setValue(FACING, ctx.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = world.getBlockState(pos.below());
			return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
		}
		BlockState above = world.getBlockState(pos.above());
		return above.is(this) || above.canBeReplaced();
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader world,
		ScheduledTickAccess scheduledTickView,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		RandomSource random
	) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y) {
			boolean lookingAtPartner =
				(half == DoubleBlockHalf.LOWER && direction == Direction.UP)
					|| (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN);
			if (lookingAtPartner && (!neighborState.is(this) || neighborState.getValue(HALF) == half)) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.updateShape(state, world, scheduledTickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
		// Survival/adventure cannot mine rifts — only removing the rift charge's redstone closes them.
		if (!player.getAbilities().instabuild) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, world, pos);
	}

	/** Creative-only block: never drop an item, including on depower / explosions. */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		return List.of();
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!player.getAbilities().instabuild) {
			return state;
		}
		if (!world.isClientSide()) {
			DoubleBlockHalf half = state.getValue(HALF);
			if (half == DoubleBlockHalf.UPPER) {
				BlockPos below = pos.below();
				BlockState belowState = world.getBlockState(below);
				if (belowState.is(this) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
					world.destroyBlock(below, false);
				}
			} else {
				BlockPos above = pos.above();
				if (world.getBlockState(above).is(this)) {
					world.destroyBlock(above, false);
				}
			}
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case EAST, WEST -> SHAPE_EW;
			default -> SHAPE_NS;
		};
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void entityInside(
		BlockState state,
		Level world,
		BlockPos pos,
		Entity entity,
		InsideBlockEffectApplier applier,
		boolean firstTick
	) {
		if (world.isClientSide() || !(world instanceof ServerLevel serverWorld)) {
			return;
		}
		if (entity.isPassenger() || (entity instanceof Player player && player.isSpectator())) {
			return;
		}
		if (isOnCooldown(entity, serverWorld)) {
			return;
		}
		teleportThroughRift(state, serverWorld, pos, entity);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		Direction facing = state.getValue(FACING);
		boolean ns = facing.getAxis() == Direction.Axis.Z;
		int bursts = 3 + random.nextInt(4);
		for (int i = 0; i < bursts; i++) {
			double x = ns ? pos.getX() + random.nextDouble() : pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.35;
			double y = pos.getY() + random.nextDouble();
			double z = ns ? pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.35 : pos.getZ() + random.nextDouble();
			ParticleOptions particle = UNSTABLE_PARTICLES[random.nextInt(UNSTABLE_PARTICLES.length)];
			world.addParticle(
				particle,
				x,
				y,
				z,
				(random.nextDouble() - 0.5) * 0.85,
				(random.nextDouble() - 0.5) * 0.7,
				(random.nextDouble() - 0.5) * 0.85
			);
		}
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER && random.nextInt(40) == 0) {
			world.playLocalSound(
				pos.getX() + 0.5,
				pos.getY() + 1.0,
				pos.getZ() + 0.5,
				SoundEvents.PORTAL_AMBIENT,
				SoundSource.BLOCKS,
				0.35F,
				random.nextFloat() * 0.4F + 0.8F,
				false
			);
		}
	}

	private void teleportThroughRift(BlockState state, ServerLevel world, BlockPos pos, Entity entity) {
		var server = world.getServer();
		ServerLevel targetWorld = server.getLevel(riftDestination(world));
		if (targetWorld == null) {
			return;
		}

		BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
		Direction facing = state.getValue(FACING);
		BlockPos landingPos = prepareDestinationRift(targetWorld, lowerPos, facing);
		Vec3 destination = offsetInFront(landingPos, facing);

		setCooldown(entity, targetWorld);
		Entity teleported = entity.teleport(new TeleportTransition(
			targetWorld,
			destination,
			entity.getDeltaMovement(),
			entity.getYRot(),
			entity.getXRot(),
			TeleportTransition.PLAY_PORTAL_SOUND
		));
		if (teleported != null) {
			setCooldown(teleported, targetWorld);
		}
		targetWorld.playSound(
			null,
			BlockPos.containing(destination),
			SoundEvents.PORTAL_TRAVEL,
			SoundSource.BLOCKS,
			0.4F,
			1.0F
		);
	}

	private static BlockPos prepareDestinationRift(ServerLevel targetWorld, BlockPos sourceLower, Direction facing) {
		BlockPos existing = findExistingRift(targetWorld, sourceLower.getX(), sourceLower.getZ());
		if (existing != null) {
			return existing;
		}

		BlockPos surface = findSurface(targetWorld, sourceLower.getX(), sourceLower.getZ());
		BlockState below = targetWorld.getBlockState(surface.below());
		if (below.isAir() || !below.isCollisionShapeFullBlock(targetWorld, surface.below())) {
			targetWorld.setBlockAndUpdate(surface.below(), riftFoundation(targetWorld));
		}

		BlockState rift = ModBlocks.RIFT.defaultBlockState()
			.setValue(FACING, facing)
			.setValue(HALF, DoubleBlockHalf.LOWER);
		if (targetWorld.getBlockState(surface).canBeReplaced() && targetWorld.getBlockState(surface.above()).canBeReplaced()) {
			targetWorld.setBlock(surface, rift, Block.UPDATE_ALL);
			targetWorld.setBlock(surface.above(), rift.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		}
		return surface;
	}

	@Nullable
	private static BlockPos findExistingRift(ServerLevel world, int x, int z) {
		int minY = world.getMinY();
		int maxY = world.getMaxY();
		for (int y = maxY; y >= minY; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			BlockState state = world.getBlockState(pos);
			if (state.is(ModBlocks.RIFT) && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
				return pos;
			}
		}
		return null;
	}

	private static BlockPos findSurface(ServerLevel world, int x, int z) {
		int top = world.getMaxY();
		int bottom = world.getMinY();
		for (int y = top; y >= bottom; y--) {
			BlockPos ground = new BlockPos(x, y, z);
			BlockState groundState = world.getBlockState(ground);
			if (groundState.isAir() || !groundState.isCollisionShapeFullBlock(world, ground)) {
				continue;
			}
			BlockPos lower = ground.above();
			BlockPos upper = lower.above();
			if (world.getBlockState(lower).canBeReplaced() && world.getBlockState(upper).canBeReplaced()) {
				return lower;
			}
		}
		return new BlockPos(x, Math.max(bottom + 64, 64), z);
	}

	private static Vec3 offsetInFront(BlockPos riftLower, Direction facing) {
		return Vec3.atBottomCenterOf(riftLower).add(facing.getStepX() * 1.5, 0.0, facing.getStepZ() * 1.5);
	}

	private static ResourceKey<Level> riftDestination(ServerLevel world) {
		if (ModDimensions.isMoon(world.dimension())
			|| ModDimensions.isInnerworld(world.dimension())
			|| ModDimensions.isNearworld(world.dimension())) {
			return ModDimensions.OUTERWORLD_WORLD_KEY;
		}
		if (ModDimensions.isOuterworld(world.dimension())) {
			return Level.OVERWORLD;
		}
		return ModDimensions.OUTERWORLD_WORLD_KEY;
	}

	private static BlockState riftFoundation(ServerLevel targetWorld) {
		if (ModDimensions.isMoon(targetWorld.dimension())) {
			return ModBlocks.NORITE.defaultBlockState();
		}
		if (ModDimensions.isInnerworld(targetWorld.dimension())) {
			return ModBlocks.KOMATIITE.defaultBlockState();
		}
		if (ModDimensions.isNearworld(targetWorld.dimension())
			|| ModDimensions.isEmberworld(targetWorld.dimension())) {
			return ModBlocks.SULFURIC_BASALT.defaultBlockState();
		}
		if (ModDimensions.isFrostworld(targetWorld.dimension())) {
			return ModBlocks.CARBONIC_ICE.defaultBlockState();
		}
		if (ModDimensions.isAmberworld(targetWorld.dimension())) {
			return ModBlocks.THOLIN.defaultBlockState();
		}
		if (ModDimensions.isSpongeworld(targetWorld.dimension())) {
			return Blocks.PACKED_ICE.defaultBlockState();
		}
		if (ModDimensions.isPotatoworlds(targetWorld.dimension())) {
			return ModBlocks.REGOLITH.defaultBlockState();
		}
		if (ModDimensions.isOuterworld(targetWorld.dimension())) {
			return ModBlocks.OXIDIZED_BASALT.defaultBlockState();
		}
		return Blocks.STONE.defaultBlockState();
	}

	private static boolean isOnCooldown(Entity entity, ServerLevel world) {
		Long readyAt = TELEPORT_COOLDOWNS.get(entity);
		return readyAt != null && world.getGameTime() < readyAt;
	}

	private static void setCooldown(Entity entity, ServerLevel world) {
		TELEPORT_COOLDOWNS.put(entity, world.getGameTime() + TELEPORT_COOLDOWN_TICKS);
	}
}
