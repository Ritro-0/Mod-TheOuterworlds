package com.theouterworld.block;

import com.theouterworld.OuterWorldMod;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

/**
 * Glass, tinted glass, and stained glass forged with a trim template and a trim
 * material. Hidden from the creative menu. Looks like the source glass, plus the
 * glass trim texture colored by that material's palette. Tinted still blocks light.
 */
public class TrimmedGlassBlock extends TransparentBlock implements EntityBlock {
	public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
	public static final EnumProperty<Material> MATERIAL = EnumProperty.create("material", Material.class);

	public static final TrimmedGlassBlock BLOCK;
	public static final Item ITEM;

	static {
		Identifier id = OuterWorldMod.id("trimmed_glass");
		ResourceKey<net.minecraft.world.level.block.Block> blockKey = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
		BLOCK = new TrimmedGlassBlock(
			Properties.ofFullCopy(Blocks.GLASS).setId(blockKey).noOcclusion()
		);
		Registry.register(BuiltInRegistries.BLOCK, id, BLOCK);
		ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
		TrimmedGlassBlockItem item = new TrimmedGlassBlockItem(BLOCK, new Item.Properties().setId(itemKey));
		ITEM = Registry.register(BuiltInRegistries.ITEM, id, item);
		item.registerBlocks(Item.BY_BLOCK, item);
	}

	public static void register() {
		// Touch the class so the block is registered during mod init.
		if (BLOCK == null) {
			throw new IllegalStateException("trimmed glass failed to register");
		}
	}

	public TrimmedGlassBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(KIND, Kind.GLASS)
			.setValue(MATERIAL, Material.QUARTZ));
	}

	/**
	 * Smithing result for wearable glass, or {@code null} when the base is not glass.
	 * {@link ItemStack#EMPTY} means this exact trim is already applied.
	 */
	@Nullable
	public static ItemStack smith(ItemStack base, ItemStack addition, Holder<TrimPattern> pattern) {
		Holder<TrimMaterial> provided = addition.get(DataComponents.PROVIDES_TRIM_MATERIAL);
		if (provided == null || pattern == null) {
			return null;
		}
		Material material = Material.byHolder(provided);
		if (material == null) {
			return null;
		}

		Kind kind;
		if (base.is(ITEM)) {
			kind = kindOf(base);
			if (kind == null) {
				kind = Kind.GLASS;
			}
			ArmorTrim existing = base.get(DataComponents.TRIM);
			BlockItemStateProperties props = base.get(DataComponents.BLOCK_STATE);
			Material current = props == null ? null : props.get(MATERIAL);
			if (existing != null && existing.pattern().equals(pattern) && existing.material().equals(provided) && current == material) {
				return ItemStack.EMPTY;
			}
		} else {
			kind = Kind.fromItem(base.getItem());
			if (kind == null) {
				return null;
			}
		}
		return createStack(kind, material, pattern, provided);
	}

	public static ItemStack createStack(Kind kind, Material material, @Nullable Holder<TrimPattern> pattern, @Nullable Holder<TrimMaterial> trimMaterial) {
		ItemStack stack = new ItemStack(ITEM);
		stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(KIND, kind).with(MATERIAL, material));
		if (pattern != null && trimMaterial != null) {
			stack.set(DataComponents.TRIM, new ArmorTrim(trimMaterial, pattern));
		}
		return stack;
	}

	public static ItemStack stackFrom(BlockState state, @Nullable TrimmedGlassBlockEntity entity, @Nullable net.minecraft.core.RegistryAccess access) {
		Kind kind = state.getValue(KIND);
		Material material = state.getValue(MATERIAL);
		Holder<TrimPattern> pattern = null;
		Holder<TrimMaterial> trimMaterial = null;
		if (entity != null && access != null && entity.patternId() != null) {
			var patterns = access.lookupOrThrow(Registries.TRIM_PATTERN);
			var materials = access.lookupOrThrow(Registries.TRIM_MATERIAL);
			pattern = patterns.get(ResourceKey.create(Registries.TRIM_PATTERN, entity.patternId())).orElse(null);
			trimMaterial = materials.get(material.key()).orElse(null);
		}
		return createStack(kind, material, pattern, trimMaterial);
	}

	@Nullable
	public static Kind kindOf(ItemStack stack) {
		if (stack.is(ITEM)) {
			BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
			if (props != null) {
				Kind kind = props.get(KIND);
				if (kind != null) {
					return kind;
				}
			}
			return Kind.GLASS;
		}
		return Kind.fromItem(stack.getItem());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		builder.add(KIND, MATERIAL);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TrimmedGlassBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (level.getBlockEntity(pos) instanceof TrimmedGlassBlockEntity entity) {
			ArmorTrim trim = stack.get(DataComponents.TRIM);
			if (trim != null) {
				trim.pattern().unwrapKey().ifPresent(key -> entity.setPatternId(key.identifier()));
			}
		}
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockEntity raw = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		TrimmedGlassBlockEntity entity = raw instanceof TrimmedGlassBlockEntity trimmed ? trimmed : null;
		return List.of(stackFrom(state, entity, params.getLevel().registryAccess()));
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		TrimmedGlassBlockEntity entity = null;
		if (includeData && level.getBlockEntity(pos) instanceof TrimmedGlassBlockEntity trimmed) {
			entity = trimmed;
		}
		net.minecraft.core.RegistryAccess access = level.registryAccess();
		return stackFrom(state, entity, access);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
		return adjacent.is(this)
			&& adjacent.getValue(KIND) == state.getValue(KIND)
			&& adjacent.getValue(MATERIAL) == state.getValue(MATERIAL);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return state.getValue(KIND) != Kind.TINTED;
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return state.getValue(KIND) == Kind.TINTED ? 15 : 0;
	}

	public enum Kind implements StringRepresentable {
		GLASS("glass"),
		TINTED("tinted"),
		WHITE("white"),
		ORANGE("orange"),
		MAGENTA("magenta"),
		LIGHT_BLUE("light_blue"),
		YELLOW("yellow"),
		LIME("lime"),
		PINK("pink"),
		GRAY("gray"),
		LIGHT_GRAY("light_gray"),
		CYAN("cyan"),
		PURPLE("purple"),
		BLUE("blue"),
		BROWN("brown"),
		GREEN("green"),
		RED("red"),
		BLACK("black");

		private final String name;

		Kind(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public String blockTexture() {
			if (this == GLASS) {
				return "minecraft:block/glass";
			}
			if (this == TINTED) {
				return "minecraft:block/tinted_glass";
			}
			return "minecraft:block/" + this.name + "_stained_glass";
		}

		public Identifier overlayTexture() {
			if (this == GLASS) {
				return Identifier.fromNamespaceAndPath("minecraft", "textures/block/glass.png");
			}
			if (this == TINTED) {
				return Identifier.fromNamespaceAndPath("minecraft", "textures/block/tinted_glass.png");
			}
			return Identifier.fromNamespaceAndPath("minecraft", "textures/block/" + this.name + "_stained_glass.png");
		}

		public String descriptionId() {
			if (this == GLASS) {
				return "block.minecraft.glass";
			}
			if (this == TINTED) {
				return "block.minecraft.tinted_glass";
			}
			return "block.minecraft." + this.name + "_stained_glass";
		}

		@Nullable
		public static Kind fromItem(Item item) {
			if (item == Items.GLASS) {
				return GLASS;
			}
			if (item == Items.TINTED_GLASS) {
				return TINTED;
			}
			for (DyeColor color : DyeColor.values()) {
				if (item == Items.STAINED_GLASS.pick(color)) {
					return byName(color.getSerializedName());
				}
			}
			return null;
		}

		@Nullable
		public static Kind byName(String name) {
			for (Kind kind : values()) {
				if (kind.name.equals(name)) {
					return kind;
				}
			}
			return null;
		}
	}

	public enum Material implements StringRepresentable {
		QUARTZ("quartz", "minecraft"),
		IRON("iron", "minecraft"),
		NETHERITE("netherite", "minecraft"),
		REDSTONE("redstone", "minecraft"),
		COPPER("copper", "minecraft"),
		GOLD("gold", "minecraft"),
		EMERALD("emerald", "minecraft"),
		DIAMOND("diamond", "minecraft"),
		LAPIS("lapis", "minecraft"),
		AMETHYST("amethyst", "minecraft"),
		RESIN("resin", "minecraft"),
		OLIVINE("olivine", "theouterworlds"),
		OPAL("opal", "theouterworlds"),
		JAROSITE("jarosite", "theouterworlds"),
		GRAPHITE("graphite", "theouterworlds"),
		NICKEL("nickel", "theouterworlds"),
		OSMIUM("osmium", "theouterworlds"),
		IRIDIUM("iridium", "theouterworlds"),
		ACID("acid", "theouterworlds");

		private final String name;
		private final String namespace;

		Material(String name, String namespace) {
			this.name = name;
			this.namespace = namespace;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public ResourceKey<TrimMaterial> key() {
			return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.fromNamespaceAndPath(this.namespace, this.name));
		}

		public String trimTexture() {
			return "theouterworlds:trims/entity/humanoid/glass/glass_grayscale_" + this.name;
		}

		@Nullable
		public static Material byHolder(Holder<TrimMaterial> holder) {
			for (Material material : values()) {
				if (holder.is(material.key())) {
					return material;
				}
			}
			Identifier id = holder.unwrapKey().map(ResourceKey::identifier).orElse(null);
			if (id == null) {
				return null;
			}
			return byName(id.getPath());
		}

		@Nullable
		public static Material byName(String name) {
			String normalized = name.toLowerCase(Locale.ROOT);
			for (Material material : values()) {
				if (material.name.equals(normalized)) {
					return material;
				}
			}
			return null;
		}
	}
}
