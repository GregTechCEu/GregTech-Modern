package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ActivableSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ActivableCacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ActivablePipeModel extends AbstractPipeModel<ActivableCacheKey> {

    private static final ResourceLocation loc = GTCEu.id("block/pipe_activable");

    public static final ModelProperty<Boolean> ACTIVE_PROPERTY = new ModelProperty<>();

    public static final ActivablePipeModel OPTICAL = new ActivablePipeModel(Textures.OPTICAL_PIPE_IN,
            Textures.OPTICAL_PIPE_SIDE, Textures.OPTICAL_PIPE_SIDE_OVERLAY, Textures.OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE,
            false, "optical");
    public static final ActivablePipeModel LASER = new ActivablePipeModel(Textures.LASER_PIPE_IN,
            Textures.LASER_PIPE_SIDE, Textures.LASER_PIPE_OVERLAY, Textures.LASER_PIPE_OVERLAY_EMISSIVE,
            true, "laser");

    private final Supplier<SpriteInformation> inTex;
    private final Supplier<SpriteInformation> sideTex;
    private final Supplier<SpriteInformation> overlayTex;
    private final Supplier<SpriteInformation> overlayActiveTex;

    private final boolean emissiveActive;

    public ActivablePipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                              @NotNull Supplier<SpriteInformation> overlayTex,
                              @NotNull Supplier<SpriteInformation> overlayActiveTex, boolean emissiveActive,
                              String variant) {
        super(new ModelResourceLocation(loc, variant));
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
        // don't render the main shape to the bloom layer
        List<BakedQuad> quads = super.getQuads(key, connectionMask, closedMask, blockedMask, data, frameMaterial, frameMask, coverMask);

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
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideTex.get();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return getParticleSprite(null).sprite();
    }

    @Override
    protected StructureQuadCache constructForKey(ActivableCacheKey key) {
        return ActivableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                overlayTex.get(), overlayActiveTex.get());
    }

    public boolean allowActive() {
        return !ConfigHolder.INSTANCE.client.preventAnimatedCables;
    }

    @Override
    protected @Nullable PipeItemModel<ActivableCacheKey> getItemModel(@NotNull ItemStack stack, ClientLevel world, LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(this, new ActivableCacheKey(block.getStructure().getRenderThickness(), false),
                new ColorData(PipeBlockEntity.DEFAULT_COLOR));
    }

    public static void registerModels(BiConsumer<ModelResourceLocation, BakedModel> registry) {
        registry.accept(OPTICAL.getLoc(), OPTICAL);
        registry.accept(LASER.getLoc(), LASER);
    }
}
