package com.theouterworld.video.patrick;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * TEMP VIDEO FEATURE — baked from patrick.litematic (feet at y=0, head at +X).
 * Delete {@code com.theouterworld.video.patrick} to remove.
 */
public final class PatrickTemplate {
	public static final int SIZE_X = 8;
	public static final int SIZE_Y = 8;
	public static final int SIZE_Z = 4;
	public static final int SEARCH_RADIUS = 80;

	private static final List<BlockEntry> BLOCKS = List.of(
entry(1, 0, 3, "polished_blackstone"),
		entry(4, 0, 3, "polished_blackstone"),
		entry(2, 0, 2, "glow_lichen", "down=false,east=false,north=false,south=false,up=true,waterlogged=false,west=false"),
		entry(2, 0, 1, "glow_lichen", "down=false,east=false,north=false,south=false,up=true,waterlogged=false,west=false"),
		entry(1, 0, 0, "polished_blackstone"),
		entry(4, 0, 0, "polished_blackstone"),
		entry(0, 1, 3, "polished_blackstone_stairs", "facing=east,half=top,shape=straight,waterlogged=false"),
		entry(1, 1, 3, "polished_blackstone_stairs", "facing=west,half=bottom,shape=straight,waterlogged=false"),
		entry(3, 1, 3, "polished_blackstone_stairs", "facing=east,half=top,shape=straight,waterlogged=false"),
		entry(4, 1, 3, "polished_blackstone"),
		entry(1, 1, 2, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(2, 1, 2, "red_nether_bricks"),
		entry(3, 1, 2, "polished_blackstone_slab", "type=bottom,waterlogged=false"),
		entry(4, 1, 2, "polished_blackstone_stairs", "facing=west,half=top,shape=straight,waterlogged=false"),
		entry(1, 1, 1, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(2, 1, 1, "red_nether_bricks"),
		entry(3, 1, 1, "polished_blackstone_slab", "type=bottom,waterlogged=false"),
		entry(4, 1, 1, "polished_blackstone_stairs", "facing=west,half=top,shape=straight,waterlogged=false"),
		entry(0, 1, 0, "polished_blackstone_stairs", "facing=east,half=top,shape=straight,waterlogged=false"),
		entry(1, 1, 0, "polished_blackstone_stairs", "facing=west,half=bottom,shape=straight,waterlogged=false"),
		entry(3, 1, 0, "polished_blackstone_stairs", "facing=east,half=top,shape=straight,waterlogged=false"),
		entry(4, 1, 0, "polished_blackstone"),
		entry(0, 2, 3, "polished_blackstone"),
		entry(1, 2, 3, "polished_blackstone"),
		entry(2, 2, 3, "red_nether_bricks"),
		entry(3, 2, 3, "polished_blackstone"),
		entry(4, 2, 3, "polished_blackstone"),
		entry(0, 2, 2, "polished_blackstone"),
		entry(4, 2, 2, "polished_blackstone"),
		entry(5, 2, 2, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(0, 2, 1, "polished_blackstone"),
		entry(4, 2, 1, "polished_blackstone"),
		entry(5, 2, 1, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(0, 2, 0, "polished_blackstone"),
		entry(1, 2, 0, "red_nether_bricks"),
		entry(2, 2, 0, "red_nether_bricks"),
		entry(3, 2, 0, "polished_blackstone"),
		entry(4, 2, 0, "polished_blackstone"),
		entry(1, 3, 3, "polished_blackstone"),
		entry(3, 3, 3, "polished_blackstone"),
		entry(0, 3, 2, "polished_blackstone"),
		entry(2, 3, 2, "red_nether_bricks"),
		entry(4, 3, 2, "polished_blackstone"),
		entry(5, 3, 2, "polished_blackstone"),
		entry(6, 3, 2, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(0, 3, 1, "polished_blackstone"),
		entry(2, 3, 1, "red_nether_bricks"),
		entry(4, 3, 1, "polished_blackstone"),
		entry(5, 3, 1, "polished_blackstone"),
		entry(6, 3, 1, "polished_blackstone_slab", "type=top,waterlogged=false"),
		entry(1, 3, 0, "polished_blackstone"),
		entry(3, 3, 0, "polished_blackstone"),
		entry(1, 4, 2, "polished_blackstone"),
		entry(2, 4, 2, "glow_lichen", "down=true,east=false,north=false,south=false,up=false,waterlogged=false,west=false"),
		entry(3, 4, 2, "polished_blackstone"),
		entry(5, 4, 2, "polished_blackstone_stairs", "facing=east,half=bottom,shape=straight,waterlogged=false"),
		entry(6, 4, 2, "polished_blackstone"),
		entry(1, 4, 1, "polished_blackstone"),
		entry(2, 4, 1, "glow_lichen", "down=true,east=false,north=false,south=false,up=false,waterlogged=false,west=false"),
		entry(3, 4, 1, "polished_blackstone"),
		entry(5, 4, 1, "polished_blackstone_stairs", "facing=east,half=bottom,shape=straight,waterlogged=false"),
		entry(6, 4, 1, "polished_blackstone"),
		entry(6, 5, 2, "polished_blackstone"),
		entry(6, 5, 1, "polished_blackstone"),
		entry(6, 6, 2, "polished_blackstone"),
		entry(7, 6, 2, "polished_blackstone"),
		entry(6, 6, 1, "polished_blackstone"),
		entry(7, 6, 1, "polished_blackstone"),
		entry(6, 7, 2, "polished_blackstone_stairs", "facing=west,half=bottom,shape=straight,waterlogged=false"),
		entry(6, 7, 1, "polished_blackstone_stairs", "facing=west,half=bottom,shape=straight,waterlogged=false")
	);

	/** Glow item frame attach positions + facing (eyes on the snout). */
	private static final List<FrameEntry> FRAMES = List.of(
		new FrameEntry(7, 6, 1, Direction.EAST),
		new FrameEntry(7, 6, 2, Direction.EAST)
	);

	private PatrickTemplate() {
	}

	public static List<BlockEntry> blocks() {
		return BLOCKS;
	}

	public static List<FrameEntry> frames() {
		return FRAMES;
	}

	public static BlockPos transform(int x, int y, int z, Rotation rotation) {
		return switch (rotation) {
			case NONE -> new BlockPos(x, y, z);
			case CLOCKWISE_90 -> new BlockPos(SIZE_Z - 1 - z, y, x);
			case CLOCKWISE_180 -> new BlockPos(SIZE_X - 1 - x, y, SIZE_Z - 1 - z);
			case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, SIZE_X - 1 - x);
		};
	}

	public static Vec3i sizeFor(Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
			return new Vec3i(SIZE_Z, SIZE_Y, SIZE_X);
		}
		return new Vec3i(SIZE_X, SIZE_Y, SIZE_Z);
	}

	/** Match ignoring stair {@code shape} and optional decorative lichen. */
	public static boolean statesMatch(BlockState worldState, BlockState expected) {
		if (expected.is(net.minecraft.world.level.block.Blocks.GLOW_LICHEN)) {
			// Lichen is decorative and often broken/replaced; don't require it.
			return worldState.isAir() || worldState.is(expected.getBlock());
		}
		if (!worldState.is(expected.getBlock())) {
			return false;
		}
		for (Property<?> property : expected.getProperties()) {
			if ("shape".equals(property.getName())) {
				continue;
			}
			if (!worldState.hasProperty(property)) {
				return false;
			}
			if (!worldState.getValue(property).equals(expected.getValue(property))) {
				return false;
			}
		}
		return true;
	}

	public static Direction rotateFacing(Direction facing, Rotation rotation) {
		return rotation.rotate(facing);
	}

	public static BlockState rotateState(BlockState state, Rotation rotation) {
		return state.rotate(rotation);
	}

	public static List<PlacedBlock> placedBlocks(Rotation rotation) {
		List<PlacedBlock> out = new ArrayList<>(BLOCKS.size());
		for (BlockEntry entry : BLOCKS) {
			BlockPos local = transform(entry.x, entry.y, entry.z, rotation);
			out.add(new PlacedBlock(local, rotateState(entry.state(), rotation)));
		}
		return out;
	}

	public static List<PlacedFrame> placedFrames(Rotation rotation) {
		List<PlacedFrame> out = new ArrayList<>(FRAMES.size());
		for (FrameEntry entry : FRAMES) {
			BlockPos local = transform(entry.x, entry.y, entry.z, rotation);
			out.add(new PlacedFrame(local, rotateFacing(entry.facing, rotation)));
		}
		return out;
	}

	private static BlockEntry entry(int x, int y, int z, String name) {
		return entry(x, y, z, name, null);
	}

	private static BlockEntry entry(int x, int y, int z, String name, String props) {
		Block block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(name));
		BlockState state = block.defaultBlockState();
		if (props != null && !props.isEmpty()) {
			state = applyProps(state, props);
		}
		return new BlockEntry(x, y, z, state);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static BlockState applyProps(BlockState state, String props) {
		BlockState result = state;
		for (String part : props.split(",")) {
			String[] kv = part.split("=", 2);
			if (kv.length != 2) {
				continue;
			}
			Property property = result.getBlock().getStateDefinition().getProperty(kv[0]);
			if (property == null) {
				continue;
			}
			Comparable value = (Comparable) property.getValue(kv[1]).orElse(null);
			if (value != null) {
				result = result.setValue(property, value);
			}
		}
		return result;
	}

	public record BlockEntry(int x, int y, int z, BlockState state) {
	}

	public record FrameEntry(int x, int y, int z, Direction facing) {
	}

	public record PlacedBlock(BlockPos local, BlockState state) {
	}

	public record PlacedFrame(BlockPos local, Direction facing) {
	}
}
