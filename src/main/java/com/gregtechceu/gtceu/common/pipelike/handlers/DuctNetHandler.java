package com.gregtechceu.gtceu.common.pipelike.handlers;

import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.common.pipelike.block.duct.DuctStructure;
import com.gregtechceu.gtceu.common.pipelike.block.optical.OpticalStructure;
import com.gregtechceu.gtceu.common.pipelike.net.optical.WorldOpticalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class DuctNetHandler implements IPipeNetNodeHandler {

    public static final DuctNetHandler INSTANCE = new DuctNetHandler();

    @Override
    public @NotNull Collection<WorldPipeNetNode> getOrCreateFromNets(ServerLevel world, BlockPos pos,
                                                                     IPipeStructure structure) {
        if (structure instanceof DuctStructure) {
            return Collections.singletonList(WorldOpticalNet.getWorldNet(world).getOrCreateNode(pos));
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<WorldPipeNetNode> getFromNets(ServerLevel world, BlockPos pos,
                                                             IPipeStructure structure) {
        if (structure instanceof DuctStructure) {
            WorldPipeNetNode node = WorldOpticalNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof DuctStructure) {
            WorldOpticalNet net = WorldOpticalNet.getWorldNet(world);
            WorldPipeNetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flagIn, IPipeStructure structure) {
        tooltip.add(Component.translatable("block.gtceu.normal_optical_pipe.tooltip"));
    }
}

