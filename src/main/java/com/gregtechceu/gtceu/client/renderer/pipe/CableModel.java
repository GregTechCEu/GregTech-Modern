package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ExtraCappedSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.CacheKey;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CableModel extends AbstractPipeModel<CacheKey> {

    public static final int DEFAULT_INSULATION_COLOR = 0xFF404040;

    private static final ResourceLocation loc = GTCEu.id("block/cable");

    public static final CableModel INSTANCE = new CableModel("wire");
    public static final CableModel[] INSULATED_INSTANCES = new CableModel[5];

    @OnlyIn(Dist.CLIENT)
    public static final Supplier<ResourceLocation> WIRE = () -> MaterialIconType.wire
            .getBlockTexturePath(MaterialIconSet.DULL, true).withSuffix("_side");
    @OnlyIn(Dist.CLIENT)
    public static final Supplier<ResourceLocation> INSULATION_FULL = () -> GTCEu.id("block/cable/insulation_5");

    static {
        for (int i = 0; i < INSULATED_INSTANCES.length; i++) {
            final int finalI = i;
            INSULATED_INSTANCES[i] = new CableModel(() -> GTCEu.id("block/cable/insulation_%s".formatted(finalI)),
                    INSULATION_FULL, "insulated_" + i);
        }
    }

    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> wireTex;
    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> insulationTex;
    private final SupplierMemoizer.MemoizedSupplier<ResourceLocation> fullInsulationTex;

    private SpriteInformation wireSprite;
    private SpriteInformation insulationSprite;
    private SpriteInformation fullInsulationSprite;

    public CableModel(@NotNull Supplier<ResourceLocation> wireTex, @Nullable Supplier<ResourceLocation> insulationTex,
                      @Nullable Supplier<ResourceLocation> fullInsulationTex, String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.wireTex = SupplierMemoizer.memoize(wireTex);
        this.insulationTex = SupplierMemoizer.memoize(insulationTex);
        this.fullInsulationTex = SupplierMemoizer.memoize(fullInsulationTex);
    }

    public CableModel(@Nullable Supplier<ResourceLocation> insulationTex,
                      @Nullable Supplier<ResourceLocation> fullInsulationTex, String variant) {
        this(WIRE, insulationTex, fullInsulationTex, variant);
    }

    public CableModel(String variant) {
        this(null, null, variant);
    }

    @Override
    protected ColorData computeColorData(@NotNull ModelData ext) {
        if (insulationTex == null) return super.computeColorData(ext);
        Material material = ext.get(AbstractPipeModel.MATERIAL_PROPERTY);
        int insulationColor = safeInt(ext.get(COLOR_PROPERTY));
        if (material != null) {
            int matColor = GTUtil.convertRGBtoARGB(material.getMaterialRGB());
            if (insulationColor == 0 || insulationColor == matColor) {
                // unpainted
                insulationColor = DEFAULT_INSULATION_COLOR;
            }
            return new ColorData(matColor, insulationColor);
        }
        return new ColorData(0, 0);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return wireSprite;
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull ModelData state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        if (fullInsulationSprite == null && fullInsulationTex != null) {
            fullInsulationSprite = new SpriteInformation(ModelFactory.getBlockSprite(fullInsulationTex.get()), 1);
        }
        if (insulationSprite == null && insulationTex != null) {
            insulationSprite = new SpriteInformation(ModelFactory.getBlockSprite(insulationTex.get()), 1);
        }
        if (wireSprite == null && wireTex != null) {
            wireSprite = new SpriteInformation(ModelFactory.getBlockSprite(wireTex.get()), 0);
        }

        SpriteInformation sideTex = fullInsulationSprite != null ? fullInsulationSprite : wireSprite;
        if (insulationSprite == null) {
            return StructureQuadCache.create(PipeQuadHelper.create(key.getThickness()), wireSprite, sideTex);
        } else {
            return ExtraCappedSQC.create(PipeQuadHelper.create(key.getThickness()), wireSprite, sideTex,
                    insulationSprite);
        }
    }

    @Override
    protected @Nullable PipeItemModel<CacheKey> getItemModel(@NotNull ItemStack stack, ClientLevel world,
                                                             LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock mat ? mat.material : null;
        return new PipeItemModel<>(this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData(mater != null ? GTUtil.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeBlockEntity.DEFAULT_COLOR, DEFAULT_INSULATION_COLOR));
    }

    public static void registerModels(BiConsumer<ModelResourceLocation, BakedModel> registry) {
        registry.accept(INSTANCE.getLoc(), INSTANCE);
        for (CableModel model : INSULATED_INSTANCES) {
            registry.accept(model.getLoc(), model);
        }
    }
}
