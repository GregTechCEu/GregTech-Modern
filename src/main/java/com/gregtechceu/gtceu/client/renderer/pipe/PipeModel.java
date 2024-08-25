package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.BlockableSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.RestrictiveSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.client.renderer.pipe.util.TextureInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class PipeModel extends AbstractPipeModel<CacheKey> {

    private final @NotNull TextureInformation inTex;
    private final @NotNull TextureInformation sideTex;
    private final @Nullable TextureInformation restrictiveTex;
    private final @NotNull TextureInformation blockedTex;

    private SpriteInformation inSprite;
    private SpriteInformation sideSprite;
    private SpriteInformation restrictiveSprite;
    private SpriteInformation blockedSprite;

    public PipeModel(@NotNull TextureInformation inTex, @NotNull TextureInformation sideTex,
                     @Nullable TextureInformation restrictiveTex,
                     @NotNull TextureInformation blockedTex) {
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.restrictiveTex = restrictiveTex;
        this.blockedTex = blockedTex;
    }

    public PipeModel(@NotNull TextureInformation inTex, @NotNull TextureInformation sideTex,
                     boolean restrictive) {
        this(inTex, sideTex, restrictive ? new TextureInformation(GTCEu.id("block/pipe/pipe_restrictive"), -1) : null,
                new TextureInformation(GTCEu.id("block/pipe/pipe_blocked"), -1));
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideSprite;
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull ModelData data) {
        return defaultKey(data);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        if (inSprite == null) {
            inSprite = new SpriteInformation(ModelFactory.getBlockSprite(inTex.texture()), inTex.colorID());
        }
        if (sideSprite == null) {
            sideSprite = new SpriteInformation(ModelFactory.getBlockSprite(sideTex.texture()), sideTex.colorID());
        }
        if (restrictiveSprite == null && restrictiveTex != null) {
            restrictiveSprite = new SpriteInformation(ModelFactory.getBlockSprite(restrictiveTex.texture()),
                    restrictiveTex.colorID());
        }
        if (blockedSprite == null) {
            blockedSprite = new SpriteInformation(ModelFactory.getBlockSprite(blockedTex.texture()),
                    blockedTex.colorID());
        }

        if (restrictiveTex != null) {
            return RestrictiveSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite, sideSprite,
                    blockedSprite, restrictiveSprite);
        } else {
            return BlockableSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite, sideSprite,
                    blockedSprite);
        }
    }

    @Override
    @Nullable
    protected PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                   ClientLevel world, LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock matBlock ? matBlock.material : null;
        return new PipeItemModel<>(redirector, this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData(mater != null ? GTUtil.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeBlockEntity.DEFAULT_COLOR));
    }
}
