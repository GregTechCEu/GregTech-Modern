package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FallingBlock;
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

    public static int tintedColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        if (blockState.getBlock() instanceof MaterialBlock block) {
            return block.material.getMaterialRGB();
        }
        return -1;
    }


    /** Start falling ore stuff */
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && super.material == net.minecraft.world.level.material.Material.SAND && ConfigHolder.INSTANCE.worldgen.sandOresFall) {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && super.material == net.minecraft.world.level.material.Material.SAND && ConfigHolder.INSTANCE.worldgen.sandOresFall) {
            level.scheduleTick(currentPos, this, this.getDelayAfterPlace());
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!FallingBlock.isFree(level.getBlockState(pos.below())) || pos.getY() < level.getMinBuildHeight()) {
            return;
        }
        FallingBlockEntity.fall(level, pos, state);
    }

    /**
     * Gets the amount of time in ticks this block will wait before attempting to start falling.
     */
    protected int getDelayAfterPlace() {
        return 2;
    }
    /** End falling ore stuff */


    @Override
    public String getDescriptionId() {
        return tagPrefix.getLocalNameForItem(material);
    }

}
