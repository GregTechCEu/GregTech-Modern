package com.gregtechceu.gtceu.common.pipelike.block.duct;

import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.common.pipelike.handlers.DuctNetHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.NotNull;

public class DuctPipeBlock extends PipeBlock {

    public DuctPipeBlock(Properties properties, DuctStructure structure) {
        super(properties, structure);
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wrench.connect";
    }

    @Override
    public boolean allowsBlocking() {
        return false;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(BlockGetter world, BlockPos pos) {
        return DuctNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return DuctNetHandler.INSTANCE;
    }
}
