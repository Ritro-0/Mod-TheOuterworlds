package com.theouterworld.block;

import com.theouterworld.entity.WeaverEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StrawBedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Straw-bed shape with a permanent owner. A Weaver will not lie down in a pad
 * that already belongs to another, and waking up does not give the bunk away.
 */
public class WeaverPadBlock extends StrawBedBlock implements EntityBlock {
	private static final BedRule RULE = new BedRule(
		BedRule.Rule.WHEN_DARK,
		BedRule.Rule.NEVER,
		false,
		false,
		Optional.of(Component.translatable("block.minecraft.bed.no_sleep"))
	);

	public WeaverPadBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BedRule getBedRule(Level level, BlockPos pos) {
		return RULE;
	}

	@Override
	protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
		return EnvironmentAttributes.BED_RULE;
	}

	@Override
	public Identifier getSleptInBedStatType() {
		return Stats.SLEEP_IN_BED;
	}

	@Override
	protected InteractionResult destroyOnUse(BlockState state, Level level, BlockPos pos, Player player) {
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected void destroyOnLeave(Level level, BlockPos pos) {
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WeaverPadBlockEntity(pos, state);
	}

	/** Straw beds have no owner. A pad is free when it is unclaimed or already this Weaver's. */
	public static boolean availableTo(Level level, BlockPos pos, UUID weaverId) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof WeaverPadBlock)) {
			return true;
		}
		if (!allows(level, pos, weaverId)) {
			return false;
		}
		BlockPos other = otherHalf(pos, state);
		return other == null || !(level.getBlockState(other).getBlock() instanceof WeaverPadBlock) || allows(level, other, weaverId);
	}

	/** Writes this Weaver onto both halves. Fails if either half already belongs to someone else. */
	public static boolean claim(Level level, BlockPos pos, UUID weaverId) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof WeaverPadBlock)) {
			return true;
		}
		BlockPos other = otherHalf(pos, state);
		boolean otherIsPad = other != null && level.getBlockState(other).getBlock() instanceof WeaverPadBlock;
		if (!allows(level, pos, weaverId) || (otherIsPad && !allows(level, other, weaverId))) {
			return false;
		}
		remember(level, pos, weaverId);
		if (otherIsPad) {
			remember(level, other, weaverId);
		}
		return true;
	}

	public static void release(Level level, BlockPos pos, UUID weaverId) {
		if (!level.isLoaded(pos)) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof WeaverPadBlock)) {
			return;
		}
		forget(level, pos, weaverId);
		BlockPos other = otherHalf(pos, state);
		if (other != null && level.isLoaded(other) && level.getBlockState(other).getBlock() instanceof WeaverPadBlock) {
			forget(level, other, weaverId);
		}
	}

	/** True when a living sleeper, other than {@code except}, is already in this bunk. */
	public static boolean heldByOther(Level level, BlockPos pos, LivingEntity except) {
		AABB box = new AABB(pos).inflate(1.5);
		for (LivingEntity sleeper : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isSleeping)) {
			if (sleeper == except) {
				continue;
			}
			if (sleeper.getSleepingPos().map(sleep -> sleep.equals(pos) || WeaverEntity.isSameBed(level, sleep, pos)).orElse(false)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Stamp the owner onto a pad that was just placed in the chunk being generated.
	 * The live world cannot answer for that chunk yet; asking it deadlocks generation.
	 */
	public static void claimInPlace(LevelAccessor level, BlockPos pos, UUID weaverId) {
		if (!(level.getBlockState(pos).getBlock() instanceof WeaverPadBlock)) {
			return;
		}
		WeaverPadBlockEntity pad = entity(level, pos);
		if (pad != null) {
			pad.claim(weaverId);
		}
	}

	private static boolean allows(Level level, BlockPos pos, UUID weaverId) {
		WeaverPadBlockEntity pad = entity(level, pos);
		return pad == null || pad.allows(weaverId);
	}

	private static void remember(Level level, BlockPos pos, UUID weaverId) {
		WeaverPadBlockEntity pad = entity(level, pos);
		if (pad != null) {
			pad.claim(weaverId);
		}
	}

	private static void forget(Level level, BlockPos pos, UUID weaverId) {
		if (level.getBlockEntity(pos) instanceof WeaverPadBlockEntity pad) {
			pad.release(weaverId);
		}
	}

	private static @Nullable WeaverPadBlockEntity entity(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof WeaverPadBlock)) {
			return null;
		}
		if (level.getBlockEntity(pos) instanceof WeaverPadBlockEntity pad) {
			return pad;
		}
		if (!(level instanceof Level writable)) {
			return null;
		}
		WeaverPadBlockEntity created = new WeaverPadBlockEntity(pos, state);
		writable.setBlockEntity(created);
		return created;
	}

	private static @Nullable BlockPos otherHalf(BlockPos pos, BlockState state) {
		if (!state.hasProperty(PART) || !state.hasProperty(FACING)) {
			return null;
		}
		BedPart part = state.getValue(PART);
		return pos.relative(part == BedPart.FOOT ? state.getValue(FACING) : state.getValue(FACING).getOpposite());
	}
}
