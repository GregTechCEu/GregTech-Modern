package com.gregtechceu.gtceu.client.mui.schema;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.utils.BlockPosUtil;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;

public class MultiblockSchema extends MutableSchema {

    private BlockPos controllerPos = BlockPos.ZERO;

    public MultiblockSchema(Long2ReferenceMap<BlockState> schemaMap) {
        super(schemaMap);
    }

    public MultiblockSchema setBlocks(Long2ReferenceMap<BlockState> blocks) {
        this.blocks.clear();
        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        MultiblockControllerMachine controller = null;
        for (long l : blocks.keySet()) {
            if (blocks.get(l).isAir()) continue;
            BlockState block = blocks.get(l);
            BlockPos pos = BlockPos.of(l);
            this.blocks.put(l, block);
            getLevel().setBlockAndUpdate(pos, block);
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);

            if (block.getBlock() instanceof EntityBlock entityBlock) {
                BlockEntity newEntity = entityBlock.newBlockEntity(pos, block);
                if (newEntity == null) {
                    GTCEu.LOGGER.error(
                            "Could not create BlockEntity in renderer's MutableSchema for block {} at pos {}",
                            block.getBlock().getName(), pos);
                } else {
                    getLevel().setBlockEntity(newEntity);
                }
                if (newEntity instanceof MultiblockControllerMachine newController) {
                    controller = newController;
                    controllerPos = pos;
                }
            }
        }
        if (controller != null) {
            var partsList = controller.getParts();
            for (var entry : this.blocks.long2ReferenceEntrySet()) {
                BlockPos pos = BlockPos.of(entry.getLongKey());
                BlockEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof IMultiPart multiPart) {
                    partsList.add(multiPart);
                }
            }
            controller.getPatternState("main").setState(PatternState.CheckState.VALID_UNCACHED);
            controller.formStructure("main");
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterF(min, max);
        return this;
    }
}
