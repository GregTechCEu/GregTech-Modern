package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MaterialPipeBlock<NodeDataType extends PipeSegmentProperties> extends PipeBlock<NodeDataType> {

    public final Material material;
    public final IMaterialPipeVariant<NodeDataType> materialPipeType;

    public MaterialPipeBlock(Properties properties, IMaterialPipeVariant<NodeDataType> pipeType, PipeNetworkType networkType, Material material) {
        super(properties, pipeType, networkType);
        this.material = material;
        this.materialPipeType = pipeType;
        if (material.isNull()) throw new IllegalStateException("Cannot create material pipe block with null material");
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, level, pos, index) -> {
            if (level != null && pos != null && (index == 0 || index == 1)) {
                if (level.getBlockEntity(pos) instanceof IPaintable paintable && paintable.isPainted()) {
                    return paintable.getPaintingColor();
                }
            }
            if (state.getBlock() instanceof MaterialPipeBlock<?> block) {
                return block.tinted(state, level, pos, index);
            }
            return -1;
        };
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        return index == 0 || index == 1 ? material.getMaterialRGB() : -1;
    }

    @Override
    public String getDescriptionId() {
        return materialPipeType.getTagPrefix().getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return materialPipeType.getTagPrefix().getLocalizedName(material);
    }
}
