package com.theouterworld.video.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * TEMP VIDEO FEATURE — falling-tree style: real blocks become {@link Display.BlockDisplay}s,
 * eyes become item displays, and a controller block entity drives the gag.
 */
public final class PatrickActivator {
	private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

	private PatrickActivator() {
	}

	public static Result awaken(ServerLevel world, PatrickMatcher.Match match) {
		BlockPos origin = match.origin();
		Rotation rotation = match.rotation();
		List<PatrickTemplate.PlacedBlock> blocks = PatrickTemplate.placedBlocks(rotation);

		List<Vec3> eyeOrigins = new ArrayList<>(PatrickMatcher.eyeOrigins(world, origin, rotation));
		removeFrames(world, origin, rotation);

		List<UUID> displayIds = new ArrayList<>();

		for (PatrickTemplate.PlacedBlock placed : blocks) {
			BlockPos worldPos = origin.offset(placed.local());
			BlockState state = placed.state();
			if (state.is(Blocks.GLOW_LICHEN) && world.getBlockState(worldPos).isAir()) {
				continue;
			}
			world.setBlock(worldPos, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);

			Display.BlockDisplay display = EntityTypes.BLOCK_DISPLAY.create(world, EntitySpawnReason.COMMAND);
			if (display == null) {
				continue;
			}
			display.snapTo(worldPos.getX(), worldPos.getY(), worldPos.getZ(), 0.0F, 0.0F);
			display.setBlockState(state);
			display.setBrightnessOverride(Brightness.FULL_BRIGHT);
			display.setTransformation(new com.mojang.math.Transformation(
				new Vector3f(0.0F, 0.0F, 0.0F),
				new Quaternionf(),
				new Vector3f(1.0F, 1.0F, 1.0F),
				new Quaternionf()
			));
			world.addFreshEntity(display);
			displayIds.add(display.getUUID());
		}

		for (Vec3 eye : eyeOrigins) {
			Display.ItemDisplay eyeDisplay = EntityTypes.ITEM_DISPLAY.create(world, EntitySpawnReason.COMMAND);
			if (eyeDisplay == null) {
				continue;
			}
			eyeDisplay.snapTo(eye.x, eye.y, eye.z, 0.0F, 0.0F);
			eyeDisplay.setItemStack(new ItemStack(Items.REDSTONE_BLOCK));
			eyeDisplay.setBrightnessOverride(Brightness.FULL_BRIGHT);
			eyeDisplay.setTransformation(new com.mojang.math.Transformation(
				new Vector3f(-0.15F, -0.15F, -0.15F),
				new Quaternionf(),
				new Vector3f(0.35F, 0.35F, 0.35F),
				new Quaternionf()
			));
			world.addFreshEntity(eyeDisplay);
			displayIds.add(eyeDisplay.getUUID());
		}

		BlockPos controllerPos = origin;
		world.setBlock(controllerPos, PatrickVideo.CONTROLLER.defaultBlockState(), PLACE_FLAGS);
		if (world.getBlockEntity(controllerPos) instanceof PatrickControllerBlockEntity controller) {
			controller.configure(
				rotation,
				PatrickMatcher.lookTarget(origin, rotation),
				eyeOrigins,
				displayIds
			);
		}

		return new Result(controllerPos, displayIds.size(), eyeOrigins.size());
	}

	public static void spawnStructure(ServerLevel world, BlockPos origin, Rotation rotation) {
		for (PatrickTemplate.PlacedBlock placed : PatrickTemplate.placedBlocks(rotation)) {
			BlockPos worldPos = origin.offset(placed.local());
			world.setBlock(worldPos, placed.state(), PLACE_FLAGS);
		}
		for (PatrickTemplate.PlacedFrame frame : PatrickTemplate.placedFrames(rotation)) {
			BlockPos attach = origin.offset(frame.local());
			Direction facing = frame.facing();
			// Ensure support is solid and the cell the frame occupies is empty.
			BlockPos frameCell = attach.relative(facing);
			if (!world.getBlockState(frameCell).isAir()) {
				world.setBlock(frameCell, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
			}
			GlowItemFrame itemFrame = new GlowItemFrame(world, attach, facing);
			itemFrame.setItem(new ItemStack(Items.REDSTONE_BLOCK), false);
			itemFrame.setInvisible(false);
			world.addFreshEntity(itemFrame);
		}
	}

	/** Patrick faces the player (head toward them). */
	public static Rotation rotationFromFacing(Direction playerLook) {
		Direction patrickFaces = playerLook.getOpposite();
		return switch (patrickFaces) {
			case EAST -> Rotation.NONE;
			case SOUTH -> Rotation.CLOCKWISE_90;
			case WEST -> Rotation.CLOCKWISE_180;
			case NORTH -> Rotation.COUNTERCLOCKWISE_90;
			default -> Rotation.NONE;
		};
	}

	/** Origin so Patrick stands just in front of the player, facing them. */
	public static BlockPos originInFrontOf(BlockPos playerPos, Direction playerLook) {
		Rotation rotation = rotationFromFacing(playerLook);
		var size = PatrickTemplate.sizeFor(rotation);
		Direction patrickFaces = playerLook.getOpposite();
		BlockPos feet = playerPos.relative(playerLook, 2);
		return switch (patrickFaces) {
			case EAST -> feet.offset(0, 0, -(size.getZ() / 2));
			case WEST -> feet.offset(-(size.getX() - 1), 0, -(size.getZ() / 2));
			case SOUTH -> feet.offset(-(size.getX() / 2), 0, 0);
			case NORTH -> feet.offset(-(size.getX() / 2), 0, -(size.getZ() - 1));
			default -> feet;
		};
	}

	private static void removeFrames(ServerLevel world, BlockPos origin, Rotation rotation) {
		AABB box = PatrickMatcher.structureBox(origin, rotation).inflate(2.0);
		for (GlowItemFrame frame : world.getEntitiesOfClass(GlowItemFrame.class, box, f -> true)) {
			frame.discard();
		}
	}

	public record Result(BlockPos controllerPos, int displayCount, int eyeCount) {
	}
}
