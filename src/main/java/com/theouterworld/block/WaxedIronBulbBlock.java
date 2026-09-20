package com.theouterworld.block;

import net.minecraft.tags.ItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Waxed iron bulb - works like copper bulbs but cannot oxidize.
 * Uses scheduled ticks for redstone updates.
 */
public class WaxedIronBulbBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    
    private final Block unwaxedVersion;

    public WaxedIronBulbBlock(Block unwaxedVersion, Properties settings) {
        super(settings);
        this.unwaxedVersion = unwaxedVersion;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.is(state.getBlock()) && world instanceof ServerLevel serverWorld) {
            // Schedule immediate tick for initial power check
            serverWorld.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess scheduledTickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (world instanceof ServerLevel serverWorld) {
            boolean poweredNow = serverWorld.hasNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);
            if (poweredNow && !wasPowered) {
                // Rising edge: schedule tick for toggle (matches vanilla CopperBulbBlock exactly)
                scheduledTickView.scheduleTick(pos, this, 1);
            }
            if (poweredNow != wasPowered) {
                // Update POWERED state for model/observers/comparator
                return state.setValue(POWERED, poweredNow);
            }
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        // Only toggle if still powered (prevents short pulses from toggling)
        if (!state.getValue(POWERED) || !world.hasNeighborSignal(pos)) {
            return;
        }
        // Toggle LIT
        BlockState newState = state.cycle(LIT);
        world.setBlock(pos, newState, Block.UPDATE_ALL);
        // Play iron block click sound (using metal block sounds)
        world.playSound(null, pos, newState.getValue(LIT) ? SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON : SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
                        SoundSource.BLOCKS, 0.4F, newState.getValue(LIT) ? 0.8F : 1.2F);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        WeatheringCopper.WeatherState age = unwaxedVersion instanceof OxidizableIronBulbBlock oxidizable
            ? oxidizable.getAge()
            : WeatheringCopper.WeatherState.UNAFFECTED;
        return OxidizableIronBehavior.bulbComparatorSignal(state.getValue(LIT), age);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        if (stack.is(ItemTags.AXES)) {
            if (!world.isClientSide()) {
                BlockState newState = unwaxedVersion.defaultBlockState()
                    .setValue(LIT, state.getValue(LIT))
                    .setValue(POWERED, state.getValue(POWERED));
                world.setBlockAndUpdate(pos, newState);
                world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                world.playSound(null, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, player.getUsedItemHand());
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }

    public Block getUnwaxedVersion() {
        return unwaxedVersion;
    }
}
