package com.lowdragmc.gtceu.common.block;

import com.lowdragmc.gtceu.api.block.MaterialBlock;
import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.api.tag.TagPrefix;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote CompressedBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CompressedBlock extends MaterialBlock {

    public CompressedBlock(Properties properties, Material material) {
        super(properties, TagPrefix.block, material);
    }

}
