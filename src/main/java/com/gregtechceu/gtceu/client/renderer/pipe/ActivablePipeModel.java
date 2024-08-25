package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ActivableSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ActivableCacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.client.renderer.pipe.util.TextureInformation;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ActivablePipeModel extends AbstractPipeModel<ActivableCacheKey> {

    public static final ModelProperty<Boolean> ACTIVE_PROPERTY = new ModelProperty<>();

    private final TextureInformation inTex;
    private final TextureInformation sideTex;
    private final TextureInformation overlayTex;
    private final TextureInformation overlayActiveTex;

    private SpriteInformation inSprite;
    private SpriteInformation sideSprite;
    private SpriteInformation overlaySprite;
    private SpriteInformation overlayActiveSprite;

    private final boolean emissiveActive;

    public ActivablePipeModel(@NotNull TextureInformation inTex,
                              @NotNull TextureInformation sideTex,
                              @NotNull TextureInformation overlayTex,
                              @NotNull TextureInformation overlayActiveTex, boolean emissiveActive) {
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
        this.emissiveActive = emissiveActive;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(ActivableCacheKey key, byte connectionMask, byte closedMask,
                                             byte blockedMask, ColorData data, @Nullable Material frameMaterial,
                                             byte frameMask, byte coverMask) {
        List<BakedQuad> quads = super.getQuads(key, connectionMask, closedMask, blockedMask, data, frameMaterial,
                frameMask, coverMask);

        if (key.isActive() && allowActive()) {
            if (emissiveActive) {
                ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, true);
                // TODO bake this into the original quads
                quads = quads.stream()
                        .map(quad -> Quad.from(quad).setLight(15, 15).rebake())
                        .collect(Collectors.toList());
            }
            ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, true);
        } else {
            ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, false);
        }
        return quads;
    }

    @Override
    protected @NotNull ActivableCacheKey toKey(@NotNull ModelData state) {
        return ActivableCacheKey.of(state.get(THICKNESS_PROPERTY), state.get(ACTIVE_PROPERTY));
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideSprite;
    }

    @Override
    protected StructureQuadCache constructForKey(ActivableCacheKey key) {
        if (inSprite == null) {
            inSprite = new SpriteInformation(ModelFactory.getBlockSprite(inTex.texture()), inTex.colorID());
        }
        if (sideSprite == null) {
            sideSprite = new SpriteInformation(ModelFactory.getBlockSprite(sideTex.texture()), sideTex.colorID());
        }
        if (overlaySprite == null) {
            overlaySprite = new SpriteInformation(ModelFactory.getBlockSprite(overlayTex.texture()),
                    overlayTex.colorID());
        }
        if (overlayActiveSprite == null) {
            overlayActiveSprite = new SpriteInformation(ModelFactory.getBlockSprite(overlayActiveTex.texture()),
                    overlayActiveTex.colorID());
        }

        return ActivableSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite, sideSprite,
                overlaySprite, overlayActiveSprite);
    }

    public boolean allowActive() {
        return !ConfigHolder.INSTANCE.client.preventAnimatedCables;
    }

    @Override
    protected @Nullable PipeItemModel<ActivableCacheKey> getItemModel(PipeModelRedirector redirector,
                                                                      @NotNull ItemStack stack, ClientLevel world,
                                                                      LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(redirector, this,
                new ActivableCacheKey(block.getStructure().getRenderThickness(), false),
                new ColorData(PipeBlockEntity.DEFAULT_COLOR));
    }
}
