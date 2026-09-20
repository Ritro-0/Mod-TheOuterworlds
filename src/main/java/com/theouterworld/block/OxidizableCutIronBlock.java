package com.theouterworld.block;

import net.minecraft.tags.ItemTags;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class OxidizableCutIronBlock extends Block implements WeatheringCopper {
    private final WeatheringCopper.WeatherState degradationLevel;
    private Block waxedVersion;

    public OxidizableCutIronBlock(WeatheringCopper.WeatherState degradationLevel, Properties settings) {
        super(settings);
        this.degradationLevel = degradationLevel;
    }

    public void setWaxedVersion(Block waxedVersion) {
        this.waxedVersion = waxedVersion;
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
                world.setBlockAndUpdate(pos, nextState.get());
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
        return WeatheringCopper.getNext(state.getBlock()).map(block -> block.withPropertiesOf(state));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        
        // Right-click with honeycomb to wax
        if (stack.is(Items.HONEYCOMB) && waxedVersion != null) {
            if (!world.isClientSide()) {
                world.setBlockAndUpdate(pos, waxedVersion.withPropertiesOf(state));
                world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Right-click with axe to de-oxidize one stage
        if (stack.is(ItemTags.AXES)) {
            Optional<Block> previousBlock = WeatheringCopper.getPrevious(state.getBlock());
            if (previousBlock.isPresent()) {
                if (!world.isClientSide()) {
                    world.setBlockAndUpdate(pos, previousBlock.get().withPropertiesOf(state));
                    world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                    world.playSound(null, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    
                    if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, player.getUsedItemHand());
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
}

