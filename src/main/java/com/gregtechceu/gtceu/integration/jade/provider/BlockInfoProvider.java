package com.gregtechceu.gtceu.integration.jade.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.Nullable;

public abstract class BlockInfoProvider<C> implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private final ResourceLocation uid;

    protected BlockInfoProvider(Identifier uid) {
        this.uid = GTJadeIds.from(uid);
    }

    @Override
    public ResourceLocation getUid() {
        return GTJadeIds.toResourceLocation(uid);
    }

    @Nullable
    protected abstract C getCapability(Level level, BlockPos pos);

    protected abstract void write(CompoundTag data, C capability, BlockAccessor block);

    protected abstract void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                                       BlockEntity blockEntity, IPluginConfig config);

    protected boolean allowDisplaying(C capability) {
        return true;
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor block, IPluginConfig config) {
        BlockEntity be = block.getBlockEntity();
        if (be != null) {
            CompoundTag capData = block.getServerData().getCompoundOrEmpty(this.uid.toString());
            this.addTooltip(capData, tooltip, block.getPlayer(), block, be, config);
        }
    }

    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        CompoundTag capData = data.getCompoundOrEmpty(this.uid.toString());
        C capability = this.getCapability(blockAccessor.getLevel(), blockAccessor.getPosition());
        if (capability != null && this.allowDisplaying(capability)) {
            this.write(capData, capability, blockAccessor);
        }
        data.put(this.uid.toString(), capData);
    }
}
