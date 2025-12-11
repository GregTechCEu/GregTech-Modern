package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

public interface IMachineBlockEntity extends IToolGridHighlight, IPaintable, IForgeBlockEntity {

    ModelProperty<BlockAndTintGetter> MODEL_DATA_LEVEL = new ModelProperty<>();
    ModelProperty<BlockPos> MODEL_DATA_POS = new ModelProperty<>();

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Level level() {
        return self().getLevel();
    }

    default BlockPos pos() {
        return self().getBlockPos();
    }

    default void scheduleRenderUpdate() {
        var pos = pos();
        if (level() != null) {
            var state = level().getBlockState(pos);
            if (level().isClientSide) {
                level().sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                self().requestModelDataUpdate();
            } else {
                level().blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    MachineRenderState getRenderState();

    void setRenderState(MachineRenderState state);

    long getOffset();

    int getPaintingColor();

    void setPaintingColor(int color);

    int getDefaultPaintingColor();
}
