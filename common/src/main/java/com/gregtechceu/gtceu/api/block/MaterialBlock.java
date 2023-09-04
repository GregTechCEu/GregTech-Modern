package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
        this.renderer = Platform.isClient() ? MaterialBlockRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet()) : null;
    }

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, IRenderer renderer) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        this.renderer = renderer;
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Environment(EnvType.CLIENT)
    public static BlockColor tintedColor() {
        return (state, reader, pos, tintIndex) -> {
            if (state.getBlock() instanceof MaterialBlock block) {
                return block.material.getMaterialRGB();
            }
            return -1;
        };
    }


    /** Start falling ore stuff */
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && TagPrefix.ORES.get(tagPrefix).isSand() && ConfigHolder.INSTANCE.worldgen.sandOresFall) {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && TagPrefix.ORES.get(tagPrefix).isSand() && ConfigHolder.INSTANCE.worldgen.sandOresFall) {
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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!TagPrefix.ORES.containsKey(this.tagPrefix) || (tagPrefix == TagPrefix.oreSand || tagPrefix == TagPrefix.oreRedSand) || !ConfigHolder.INSTANCE.worldgen.sandOresFall) return;
        if (random.nextInt(16) == 0 && FallingBlock.isFree(level.getBlockState(pos.below()))) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() - 0.05;
            double f = (double)pos.getZ() + random.nextDouble();
            level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
        }
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
        return tagPrefix.getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return tagPrefix.getLocalizedName(material);
    }

}
