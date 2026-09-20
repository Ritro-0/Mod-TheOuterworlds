package com.theouterworld.entity;

import net.minecraft.tags.ItemTags;

import com.theouterworld.block.AbstractIronGolemStatueBlock;
import com.theouterworld.block.IronGolemStatueBlockEntity;
import com.theouterworld.block.ModBlocks;
import com.theouterworld.block.OxidizableIronBehavior;
import com.theouterworld.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * An Iron Golem that oxidizes over time in the Outerworld dimension.
 * Oxidation reduces movement speed and attack damage.
 * Can be waxed with honeycomb to prevent oxidation.
 * Can be scraped with axe to remove wax or de-oxidize.
 */
public class OxidizableIronGolemEntity extends IronGolem {
    
    private static final EntityDataAccessor<Integer> OXIDATION_LEVEL = SynchedEntityData.defineId(
        OxidizableIronGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WAXED = SynchedEntityData.defineId(
        OxidizableIronGolemEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Base oxidation chance per tick (scaled by randomTickSpeed)
    // With default randomTickSpeed=3, similar to block oxidation timing
    private static final float BASE_OXIDATION_CHANCE_PER_TICK = 0.0001f;
    
    // Petrification chance: 1/172 per tick when fully oxidized (~0.58%)
    private static final float PETRIFICATION_CHANCE = 1.0f / 172.0f;
    
    // Stat modifiers per oxidation level
    private static final float[] SPEED_MODIFIERS = {1.0f, 0.85f, 0.65f, 0.4f};
    private static final float[] DAMAGE_MODIFIERS = {1.0f, 0.9f, 0.75f, 0.5f};
    private static final float[] KNOCKBACK_MODIFIERS = {1.0f, 0.9f, 0.75f, 0.6f};

    public OxidizableIronGolemEntity(EntityType<? extends IronGolem> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createOxidizableIronGolemAttributes() {
        return IronGolem.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OXIDATION_LEVEL, 0);
        builder.define(WAXED, false);
    }

    public void writeNbt(CompoundTag nbt) {
        nbt.putInt("OxidationLevel", getOxidationLevel());
        nbt.putBoolean("Waxed", isWaxed());
    }

    public void readNbt(CompoundTag nbt) {
        nbt.getInt("OxidationLevel").ifPresent(this::setOxidationLevel);
        nbt.getBoolean("Waxed").ifPresent(this::setWaxed);
    }

    public int getOxidationLevel() {
        return this.entityData.get(OXIDATION_LEVEL);
    }

    public void setOxidationLevel(int level) {
        this.entityData.set(OXIDATION_LEVEL, Math.max(0, Math.min(3, level)));
    }

    public WeatheringCopper.WeatherState getOxidationState() {
        return switch (getOxidationLevel()) {
            case 0 -> WeatheringCopper.WeatherState.UNAFFECTED;
            case 1 -> WeatheringCopper.WeatherState.EXPOSED;
            case 2 -> WeatheringCopper.WeatherState.WEATHERED;
            default -> WeatheringCopper.WeatherState.OXIDIZED;
        };
    }

    public boolean isWaxed() {
        return this.entityData.get(WAXED);
    }

    public void setWaxed(boolean waxed) {
        this.entityData.set(WAXED, waxed);
    }

    @Override
    public void tick() {
        super.tick();
        
        Level world = level();
        // Only process on server side
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            // Check for petrification first (only when fully oxidized)
            if (getOxidationLevel() == 3 && !isWaxed() && canPetrify()) {
                // Get randomTickSpeed to scale petrification chance for testing
                int randomTickSpeed = serverWorld.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
                float petrifyChance = PETRIFICATION_CHANCE * (randomTickSpeed / 3.0f);
                
                if (random.nextFloat() < petrifyChance) {
                    petrify(serverWorld);
                    return; // Entity is removed, don't continue
                }
            }
            
            // Oxidation logic (only in Outerworld, when not waxed and not fully oxidized)
            if (OxidizableIronBehavior.shouldOxidize(serverWorld, blockPosition()) && !isWaxed() && getOxidationLevel() < 3) {
                int randomTickSpeed = serverWorld.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
                
                if (randomTickSpeed > 0) {
                    float baseChance = OxidizableIronBehavior.getOxidationChance(getOxidationState(), 0);
                    float scaledChance = BASE_OXIDATION_CHANCE_PER_TICK * randomTickSpeed * baseChance;
                    
                    if (random.nextFloat() < scaledChance) {
                        oxidize();
                    }
                }
            }
        }
    }
    
    /**
     * Check if the golem can petrify (turn into a statue).
     * Must be in air blocks (not in water, lava, or partially inside solid blocks).
     */
    private boolean canPetrify() {
        BlockPos feet = this.blockPosition();
        return !this.isUnderWater() &&
               !this.isInLava() &&
               this.onGround() &&
               level().getBlockState(feet).canBeReplaced() &&
               level().getBlockState(feet.above()).canBeReplaced();
    }
    
    /**
     * Turn this golem into a statue block.
     */
    private void petrify(ServerLevel world) {
        // Get the statue block based on oxidation level (always oxidized for petrification)
        // Convert yaw to cardinal direction
        Direction facing = Direction.fromYRot(this.getYRot());
        BlockState statueState = ModBlocks.OXIDIZED_IRON_GOLEM_STATUE.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
            .setValue(AbstractIronGolemStatueBlock.HALF, DoubleBlockHalf.LOWER);
        
        // Place the lower half; onPlace fills in the upper half
        world.setBlockAndUpdate(blockPosition(), statueState);
        
        // Get the block entity and store golem data
        if (world.getBlockEntity(blockPosition()) instanceof IronGolemStatueBlockEntity statueEntity) {
            // Store pose data
            statueEntity.setBodyYaw(this.yBodyRot);
            statueEntity.setHeadYaw(this.yHeadRot);
            statueEntity.setHeadPitch(this.getXRot());
            
            // Store custom name if present
            if (this.hasCustomName()) {
                statueEntity.setCustomName(this.getCustomName());
            }
            
            statueEntity.setChanged();
        }
        
        // Play petrification sound
        world.playSound(null, getX(), getY(), getZ(), 
            SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.5f);
        
        // Remove the golem entity
        this.discard();
    }

    private void oxidize() {
        if (getOxidationLevel() < 3) {
            setOxidationLevel(getOxidationLevel() + 1);
            // Play oxidation sound
            level().playSound(null, getX(), getY(), getZ(), 
                SoundEvents.AXE_SCRAPE.value(), SoundSource.NEUTRAL, 1.0f, 0.8f);
        }
    }

    @Override
    public float getSpeed() {
        float baseSpeed = super.getSpeed();
        return baseSpeed * SPEED_MODIFIERS[getOxidationLevel()];
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, net.minecraft.world.entity.Entity target) {
        // Get base attack damage from attributes
        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float baseKnockback = (float) this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        
        // Apply oxidation modifiers
        float damage = baseDamage * DAMAGE_MODIFIERS[getOxidationLevel()];
        float knockback = baseKnockback * KNOCKBACK_MODIFIERS[getOxidationLevel()];
        
        // Deal damage manually with our modified values
        if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
            boolean success = livingTarget.hurtServer(world, this.damageSources().mobAttack(this), damage);
            if (success) {
                // Apply knockback
                if (knockback > 0) {
                    livingTarget.knockback(
                        knockback * 0.5,
                        net.minecraft.util.Mth.sin(this.getYRot() * ((float) Math.PI / 180F)),
                        -net.minecraft.util.Mth.cos(this.getYRot() * ((float) Math.PI / 180F)),
                        this.damageSources().mobAttack(this),
                        damage
                    );
                }
                this.setLastHurtMob(target);
            }
            return success;
        }
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Level world = level();
        
        // Honeycomb waxing
        if (stack.is(Items.HONEYCOMB) && !isWaxed()) {
            if (!world.isClientSide()) {
                setWaxed(true);
                world.playSound(null, getX(), getY(), getZ(), 
                    SoundEvents.HONEYCOMB_WAX_ON, SoundSource.NEUTRAL, 1.0f, 1.0f);
                
                // Spawn wax particles
                if (world instanceof ServerLevel serverWorld) {
                    for (int i = 0; i < 10; i++) {
                        serverWorld.sendParticles(ParticleTypes.WAX_ON,
                            getX() + random.nextGaussian() * 0.5,
                            getY() + 1.0 + random.nextGaussian() * 0.5,
                            getZ() + random.nextGaussian() * 0.5,
                            1, 0, 0, 0, 0);
                    }
                }
                
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Axe interactions
        if (stack.is(ItemTags.AXES)) {
            // If waxed, remove wax first
            if (isWaxed()) {
                if (!world.isClientSide()) {
                    setWaxed(false);
                    world.playSound(null, getX(), getY(), getZ(), 
                        SoundEvents.AXE_WAX_OFF.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                    
                    // Spawn wax off particles
                    if (world instanceof ServerLevel serverWorld) {
                        for (int i = 0; i < 10; i++) {
                            serverWorld.sendParticles(ParticleTypes.WAX_OFF,
                                getX() + random.nextGaussian() * 0.5,
                                getY() + 1.0 + random.nextGaussian() * 0.5,
                                getZ() + random.nextGaussian() * 0.5,
                                1, 0, 0, 0, 0);
                        }
                    }
                    
                    if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, hand);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            
            // If not waxed, de-oxidize one stage
            if (getOxidationLevel() > 0) {
                if (!world.isClientSide()) {
                    setOxidationLevel(getOxidationLevel() - 1);
                    world.playSound(null, getX(), getY(), getZ(), 
                        SoundEvents.AXE_SCRAPE.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                    
                    // Spawn scrape particles
                    if (world instanceof ServerLevel serverWorld) {
                        for (int i = 0; i < 10; i++) {
                            serverWorld.sendParticles(ParticleTypes.SCRAPE,
                                getX() + random.nextGaussian() * 0.5,
                                getY() + 1.0 + random.nextGaussian() * 0.5,
                                getZ() + random.nextGaussian() * 0.5,
                                1, 0, 0, 0, 0);
                        }
                    }
                    
                    if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, hand);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        
        // Fall back to parent interaction (iron ingot healing)
        return super.mobInteract(player, hand);
    }

    // Override to scale damage dealt based on oxidation
    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return super.hurtServer(world, source, amount);
    }
}

