package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ColorQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererPackage;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.reference.WeakHashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractPipeModel<K extends CacheKey> {

    public static ModelProperty<Float> THICKNESS_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Material> FRAME_MATERIAL_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Byte> FRAME_MASK_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Byte> CONNECTED_MASK_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Byte> CLOSED_MASK_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Byte> BLOCKED_MASK_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Integer> COLOR_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Material> MATERIAL_PROPERTY = new ModelProperty<>();

    protected final Object2ObjectOpenHashMap<BlockState, ColorQuadCache> frameCache = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<K, StructureQuadCache> pipeCache;

    protected static final WeakHashSet<Object2ObjectOpenHashMap<? extends CacheKey, StructureQuadCache>> PIPE_CACHES = new WeakHashSet<>();

    public static void invalidateCaches() {
        for (var cache : PIPE_CACHES) {
            cache.clear();
            cache.trim(16);
        }
    }

    public AbstractPipeModel() {
        pipeCache = new Object2ObjectOpenHashMap<>();
        PIPE_CACHES.add(pipeCache);
    }

    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData modelData,
                                             @Nullable RenderType renderType) {
        if (side == null) {
            ColorData data = computeColorData(modelData);
            CoverRendererPackage rendererPackage = modelData.get(CoverRendererPackage.PROPERTY);
            byte coverMask = rendererPackage == null ? 0 : rendererPackage.getMask();
            List<BakedQuad> quads = getQuads(toKey(modelData), safeByte(modelData.get(CONNECTED_MASK_PROPERTY)),
                    safeByte(modelData.get(CLOSED_MASK_PROPERTY)), safeByte(modelData.get(BLOCKED_MASK_PROPERTY)),
                    data, modelData.get(FRAME_MATERIAL_PROPERTY),
                    safeByte(modelData.get(FRAME_MASK_PROPERTY)), coverMask,
                    rand, modelData, renderType);
            if (rendererPackage != null) renderCovers(quads, rendererPackage, rand, modelData, renderType);
            return quads;
        }
        return Collections.emptyList();
    }

    protected void renderCovers(List<BakedQuad> quads, @NotNull CoverRendererPackage rendererPackage,
                                RandomSource rand, @NotNull ModelData data, RenderType renderType) {
        int color = safeInt(data.get(COLOR_PROPERTY));
        if (data.get(AbstractPipeModel.MATERIAL_PROPERTY) != null) {
            Material material = data.get(AbstractPipeModel.MATERIAL_PROPERTY);
            if (material != null) {
                int matColor = GTUtil.convertRGBtoARGB(material.getMaterialRGB());
                if (color == 0 || color == matColor) {
                    // unpainted
                    color = 0xFFFFFFFF;
                }
            }
        }
        rendererPackage.addQuads(quads, rand, data, new ColorData(color), renderType);
    }

    protected ColorData computeColorData(@NotNull ModelData data) {
        return new ColorData(safeInt(data.get(COLOR_PROPERTY)));
    }

    protected static byte safeByte(@Nullable Byte abyte) {
        return abyte == null ? 0 : abyte;
    }

    protected static int safeInt(@Nullable Integer integer) {
        return integer == null ? 0 : integer;
    }

    public @NotNull List<BakedQuad> getQuads(K key, byte connectionMask, byte closedMask, byte blockedMask,
                                             ColorData data,
                                             @Nullable Material frameMaterial, byte frameMask, byte coverMask,
                                             RandomSource randomSource, ModelData modelData, RenderType renderType) {
        List<BakedQuad> quads = new ObjectArrayList<>();

        StructureQuadCache cache = pipeCache.computeIfAbsent(key, this::constructForKey);
        cache.addToList(quads, connectionMask, closedMask,
                blockedMask, data, coverMask);

        if (frameMaterial != null) {
            BlockState state = GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, frameMaterial).getDefaultState();
            ColorQuadCache frame = frameCache.get(state);
            if (frame == null) {
                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
                frame = new ColorQuadCache(PipeQuadHelper
                        .createFrame(model, randomSource, modelData, renderType));
                frameCache.put(state, frame);
            }
            List<BakedQuad> frameQuads = frame
                    .getQuads(new ColorData(GTUtil.convertRGBtoARGB(frameMaterial.getMaterialRGB())));
            for (Direction dir : GTUtil.DIRECTIONS) {
                if ((frameMask & (1 << dir.ordinal())) > 0) {
                    quads.addAll(frameQuads.stream().filter(quad -> quad.getDirection() == dir).toList());
                }
            }
        }
        return quads;
    }

    protected abstract @NotNull K toKey(@NotNull ModelData state);

    protected final @NotNull CacheKey defaultKey(@NotNull ModelData state) {
        return CacheKey.of(state.get(THICKNESS_PROPERTY));
    }

    protected abstract StructureQuadCache constructForKey(K key);

    public TextureAtlasSprite getParticleTexture(int paintColor, @Nullable Material material) {
        SpriteInformation spriteInformation = getParticleSprite(material);
        return spriteInformation.sprite();
    }

    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return getParticleTexture(safeInt(data.get(COLOR_PROPERTY)), data.get(MATERIAL_PROPERTY));
    }

    public abstract SpriteInformation getParticleSprite(@Nullable Material material);

    @Nullable
    protected abstract PipeItemModel<K> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                     ClientLevel world, LivingEntity entity);

    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    }
}
