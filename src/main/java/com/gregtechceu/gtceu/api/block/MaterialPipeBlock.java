package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.*;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MaterialPipeBlock<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
                                       extends PipeBlock<PipeType, NodeDataType, WorldPipeNetType> {

    public final Material material;
    public final PipeBlockRenderer renderer;
    public final PipeModel model;

    public MaterialPipeBlock(Properties properties, PipeType pipeType, Material material) {
        super(properties, pipeType);
        this.material = material;
        this.model = createPipeModel();
        this.renderer = new PipeBlockRenderer(this.model);
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, level, pos, index) -> {
            if (level != null && pos != null && (index == 0 || index == 1)) {
                if (level.getBlockEntity(pos) instanceof IPaintable paintable && paintable.isPainted()) {
                    return paintable.getPaintingColor();
                }
            }
            if (state.getBlock() instanceof MaterialPipeBlock<?, ?, ?> block) {
                return block.tinted(state, level, pos, index);
            }
            return -1;
        };
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        return index == 0 || index == 1 ? material.getMaterialRGB() : -1;
    }

    @Override
    protected PipeModel getPipeModel() {
        return model;
    }

    @Override
    public final NodeDataType createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return createMaterialData();
    }

    @Override
    public NodeDataType createProperties(IPipeNode<PipeType, NodeDataType> pipeTile) {
        PipeType pipeType = pipeTile.getPipeType();
        Material material = ((MaterialPipeBlock<PipeType, NodeDataType, WorldPipeNetType>) pipeTile
                .getPipeBlock()).material;
        if (pipeType == null || material.isNull()) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    protected abstract NodeDataType createProperties(PipeType pipeType, Material material);

    @Override
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    public final NodeDataType getFallbackType() {
        return createMaterialData();
    }

    protected abstract NodeDataType createMaterialData();

    protected abstract PipeModel createPipeModel();

    @Override
    public String getDescriptionId() {
        return pipeType.getTagPrefix().getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return pipeType.getTagPrefix().getLocalizedName(material);
    }
}
