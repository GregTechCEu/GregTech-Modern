package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MaterialBlock extends Block implements IBlockRendererProvider {

    public final TagPrefix tagPrefix;
    public final Material material;

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        MaterialBlockRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet());
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return MaterialBlockRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet());
    }

    public static int tintedColor(BlockState blockState,  @Nullable BlockAndTintGetter blockAndTintGetter,  @Nullable  BlockPos blockPos, int index) {
        if (blockState.getBlock() instanceof MaterialBlock block) {
            return block.material.getMaterialRGB();
        }
        return -1;
    }
}
