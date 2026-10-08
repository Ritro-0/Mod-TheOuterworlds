package com.theouterworld.item;

import com.mojang.serialization.Codec;
import com.theouterworld.OuterWorldMod;
import com.theouterworld.block.TrimmedGlassBlock;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Glass cut into a helmet for a tamed wolf. It keeps the source glass color,
 * cannot be placed, and occupies the head slot so wolf armor can stay on the body.
 */
public class DoggyGlassItem extends Item {
	public static final DataComponentType<String> GLASS_KIND = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		OuterWorldMod.id("glass_kind"),
		DataComponentType.<String>builder()
			.persistent(Codec.STRING)
			.networkSynchronized(ByteBufCodecs.STRING_UTF8)
			.build()
	);

	public static final ResourceKey<EquipmentAsset> EQUIPMENT_ASSET = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		OuterWorldMod.id("doggy_helmet")
	);

	public static final Item ITEM;

	static {
		Identifier id = OuterWorldMod.id("doggy_glass");
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		ITEM = Registry.register(BuiltInRegistries.ITEM, id, new DoggyGlassItem(key));
	}

	public static void register() {
		if (ITEM == null) {
			throw new IllegalStateException("doggy glass failed to register");
		}
	}

	public DoggyGlassItem(ResourceKey<Item> key) {
		super(new Item.Properties()
			.setId(key)
			.component(GLASS_KIND, TrimmedGlassBlock.Kind.GLASS.getSerializedName())
			.component(DataComponents.EQUIPPABLE, equippable()));
	}

	private static Equippable equippable() {
		return Equippable.builder(EquipmentSlot.HEAD)
			.setAsset(EQUIPMENT_ASSET)
			.setAllowedEntities(EntityTypes.WOLF)
			.setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
			.setDispensable(false)
			.setSwappable(false)
			.setDamageOnHurt(false)
			.setEquipOnInteract(false)
			.setCanBeSheared(true)
			.setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP))
			.build();
	}

	public static ItemStack create(TrimmedGlassBlock.Kind kind) {
		ItemStack stack = new ItemStack(ITEM);
		stack.set(GLASS_KIND, kind.getSerializedName());
		return stack;
	}

	public static boolean isDoggyGlass(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.is(ITEM);
	}

	public static boolean isWearing(LivingEntity entity) {
		return entity != null && isDoggyGlass(entity.getItemBySlot(EquipmentSlot.HEAD));
	}

	public static TrimmedGlassBlock.Kind kindOf(ItemStack stack) {
		String name = stack.get(GLASS_KIND);
		TrimmedGlassBlock.Kind kind = name == null ? null : TrimmedGlassBlock.Kind.byName(name);
		return kind == null ? TrimmedGlassBlock.Kind.GLASS : kind;
	}

	/** Packed RGB used to tint the wolf helmet toward the source glass. */
	public static int tintRgb(TrimmedGlassBlock.Kind kind) {
		return switch (kind) {
			case GLASS -> 0xFFFFFF;
			case TINTED -> 0x2B2B2B;
			case WHITE -> 0xF9FFFE;
			case ORANGE -> 0xF9801D;
			case MAGENTA -> 0xC74EBD;
			case LIGHT_BLUE -> 0x3AB3DA;
			case YELLOW -> 0xFED83D;
			case LIME -> 0x80C71F;
			case PINK -> 0xF38BAA;
			case GRAY -> 0x474F52;
			case LIGHT_GRAY -> 0x9D9D97;
			case CYAN -> 0x169C9C;
			case PURPLE -> 0x8932B8;
			case BLUE -> 0x3C44AA;
			case BROWN -> 0x835432;
			case GREEN -> 0x5E7C16;
			case RED -> 0xB02E26;
			case BLACK -> 0x1D1D21;
		};
	}

	public static void fillCreative(Consumer<ItemStack> output) {
		for (TrimmedGlassBlock.Kind kind : TrimmedGlassBlock.Kind.values()) {
			output.accept(create(kind));
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("item.theouterworlds.doggy_glass." + kindOf(stack).getSerializedName());
	}
}
