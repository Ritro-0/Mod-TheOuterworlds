package com.theouterworld.block;

import com.theouterworld.registry.ModFluids;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Amberworld reed. Grows like sugar cane — one block, then two, then three —
 * and the third block then ripens instead of adding a fourth. Only a stalk
 * with that ripe tip drops anything. Breaking the tip lets the third block
 * grow back and ripen again.
 */
public class TholinStalkBlock extends Block implements BonemealableBlock {
	public static final BooleanProperty MATURE = BooleanProperty.create("mature");
	/** Set by worldgen. Player-placed stalks stay false, so breeding ignores them. */
	public static final BooleanProperty WILD = BooleanProperty.create("wild");
	/** Sugar cane spends sixteen random ticks on each new block. This matches that wait. */
	private static final int GROW_ODDS = 16;
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public TholinStalkBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(MATURE, false).setValue(WILD, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MATURE, WILD);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(MATURE);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(GROW_ODDS) != 0) {
			return;
		}
		tryGrow(level, pos, state);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		BlockPos top = top(level, pos);
		BlockState crown = level.getBlockState(top);
		return crown.is(this) && !crown.getValue(MATURE) && level.getBlockState(top.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		BlockPos top = top(level, pos);
		tryGrow(level, top, level.getBlockState(top));
	}

	/**
	 * One growth step from the top of the plant. Below three blocks it adds a block.
	 * At three it ripens the tip, and it never stacks a fourth block on its own.
	 */
	public static void tryGrow(ServerLevel level, BlockPos pos, BlockState state) {
		if (!state.is(ModBlocks.THOLIN_STALK) || state.getValue(MATURE)) {
			return;
		}
		if (!level.getBlockState(pos.above()).isAir()) {
			return;
		}
		if (heightAt(level, pos) >= 3) {
			level.setBlock(pos, state.setValue(MATURE, true), 2);
			return;
		}
		level.setBlock(
			pos.above(),
			ModBlocks.THOLIN_STALK.defaultBlockState().setValue(WILD, state.getValue(WILD)),
			2
		);
	}

	/** How many stalk blocks this one sits on, including itself. */
	public static int heightAt(LevelReader level, BlockPos pos) {
		int height = 1;
		while (height < 16 && level.getBlockState(pos.below(height)).is(ModBlocks.THOLIN_STALK)) {
			height++;
		}
		return height;
	}

	public static BlockPos top(LevelReader level, BlockPos pos) {
		BlockPos top = pos;
		for (int i = 0; i < 16; i++) {
			BlockPos above = top.above();
			if (!level.getBlockState(above).is(ModBlocks.THOLIN_STALK)) {
				break;
			}
			top = above;
		}
		return top;
	}

	/**
	 * Weavers take the ripe tip, and sometimes the block under it. The base is left
	 * so the stalk can put a third block back and ripen it again.
	 *
	 * @return how many stalk items that pluck produced
	 */
	public static int pluck(ServerLevel level, BlockPos tip, boolean second) {
		BlockState state = level.getBlockState(tip);
		if (!state.is(ModBlocks.THOLIN_STALK) || !state.getValue(MATURE) || !level.getBlockState(tip.above()).isAir()) {
			return 0;
		}
		int drops = 2 + level.getRandom().nextInt(4);
		breakQuiet(level, tip, state);
		if (second) {
			BlockPos mid = tip.below();
			BlockState midState = level.getBlockState(mid);
			if (midState.is(ModBlocks.THOLIN_STALK) && level.getBlockState(mid.below()).is(ModBlocks.THOLIN_STALK)) {
				drops++;
				breakQuiet(level, mid, midState);
			}
		}
		return drops;
	}

	private static void breakQuiet(ServerLevel level, BlockPos pos, BlockState state) {
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState ground = level.getBlockState(below);
		if (ground.is(this)) {
			return true;
		}
		return ground.isFaceSturdy(level, below, Direction.UP) && hasMethane(level, below);
	}

	/** Like sugar cane: the soil block itself shares a side with methane. */
	public static boolean hasMethane(LevelReader level, BlockPos ground) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (isMethane(level, ground.relative(direction))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMethane(LevelReader level, BlockPos pos) {
		FluidState fluid = level.getFluidState(pos);
		return fluid.getType().isSame(ModFluids.LIQUID_METHANE);
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		RandomSource random
	) {
		if (!state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
		if (origin == null) {
			return List.of();
		}
		ServerLevel level = params.getLevel();
		BlockPos pos = BlockPos.containing(origin);
		if (!harvestable(level, pos, state)) {
			return List.of();
		}
		int count = state.getValue(MATURE) ? 2 + level.getRandom().nextInt(4) : 1;
		return List.of(new ItemStack(this, count));
	}

	/** A stalk drops nothing until some block in the column is a ripe tip. */
	public static boolean harvestable(LevelReader level, BlockPos pos, BlockState state) {
		if (state.is(ModBlocks.THOLIN_STALK) && state.getValue(MATURE)) {
			return true;
		}
		BlockPos cursor = pos.below();
		for (int i = 0; i < 16 && level.getBlockState(cursor).is(ModBlocks.THOLIN_STALK); i++) {
			if (level.getBlockState(cursor).getValue(MATURE)) {
				return true;
			}
			cursor = cursor.below();
		}
		cursor = pos.above();
		for (int i = 0; i < 16 && level.getBlockState(cursor).is(ModBlocks.THOLIN_STALK); i++) {
			if (level.getBlockState(cursor).getValue(MATURE)) {
				return true;
			}
			cursor = cursor.above();
		}
		return false;
	}

	/** A wild plant and whether its tip is still ripe. Mature tips win over a bare stalk. */
	public record WildStand(BlockPos pos, boolean mature) {
	}

	public static @Nullable WildStand nearestWild(LevelReader level, BlockPos origin, int radius) {
		BlockPos ripe = null;
		BlockPos any = null;
		double ripeDist = (double) radius * radius;
		double anyDist = ripeDist;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int y0 = Math.min(62, origin.getY() - 8);
		int y1 = Math.max(80, origin.getY() + 8);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				for (int y = y0; y <= y1; y++) {
					boolean shoreBand = y >= 62 && y <= 80;
					boolean atFeet = Math.abs(y - origin.getY()) <= 8;
					if (!shoreBand && !atFeet) {
						continue;
					}
					cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!state.is(ModBlocks.THOLIN_STALK) || !state.getValue(WILD)) {
						continue;
					}
					double dist = origin.distSqr(cursor);
					if (dist < anyDist) {
						anyDist = dist;
						any = cursor.immutable();
					}
					if (state.getValue(MATURE) && level.getBlockState(cursor.above()).isAir() && dist < ripeDist) {
						ripeDist = dist;
						ripe = cursor.immutable();
					}
				}
			}
		}
		if (ripe != null) {
			return new WildStand(ripe, true);
		}
		return any == null ? null : new WildStand(any, false);
	}

	/** Wild stalks along the surface, out to {@code radius} from {@code origin}. */
	public static @Nullable WildStand nearestWildSurface(ServerLevel level, BlockPos origin, int radius) {
		BlockPos ripe = null;
		BlockPos any = null;
		double ripeDist = (double) radius * radius;
		double anyDist = ripeDist;
		int radiusSqr = radius * radius;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radiusSqr) {
					continue;
				}
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
				int bottom = Math.max(level.getMinY(), top - 8);
				for (int y = top + 2; y >= bottom; y--) {
					cursor.set(x, y, z);
					BlockState state = level.getBlockState(cursor);
					if (!state.is(ModBlocks.THOLIN_STALK)) {
						if (y < top && !state.isAir()) {
							break;
						}
						continue;
					}
					if (!state.getValue(WILD)) {
						continue;
					}
					double dist = origin.distSqr(cursor);
					if (dist < anyDist) {
						anyDist = dist;
						any = cursor.immutable();
					}
					if (state.getValue(MATURE) && level.getBlockState(cursor.above()).isAir() && dist < ripeDist) {
						ripeDist = dist;
						ripe = cursor.immutable();
					}
				}
			}
		}
		if (ripe != null) {
			return new WildStand(ripe, true);
		}
		return any == null ? null : new WildStand(any, false);
	}

	/** A ripe tip that worldgen planted, within {@code radius} blocks of {@code origin}. */
	public static @Nullable BlockPos findWildMatureTip(LevelReader level, BlockPos origin, int radius) {
		WildStand stand = nearestWild(level, origin, radius);
		return stand != null && stand.mature() ? stand.pos() : null;
	}

	public static @Nullable BlockPos findMatureTip(LevelReader level, BlockPos origin, int radius) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -6; dy <= 8; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (dx * dx + dz * dz > radius * radius) {
						continue;
					}
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!state.is(ModBlocks.THOLIN_STALK) || !state.getValue(MATURE)) {
						continue;
					}
					if (!level.getBlockState(cursor.above()).isAir()) {
						continue;
					}
					double dist = origin.distSqr(cursor);
					if (dist < bestDist) {
						bestDist = dist;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}
}
