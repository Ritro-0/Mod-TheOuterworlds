package com.theouterworld.entity;

import com.theouterworld.block.ModBlocks;
import com.theouterworld.mixin.PrimedTntAccessor;
import com.theouterworld.registry.ModEntities;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

public class PrimedPerchlorateCharge extends PrimedTnt {
	public static final float EXPLOSION_POWER = 16.0f;

	public PrimedPerchlorateCharge(EntityType<PrimedPerchlorateCharge> type, Level level) {
		super(type, level);
		applyChargeDefaults();
	}

	public static PrimedPerchlorateCharge create(EntityType<PrimedPerchlorateCharge> type, Level level) {
		return new PrimedPerchlorateCharge(type, level);
	}

	public PrimedPerchlorateCharge(Level level, double x, double y, double z, @Nullable LivingEntity owner) {
		this(ModEntities.PRIMED_PERCHLORATE_CHARGE, level);
		this.setPos(x, y, z);
		double angle = level.getRandom().nextDouble() * (Math.PI * 2.0);
		this.setDeltaMovement(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.setFuse(DEFAULT_FUSE_TIME * 3);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		if (owner != null) {
			((PrimedTntAccessor) this).theouterworlds$setOwner(EntityReference.of(owner));
		}
	}

	private void applyChargeDefaults() {
		((PrimedTntAccessor) this).theouterworlds$setExplosionPower(EXPLOSION_POWER);
		this.setBlockState(ModBlocks.PERCHLORATE_CHARGE.defaultBlockState());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		applyChargeDefaults();
	}
}
