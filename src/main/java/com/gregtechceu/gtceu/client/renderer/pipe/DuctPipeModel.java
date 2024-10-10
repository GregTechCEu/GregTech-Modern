package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuctPipeModel extends AbstractPipeModel<CacheKey> {

    private static final ResourceLocation loc = GTCEu.id("pipe_duct");

    public static final DuctPipeModel INSTANCE = new DuctPipeModel();

    private static final ResourceLocation SIDE_TEXTURE = GTCEu.id("block/pipe/pipe_duct_side");
    private static final ResourceLocation END_TEXTURE = GTCEu.id("block/pipe/pipe_duct_in");

    private SpriteInformation sideSprite;
    private SpriteInformation endSprite;

    @Override
    protected @NotNull CacheKey toKey(@NotNull ModelData state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        if (sideSprite == null) {
            sideSprite = new SpriteInformation(ModelFactory.getBlockSprite(SIDE_TEXTURE), -1);
        }
        if (endSprite == null) {
            endSprite = new SpriteInformation(ModelFactory.getBlockSprite(END_TEXTURE), -1);
        }

        return StructureQuadCache.create(PipeQuadHelper.create(key.getThickness()), endSprite, sideSprite);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideSprite;
    }

    @Override
    protected @Nullable PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                             ClientLevel world, LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(redirector, this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData());
    }
}
