package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface IPipeNetNodeHandler {

    @NotNull
    Collection<WorldPipeNetNode> getOrCreateFromNets(World world, BlockPos pos, IPipeStructure structure);

    @NotNull
    Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure);

    void removeFromNets(World world, BlockPos pos, IPipeStructure structure);

    void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                        @NotNull ITooltipFlag flagIn,
                        IPipeStructure structure);
}
