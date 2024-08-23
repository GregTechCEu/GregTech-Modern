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
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ActivablePipeModel extends AbstractPipeModel<ActivableCacheKey> {

    private static final ResourceLocation loc = GTCEu.id("pipe_activable");

    public static final ModelProperty<Boolean> ACTIVE_PROPERTY = new ModelProperty<>();

    public static final ActivablePipeModel OPTICAL = new ActivablePipeModel(
            () -> GTCEu.id("block/pipe/pipe_optical_in"),
            () -> GTCEu.id("block/pipe/pipe_optical_side"),
            () -> GTCEu.id("block/pipe/pipe_optical_side_overlay"),
            () -> GTCEu.id("block/pipe/pipe_optical_side_overlay_active"),
            false, "optical");
    public static final ActivablePipeModel LASER = new ActivablePipeModel(() -> GTCEu.id("block/pipe/pipe_laser_in"),
            () -> GTCEu.id("block/pipe/pipe_laser_side"),
            () -> GTCEu.id("block/pipe/pipe_laser_side_overlay"),
            () -> GTCEu.id("block/pipe/pipe_laser_side_overlay_emissive"),
            true, "laser");

    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> inTex;
    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> sideTex;
    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> overlayTex;
    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> overlayActiveTex;

    private SpriteInformation inSprite;
    private SpriteInformation sideSprite;
    private SpriteInformation overlaySprite;
    private SpriteInformation overlayActiveSprite;

    private final boolean emissiveActive;

    public ActivablePipeModel(@NotNull Supplier<ResourceLocation> inTex, @NotNull Supplier<ResourceLocation> sideTex,
                              @NotNull Supplier<ResourceLocation> overlayTex,
                              @NotNull Supplier<ResourceLocation> overlayActiveTex, boolean emissiveActive,
                              String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.inTex = SupplierMemoizer.memoize(inTex);
        this.sideTex = SupplierMemoizer.memoize(sideTex);
        this.overlayTex = SupplierMemoizer.memoize(overlayTex);
        this.overlayActiveTex = SupplierMemoizer.memoize(overlayActiveTex);
        this.emissiveActive = emissiveActive;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(ActivableCacheKey key, byte connectionMask, byte closedMask,
                                             byte blockedMask, ColorData data, @Nullable Material frameMaterial,
                                             byte frameMask, byte coverMask) {
        // don't render the main shape to the bloom layer
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
        if (inSprite == null && inTex != null) {
            inSprite = new SpriteInformation(ModelFactory.getBlockSprite(inTex.get()), -1);
        }
        if (sideSprite == null && sideTex != null) {
            sideSprite = new SpriteInformation(ModelFactory.getBlockSprite(sideTex.get()), -1);
        }
        if (overlaySprite == null && overlayTex != null) {
            overlaySprite = new SpriteInformation(ModelFactory.getBlockSprite(overlayTex.get()), 0);
        }
        if (overlayActiveSprite == null && overlayActiveTex != null) {
            overlayActiveSprite = new SpriteInformation(ModelFactory.getBlockSprite(overlayActiveTex.get()), 0);
        }

        return ActivableSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite, sideSprite,
                overlaySprite, overlayActiveSprite);
    }

    public boolean allowActive() {
        return !ConfigHolder.INSTANCE.client.preventAnimatedCables;
    }

    @Override
    protected @Nullable PipeItemModel<ActivableCacheKey> getItemModel(@NotNull ItemStack stack, ClientLevel world,
                                                                      LivingEntity entity) {
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
