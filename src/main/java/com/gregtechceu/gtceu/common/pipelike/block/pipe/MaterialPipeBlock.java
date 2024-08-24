package com.gregtechceu.gtceu.common.pipelike.block.pipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IBurnable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IFreezable;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MaterialPipeBlock extends PipeMaterialBlock implements IBurnable, IFreezable {

    public MaterialPipeBlock(BlockBehaviour.Properties properties, MaterialPipeStructure structure, Material material) {
        super(properties, structure, material);
    }
}
