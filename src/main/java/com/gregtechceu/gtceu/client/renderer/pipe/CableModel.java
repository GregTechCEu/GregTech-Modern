package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
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
import com.gregtechceu.gtceu.client.renderer.pipe.util.TextureInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableModel extends AbstractPipeModel<CacheKey> {

    public static final int DEFAULT_INSULATION_COLOR = 0xFF404040;

    private static final ResourceLocation loc = GTCEu.id("pipe_cable");

    public static final TextureInformation WIRE = new TextureInformation(GTCEu.id("block/cable/wire"), 0);
    public static final TextureInformation[] INSULATION = new TextureInformation[5];
    public static final TextureInformation INSULATION_FULL = new TextureInformation(
            GTCEu.id("block/cable/insulation_5"), 1);

    static {
        for (int i = 0; i < INSULATION.length; i++) {
            INSULATION[i] = new TextureInformation(GTCEu.id("block/cable/insulation_%s".formatted(i)), 1);
        }
    }

    private final TextureInformation wireTex;
    private final TextureInformation insulationTex;
    private final TextureInformation fullInsulationTex;

    private final Material material;

    private SpriteInformation wireSprite;
    private SpriteInformation insulationSprite;
    private SpriteInformation fullInsulationSprite;

    public CableModel(@Nullable Material material, @Nullable TextureInformation insulationTex,
                      @Nullable TextureInformation fullInsulationTex) {
        this.material = material;
        this.wireTex = material != null ?
                new TextureInformation(
                        MaterialIconType.wire.getBlockTexturePath(material.getMaterialIconSet(), "side", true), 0) :
                WIRE;
        this.insulationTex = insulationTex;
        this.fullInsulationTex = fullInsulationTex;
    }

    public CableModel(@NotNull Material material) {
        this(material, null, null);
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
            fullInsulationSprite = new SpriteInformation(ModelFactory.getBlockSprite(fullInsulationTex.texture()),
                    fullInsulationTex.colorID());
        }
        if (insulationSprite == null && insulationTex != null) {
            insulationSprite = new SpriteInformation(ModelFactory.getBlockSprite(insulationTex.texture()),
                    insulationTex.colorID());
        }
        if (wireSprite == null && wireTex != null) {
            wireSprite = new SpriteInformation(ModelFactory.getBlockSprite(wireTex.texture()), wireTex.colorID());
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
    protected @Nullable PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                             ClientLevel world, LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock mat ? mat.material : null;
        return new PipeItemModel<>(redirector, this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData(mater != null ? GTUtil.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeBlockEntity.DEFAULT_COLOR, DEFAULT_INSULATION_COLOR));
    }
}
