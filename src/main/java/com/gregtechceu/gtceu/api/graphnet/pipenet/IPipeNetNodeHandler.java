package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface IPipeNetNodeHandler {

    @NotNull
    Collection<WorldPipeNetNode> getOrCreateFromNets(ServerLevel level, BlockPos pos, IPipeStructure structure);

    @NotNull
    Collection<WorldPipeNetNode> getFromNets(ServerLevel level, BlockPos pos, IPipeStructure structure);

    void removeFromNets(ServerLevel level, BlockPos pos, IPipeStructure structure);

    void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                        @NotNull TooltipFlag flagIn,
                        IPipeStructure structure);
}
