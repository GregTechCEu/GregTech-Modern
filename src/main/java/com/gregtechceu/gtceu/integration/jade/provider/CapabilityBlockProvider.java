package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public abstract class CapabilityBlockProvider<C>
                                             implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Getter
    public final ResourceLocation uid;

    protected CapabilityBlockProvider(ResourceLocation uid) {
        this.uid = uid;
    }

    @Nullable
    protected abstract C getCapability(Level level, BlockPos pos, @Nullable Direction side);

    protected abstract void write(CompoundTag data, C capability);

    protected abstract void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                                       BlockEntity blockEntity, IPluginConfig config);

    protected boolean allowDisplaying(C capability) {
        return true;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor block, IPluginConfig config) {
        var be = block.getBlockEntity();
        if (be == null) return;

        var side = block.getSide();
        CompoundTag capData = null;
        if (side != null) {
            capData = block.getServerData().getCompound(uid.toString()).getCompound(side.getName());
        }
        if (side == null || capData.isEmpty()) {
            capData = block.getServerData().getCompound(uid.toString()).getCompound("null");
        }

        addTooltip(capData, tooltip, block.getPlayer(), block, be, config);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        // use uid as key for capability data
        var capData = data.getCompound(uid.toString());

        var capability = getCapability(blockAccessor.getLevel(), blockAccessor.getPosition(), null);
        if (capability != null && allowDisplaying(capability)) {
            var tag = new CompoundTag();
            write(tag, capability);
            capData.put("null", tag);
        }

        for (Direction value : GTUtil.DIRECTIONS) {
            capability = getCapability(blockAccessor.getLevel(), blockAccessor.getPosition(), value);
            if (capability != null && allowDisplaying(capability)) {
                var tag = new CompoundTag();
                write(tag, capability);
                capData.put(value.getName(), tag);
            }
        }

        data.put(uid.toString(), capData);
    }

    protected float getProgress(long progress, long maxProgress) {
        return maxProgress == 0 ? 0 : (float) ((double) progress / maxProgress);
    }
}
