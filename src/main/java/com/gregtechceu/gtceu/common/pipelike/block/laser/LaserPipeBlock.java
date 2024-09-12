package com.gregtechceu.gtceu.common.pipelike.block.laser;

import com.gregtechceu.gtceu.api.graphnet.pipenet.IPipeNetNodeHandler;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.ActivablePipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.pipelike.handlers.LaserNetHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;

public class LaserPipeBlock extends ActivablePipeBlock {

    public LaserPipeBlock(BlockBehaviour.Properties properties, LaserStructure structure) {
        super(properties, structure);
    }

    @Override
    public GTToolType getToolClass() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gtceu.tool_action.wire_cutter.connect";
    }

    @Override
    public boolean allowsBlocking() {
        return false;
    }

    @Override
    @NotNull
    public IPipeNetNodeHandler getHandler(PipeBlockEntity tileContext) {
        return LaserNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return LaserNetHandler.INSTANCE;
    }
}
