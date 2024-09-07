package com.gregtechceu.gtceu.common.pipelike.block.pipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IBurnable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IFreezable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MaterialPipeBlock extends PipeMaterialBlock implements IBurnable, IFreezable {

    public MaterialPipeBlock(BlockBehaviour.Properties properties, MaterialPipeStructure structure, Material material) {
        super(properties, structure, material);
    }

    @Override
    public MaterialPipeStructure getStructure() {
        return (MaterialPipeStructure) super.getStructure();
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockState.getBlock() instanceof MaterialPipeBlock block) {
                if (blockPos != null && level != null &&
                        level.getBlockEntity(blockPos) instanceof PipeBlockEntity pipe) {
                    if (pipe.getFrameMaterial() != null) {
                        if (index == 3) {
                            return pipe.getFrameMaterial().getMaterialRGB();
                        } else if (index == 4) {
                            return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                        }
                    }
                    if (index == 0 && pipe.isPainted()) {
                        return pipe.getPaintingColor();
                    }
                }
                return block.tinted(blockState, level, blockPos, index);
            }
            return -1;
        };
    }
}
