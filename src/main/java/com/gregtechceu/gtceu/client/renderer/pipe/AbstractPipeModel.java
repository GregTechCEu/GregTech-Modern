package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ColorQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererPackage;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractPipeModel<K extends CacheKey> implements BakedModel {

    public static ModelProperty<Float> THICKNESS_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Material> FRAME_MATERIAL_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Byte> FRAME_MASK_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Byte> CLOSED_MASK_PROPERTY = new ModelProperty<>();
    public static ModelProperty<Byte> BLOCKED_MASK_PROPERTY = new ModelProperty<>();

    public static ModelProperty<Integer> COLOR_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Material> MATERIAL_PROPERTY = new ModelProperty<>();

    protected final Object2ObjectOpenHashMap<ResourceLocation, ColorQuadCache> frameCache = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<K, StructureQuadCache> pipeCache = new Object2ObjectOpenHashMap<>();

    @Getter
    private final ModelResourceLocation loc;

    public AbstractPipeModel(ModelResourceLocation loc) {
        this.loc = loc;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData modelData,
                                             @Nullable RenderType renderType) {
        if (side == null) {
            ColorData data = computeColorData(modelData);
            CoverRendererPackage rendererPackage = modelData.get(CoverRendererPackage.PROPERTY);
            byte coverMask = rendererPackage == null ? 0 : rendererPackage.getMask();
            List<BakedQuad> quads = getQuads(toKey(modelData), PipeBlock.readConnectionMask(state),
                    safeByte(modelData.get(CLOSED_MASK_PROPERTY)), safeByte(modelData.get(BLOCKED_MASK_PROPERTY)),
                    data, modelData.get(FRAME_MATERIAL_PROPERTY),
                    safeByte(modelData.get(FRAME_MASK_PROPERTY)), coverMask);
            if (rendererPackage != null) renderCovers(quads, rendererPackage, rand, modelData);
            return quads;
        }
        return Collections.emptyList();
    }

    protected void renderCovers(List<BakedQuad> quads, @NotNull CoverRendererPackage rendererPackage,
                                RandomSource rand, @NotNull ModelData data) {
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
        rendererPackage.addQuads(quads, rand, new ColorData(color));
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
                                             @Nullable Material frameMaterial, byte frameMask, byte coverMask) {
        List<BakedQuad> quads = new ObjectArrayList<>();

        StructureQuadCache cache = pipeCache.get(key);
        if (cache == null) {
            cache = constructForKey(key);
            pipeCache.put(key, cache);
        }
        cache.addToList(quads, connectionMask, closedMask,
                blockedMask, data, coverMask);

        if (frameMaterial != null) {
            ResourceLocation rl = MaterialIconType.frameGt.getBlockTexturePath(frameMaterial.getMaterialIconSet(),
                    true);
            ColorQuadCache frame = frameCache.get(rl);
            if (frame == null) {
                frame = new ColorQuadCache(PipeQuadHelper
                        .createFrame(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(rl)));
                frameCache.put(rl, frame);
            }
            List<BakedQuad> frameQuads = frame
                    .getQuads(new ColorData(GTUtil.convertRGBtoARGB(frameMaterial.getMaterialRGB())));
            for (int i = 0; i < 6; i++) {
                if ((frameMask & (1 << i)) > 0) {
                    quads.add(frameQuads.get(i));
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

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return BakedModel.super.getParticleIcon(data);
    }

    public abstract SpriteInformation getParticleSprite(@Nullable Material material);

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Nullable
    protected abstract PipeItemModel<K> getItemModel(@NotNull ItemStack stack, ClientLevel world, LivingEntity entity);

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return FakeItemOverrideList.INSTANCE;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    }

    protected static class FakeItemOverrideList extends ItemOverrides {

        public static final FakeItemOverrideList INSTANCE = new FakeItemOverrideList();

        @Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int seed) {
            if (originalModel instanceof AbstractPipeModel<?> model) {
                PipeItemModel<?> item = model.getItemModel(stack, level, entity);
                if (item != null) return item;
            }
            return originalModel;
        }
    }
}
