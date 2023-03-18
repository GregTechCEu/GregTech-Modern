package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote FrameBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FrameBlock extends MaterialBlock {

    public FrameBlock(Properties properties, Material material) {
        super(properties, TagPrefix.frameGt, material);
    }

}
