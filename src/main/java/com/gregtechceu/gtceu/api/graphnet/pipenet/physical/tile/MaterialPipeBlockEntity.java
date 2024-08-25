package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockEntity extends PipeBlockEntity {

    public MaterialPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @NotNull PipeMaterialBlock getBlockType() {
        return (PipeMaterialBlock) super.getBlockType();
    }

    @Override
    public int getDefaultPaintingColor() {
        return GTUtil.convertRGBtoARGB(getBlockType().material.getMaterialRGB());
    }

    @Override
    public @NotNull ModelData getModelData() {
        return super.getModelData().derive().with(AbstractPipeModel.MATERIAL_PROPERTY, getBlockType().material).build();
    }
}
