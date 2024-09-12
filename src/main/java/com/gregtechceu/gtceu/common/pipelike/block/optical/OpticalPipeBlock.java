package com.gregtechceu.gtceu.common.pipelike.block.optical;

import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.ActivablePipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.pipelike.handlers.DuctNetHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;

public class OpticalPipeBlock extends ActivablePipeBlock {

    public OpticalPipeBlock(BlockBehaviour.Properties properties, OpticalStructure structure) {
        super(properties, structure);
    }

    @Override
    public GTToolType getToolClass() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    public boolean allowsBlocking() {
        return false;
    }

    @Override
    public IPipeNetNodeHandler getHandler(PipeBlockEntity tileContext) {
        return DuctNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return DuctNetHandler.INSTANCE;
    }
}
