package com.theouterworld.item;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.world.DimensionClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class MercuryBucketItem extends BucketItem {
	public MercuryBucketItem(Fluid content, Properties properties) {
		super(content, properties);
	}

	@Override
	public boolean emptyContents(@Nullable LivingEntity user, Level level, BlockPos pos, @Nullable BlockHitResult hitResult) {
		if (!(this.getContent() instanceof FlowingFluid)) {
			return false;
		}
		if (!DimensionClimate.shouldSolidifyMercury(level, pos)) {
			return super.emptyContents(user, level, pos, hitResult);
		}

		BlockState blockState = level.getBlockState(pos);
		Block block = blockState.getBlock();
		boolean mayReplace = blockState.canBeReplaced(this.getContent());
		boolean shiftKeyDown = user != null && user.isShiftKeyDown();
		boolean placeLiquid = mayReplace || block instanceof LiquidBlockContainer container
			&& container.canPlaceLiquid(user, level, pos, blockState, this.getContent());
		boolean canPlace = blockState.isAir() || placeLiquid && (!shiftKeyDown || hitResult == null);
		if (!canPlace) {
			return hitResult != null && this.emptyContents(user, level, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
		}

		if (!level.isClientSide() && mayReplace && !blockState.liquid()) {
			level.destroyBlock(pos, true);
		}
		if (!level.setBlock(pos, ModBlocks.MERCURY_BLOCK.defaultBlockState(), 11)) {
			return false;
		}
		this.playEmptySound(user, level, pos);
		return true;
	}
}
