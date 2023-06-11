package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote MaterialPipeBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MaterialPipeBlock<PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType extends IAttachData, WorldPipeNetType extends LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends PipeBlock<PipeType, NodeDataType, WorldPipeNetType> {

    public final Material material;
    public final PipeBlockRenderer renderer;
    public final PipeModel model;

    public MaterialPipeBlock(Properties properties, PipeType pipeType, Material material) {
        super(properties, pipeType);
        this.material = material;
        this.model = createPipeModel();
        this.renderer = new PipeBlockRenderer(this.model);
    }

    public static int tintedColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        if (blockState.getBlock() instanceof MaterialPipeBlock block) {
            return block.tinted(blockState, blockAndTintGetter, blockPos, index);
        }
        return -1;
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        return material.getMaterialRGB();
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
        return pipeType.getTagPrefix().getLocalNameForItem(material);
    }
}
