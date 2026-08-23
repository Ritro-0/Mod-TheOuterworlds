package com.theouterworld.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Iron bulb that works exactly like copper bulbs:
 * - Toggles lit state on rising edge of redstone signal
 * - Uses scheduled ticks for redstone updates
 * - Oxidizes over time in the Outerworld
 */
public class OxidizableIronBulbBlock extends Block implements WeatheringCopper {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    
    private final WeatheringCopper.WeatherState degradationLevel;
    private Block waxedVersion;

    public OxidizableIronBulbBlock(WeatheringCopper.WeatherState degradationLevel, Properties settings) {
        super(settings);
        this.degradationLevel = degradationLevel;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED);
    }

    public void setWaxedVersion(Block waxedVersion) {
        this.waxedVersion = waxedVersion;
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
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        // Only oxidize in Outerworld dimension
        if (OxidizableIronBehavior.shouldOxidize(world, pos)) {
            this.doOxidation(state, world, pos, random);
        }
    }

    private void doOxidation(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        Optional<BlockState> nextState = this.getNext(state);
        if (nextState.isPresent()) {
            float chance = OxidizableIronBehavior.getOxidationChance(this.degradationLevel, 0, this);
            if (random.nextFloat() < chance) {
                // Use NOTIFY_LISTENERS instead of NOTIFY_ALL to prevent neighbor updates
                // This prevents redstone updates that cause flickering during oxidation
                // The block state (LIT/POWERED) is preserved exactly, and updates can happen normally after oxidation
                world.setBlock(pos, nextState.get(), Block.UPDATE_CLIENTS);
                if (state.getValue(LIT)) {
                    world.updateNeighbourForOutputSignal(pos, this);
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.degradationLevel;
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        // Get the next oxidation block and preserve LIT and POWERED properties
        return WeatheringCopper.getNext(state.getBlock()).map(block -> {
            BlockState newState = block.defaultBlockState();
            // Explicitly preserve LIT and POWERED states across oxidation
            if (newState.hasProperty(LIT) && state.hasProperty(LIT)) {
                newState = newState.setValue(LIT, state.getValue(LIT));
            }
            if (newState.hasProperty(POWERED) && state.hasProperty(POWERED)) {
                newState = newState.setValue(POWERED, state.getValue(POWERED));
            }
            return newState;
        });
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return OxidizableIronBehavior.bulbComparatorSignal(state.getValue(LIT), this.degradationLevel);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        if (stack.is(Items.HONEYCOMB) && waxedVersion != null) {
            if (!world.isClientSide()) {
                BlockState newState = waxedVersion.defaultBlockState()
                    .setValue(LIT, state.getValue(LIT))
                    .setValue(POWERED, state.getValue(POWERED));
                world.setBlockAndUpdate(pos, newState);
                world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        if (stack.getItem() instanceof AxeItem) {
            Optional<Block> previousBlock = WeatheringCopper.getPrevious(state.getBlock());
            if (previousBlock.isPresent()) {
                if (!world.isClientSide()) {
                    // Use getStateWithProperties to preserve all compatible properties
                    BlockState newState = previousBlock.get().withPropertiesOf(state);
                    world.setBlockAndUpdate(pos, newState);
                    world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                    world.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    
                    if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, player.getUsedItemHand());
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    public ItemStack getPickStack(LevelReader world, BlockPos pos, BlockState state) {
        // Return vanilla iron block for middle-click in creative (iron bulbs don't exist in vanilla)
        return new ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK);
    }
}
