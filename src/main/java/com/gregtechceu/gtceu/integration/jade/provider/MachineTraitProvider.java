package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class MachineTraitProvider<T extends MachineTrait>
                                          implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Getter
    private final ResourceLocation uid;
    public final MachineTraitType<T> traitType;

    protected MachineTraitProvider(ResourceLocation uid, MachineTraitType<T> type) {
        this.uid = uid;
        this.traitType = type;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor block, IPluginConfig iPluginConfig) {
        var be = block.getBlockEntity();
        if (be == null || !block.getServerData().contains(uid.toString(), CompoundTag.TAG_COMPOUND)) return;

        var serverData = block.getServerData().getCompound(uid.toString());
        addTooltip(serverData, iTooltip, block.getPlayer(), block, be, iPluginConfig);
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        var be = blockAccessor.getBlockEntity();
        if (be instanceof MetaMachine machine) {
            T t = machine.getTraitHolder().getTrait(traitType);
            if (t != null) write(compoundTag.getCompound(uid.toString()), blockAccessor, t);
        }
    }

    protected abstract void write(CompoundTag data, BlockAccessor blockAccessor, T trait);

    protected abstract void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                                       BlockEntity blockEntity, IPluginConfig config);
}
