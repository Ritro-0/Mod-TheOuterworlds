package com.theouterworld.item;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.OpalineNickelFlailEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class OpalineNickelFlailItem extends Item {
	public static final Identifier DEFAULT_MODEL = OuterWorldMod.id("opaline_nickel_flail");
	public static final Identifier CASTED_MODEL = OuterWorldMod.id("opaline_nickel_flail_casted");
	public static final int DURABILITY = 125;
	public static final float MELEE_DAMAGE = 8.0F;
	public static final float MELEE_ATTACK_SPEED = -2.8F;

	public OpalineNickelFlailItem(ResourceKey<Item> registryKey, Item.Properties properties) {
		super(properties
			.setId(registryKey)
			.durability(DURABILITY)
			.repairable(ModItems.OPALINE_NICKEL)
			.enchantable(15)
			.attributes(createAttributes())
			.component(DataComponents.WEAPON, new Weapon(1))
		);
	}

	public static ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_ID, MELEE_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_ID, MELEE_ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
	}

	public static boolean isCast(ItemStack stack) {
		return CASTED_MODEL.equals(stack.get(DataComponents.ITEM_MODEL));
	}

	public static void setCast(ItemStack stack, boolean cast) {
		// ITEM_MODEL is a required default component — removing it makes the item invisible.
		stack.set(DataComponents.ITEM_MODEL, cast ? CASTED_MODEL : DEFAULT_MODEL);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isCast(stack)) {
			return InteractionResult.FAIL;
		}

		if (level instanceof ServerLevel serverLevel) {
			OpalineNickelFlailEntity flail = new OpalineNickelFlailEntity(serverLevel, player, stack.copy(), hand);
			flail.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, OpalineNickelFlailEntity.THROW_SPEED, 0.0F);
			serverLevel.addFreshEntity(flail);
			setCast(stack, true);
			stack.hurtAndBreak(1, player, hand);
			serverLevel.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.TRIDENT_THROW,
				SoundSource.PLAYERS,
				1.0F,
				0.9F + player.getRandom().nextFloat() * 0.2F
			);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}

	@Override
	public float getAttackDamageBonus(Entity victim, float ignoredDamage, DamageSource damageSource) {
		if (!(damageSource.getDirectEntity() instanceof LivingEntity attacker)) {
			return 0.0F;
		}
		if (attacker.fallDistance <= 1.5F || attacker.isFallFlying()) {
			return 0.0F;
		}
		double fallDistance = attacker.fallDistance;
		double damage;
		if (fallDistance <= 3.0) {
			damage = 4.0 * fallDistance;
		} else if (fallDistance <= 8.0) {
			damage = 12.0 + 2.0 * (fallDistance - 3.0);
		} else {
			damage = 22.0 + fallDistance - 8.0;
		}
		if (attacker.level() instanceof ServerLevel level) {
			return (float) (damage + EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), victim, damageSource, 0.0F) * fallDistance);
		}
		return (float) damage;
	}
}
