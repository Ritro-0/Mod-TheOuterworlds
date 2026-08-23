package com.theouterworld.block;

import com.theouterworld.entity.OxidizableIronGolemEntity;
import com.theouterworld.registry.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

/**
 * Iron Golem Statue block - a petrified iron golem at living-golem scale.
 * Can be scraped with an axe to de-oxidize, and scraping the unaffected version
 * will reanimate it into a living iron golem.
 */
public class IronGolemStatueBlock extends AbstractIronGolemStatueBlock implements WeatheringCopper {

	public static final MapCodec<IronGolemStatueBlock> CODEC = simpleCodec(settings ->
		new IronGolemStatueBlock(WeatherState.UNAFFECTED, settings));

	private final WeatherState oxidationLevel;
	private Block waxedVersion;

	@Override
	protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
		return CODEC;
	}

	public IronGolemStatueBlock(WeatherState oxidationLevel, Properties settings) {
		super(settings);
		this.oxidationLevel = oxidationLevel;
	}

	public void setWaxedVersion(Block waxedVersion) {
		this.waxedVersion = waxedVersion;
	}

	@Override
	public WeatherState getAge() {
		return this.oxidationLevel;
	}

	@Override
	public Optional<BlockState> getNext(BlockState state) {
		return WeatheringCopper.getNext(state.getBlock()).map(block -> block.withPropertiesOf(state));
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER
			&& WeatheringCopper.getNext(state.getBlock()).isPresent();
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
			return;
		}
		tryPlaceMissingUpper(world, pos, state);
		if (OxidizableIronBehavior.shouldOxidize(world, pos)) {
			this.getNext(state).ifPresent(next -> {
				if (random.nextFloat() < 0.05688889f) {
					replaceBothHalves(world, pos, state, next.getBlock());
				}
			});
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(player.getUsedItemHand());

		if (stack.is(Items.HONEYCOMB) && waxedVersion != null) {
			if (!world.isClientSide()) {
				replaceBothHalves(world, pos, state, waxedVersion);
				world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
				world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);

				if (!player.isCreative()) {
					stack.shrink(1);
				}
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.getItem() instanceof AxeItem) {
			if (this.oxidationLevel == WeatherState.UNAFFECTED) {
				if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
					reanimateGolem(serverWorld, pos, state);

					if (!player.isCreative()) {
						stack.hurtAndBreak(1, player, player.getUsedItemHand());
					}
				}
				return InteractionResult.SUCCESS;
			}

			Optional<Block> previousBlock = WeatheringCopper.getPrevious(state.getBlock());
			if (previousBlock.isPresent()) {
				if (!world.isClientSide()) {
					replaceBothHalves(world, pos, state, previousBlock.get());
					world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
					world.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);

					if (world instanceof ServerLevel serverWorld) {
						BlockPos lower = lowerPos(pos, state);
						for (int i = 0; i < 10; i++) {
							serverWorld.sendParticles(ParticleTypes.SCRAPE,
								lower.getX() + 0.5 + world.getRandom().nextGaussian() * 0.3,
								lower.getY() + 1.35 + world.getRandom().nextGaussian() * 0.5,
								lower.getZ() + 0.5 + world.getRandom().nextGaussian() * 0.3,
								1, 0, 0, 0, 0);
						}
					}

					if (!player.isCreative()) {
						stack.hurtAndBreak(1, player, player.getUsedItemHand());
					}
				}
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	private void reanimateGolem(ServerLevel world, BlockPos pos, BlockState state) {
		float bodyYaw = 0;
		float headYaw = 0;
		float headPitch = 0;
		net.minecraft.network.chat.Component customName = null;

		IronGolemStatueBlockEntity statueEntity = getStatueEntity(world, pos, state);
		if (statueEntity != null) {
			bodyYaw = statueEntity.getBodyYaw();
			headYaw = statueEntity.getHeadYaw();
			headPitch = statueEntity.getHeadPitch();
			customName = statueEntity.getCustomName();
		}

		BlockPos lower = lowerPos(pos, state);
		removeBothHalves(world, pos, state);

		OxidizableIronGolemEntity golem = ModEntities.OXIDIZABLE_IRON_GOLEM.create(world, EntitySpawnReason.CONVERSION);
		if (golem != null) {
			Direction facing = state.getValue(FACING);
			float rotation = switch (facing) {
				case NORTH -> 180.0f;
				case SOUTH -> 0.0f;
				case WEST -> 90.0f;
				case EAST -> 270.0f;
				default -> 0.0f;
			};
			golem.snapTo(
				lower.getX() + 0.5,
				lower.getY(),
				lower.getZ() + 0.5,
				rotation,
				0
			);

			golem.yBodyRot = bodyYaw;
			golem.yHeadRot = headYaw;
			golem.setXRot(headPitch);
			golem.setOxidationLevel(0);

			if (customName != null) {
				golem.setCustomName(customName);
			}

			world.addFreshEntity(golem);
			world.playSound(null, lower, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
	}
}
