package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Objects;

/**
 * Jade provider which provides info for a specific machine type.
 * 
 * @param <T>       Machine type
 * @param <TagType> Info data tag type
 */
public abstract class MachineInfoProvider<T extends MetaMachine, TagType extends Tag>
                                         implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Getter
    private final ResourceLocation uid;
    public final Class<T> machineType;

    public MachineInfoProvider(ResourceLocation uid, Class<T> type) {
        this.uid = uid;
        machineType = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void appendTooltip(ITooltip iTooltip, BlockAccessor block, IPluginConfig iPluginConfig) {
        var be = block.getBlockEntity();
        if (be == null || !block.getServerData().contains(uid.toString())) return;
        addTooltip((TagType) Objects.requireNonNull(block.getServerData().get(uid.toString())), iTooltip,
                block.getPlayer(), block, be, iPluginConfig);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        var be = blockAccessor.getBlockEntity();
        if (machineType.isAssignableFrom(be.getClass())) {
            compoundTag.put(uid.toString(), write((T) be));
        }
    }

    protected abstract TagType write(T machine);

    protected abstract void addTooltip(TagType data, ITooltip tooltip, Player player, BlockAccessor block,
                                       BlockEntity blockEntity, IPluginConfig config);
}
