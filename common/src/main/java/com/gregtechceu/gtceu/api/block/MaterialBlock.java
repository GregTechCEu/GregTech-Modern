package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
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
public class MaterialBlock extends AppearanceBlock implements IBlockRendererProvider {

    public final TagPrefix tagPrefix;
    public final Material material;
    public final IRenderer renderer;

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        this.renderer = MaterialBlockRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet());
    }

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, IRenderer renderer) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        this.renderer = renderer;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }

    public static int tintedColor(BlockState blockState,  @Nullable BlockAndTintGetter blockAndTintGetter,  @Nullable  BlockPos blockPos, int index) {
        if (blockState.getBlock() instanceof MaterialBlock block) {
            return block.material.getMaterialRGB();
        }
        return -1;
    }

    @Override
    public String getDescriptionId() {
        return tagPrefix.getLocalNameForItem(material);
    }

}
