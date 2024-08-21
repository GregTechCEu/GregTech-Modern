package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface IPipeNetNodeHandler {

    @NotNull
    Collection<WorldPipeNetNode> getOrCreateFromNets(LevelAccessor level, BlockPos pos, IPipeStructure structure);

    @NotNull
    Collection<WorldPipeNetNode> getFromNets(LevelAccessor level, BlockPos pos, IPipeStructure structure);

    void removeFromNets(LevelAccessor level, BlockPos pos, IPipeStructure structure);

    void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                        @NotNull TooltipFlag flagIn,
                        IPipeStructure structure);
}
