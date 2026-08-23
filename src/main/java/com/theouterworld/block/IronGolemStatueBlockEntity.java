package com.theouterworld.block;

import com.theouterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for Iron Golem Statues.
 * Stores pose data (body/head rotation) and custom name.
 */
public class IronGolemStatueBlockEntity extends BlockEntity {
    
    // Pose data
    private float bodyYaw = 0.0f;
    private float headYaw = 0.0f;
    private float headPitch = 0.0f;
    
    // Custom name (if the golem was named)
    @Nullable
    private String customNameJson = null;

    public IronGolemStatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_GOLEM_STATUE, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide() && getBlockState().hasProperty(AbstractIronGolemStatueBlock.HALF)
            && getBlockState().getValue(AbstractIronGolemStatueBlock.HALF) == DoubleBlockHalf.LOWER) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    // Getters and setters
    public float getBodyYaw() {
        return bodyYaw;
    }

    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    @Nullable
    public Component getCustomName() {
        if (customNameJson == null) return null;
        try {
            return Component.literal(customNameJson);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCustomName(@Nullable Component customName) {
        if (customName != null) {
            this.customNameJson = customName.getString();
        } else {
            this.customNameJson = null;
        }
    }

    public boolean hasCustomName() {
        return customNameJson != null;
    }

    public void readNbt(CompoundTag nbt) {
        nbt.getFloat("BodyYaw").ifPresent(val -> this.bodyYaw = val);
        nbt.getFloat("HeadYaw").ifPresent(val -> this.headYaw = val);
        nbt.getFloat("HeadPitch").ifPresent(val -> this.headPitch = val);
        
        if (nbt.contains("CustomName")) {
            nbt.getString("CustomName").ifPresent(name -> this.customNameJson = name);
        }
    }

    public void writeNbt(CompoundTag nbt) {
        nbt.putFloat("BodyYaw", bodyYaw);
        nbt.putFloat("HeadYaw", headYaw);
        nbt.putFloat("HeadPitch", headPitch);
        
        if (customNameJson != null) {
            nbt.putString("CustomName", customNameJson);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        writeNbt(nbt);
        return nbt;
    }
}

