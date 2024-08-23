package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockEntity extends PipeBlockEntity {

    @Persisted
    @DescSynced
    private Material material;

    public MaterialPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void initialize() {
        // prevent initialization when we don't know our material;
        // this specifically happens right after we have been
        // placed and placedBy() has yet to be called.
        if (material != null) super.initialize();
    }

    @Override
    public void placedBy(ItemStack stack, Player player) {
        super.placedBy(stack, player);
        setMaterial(getBlockType().material);
        initialize();
    }

    @Override
    public @NotNull MaterialPipeBlock getBlockType() {
        return (MaterialPipeBlock) super.getBlockType();
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        if (material == null) return GTMaterials.Aluminium;
        return material;
    }

    @Override
    public ItemStack getMainDrop(@NotNull BlockState state) {
        return new ItemStack(getBlockType().asItem(), 1);
    }

    @Override
    public int getDefaultPaintingColor() {
        return GTUtil.convertRGBtoARGB(getMaterial().getMaterialRGB());
    }

    @Override
    public @NotNull ModelData getModelData() {
        return super.getModelData().derive().with(AbstractPipeModel.MATERIAL_PROPERTY, getMaterial()).build();
    }
}
