package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.BlockableSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.RestrictiveSQC;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.StructureQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.PipeQuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.PipeSpriteWoodClarifier;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.client.renderer.pipe.util.WoodCacheKey;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

@OnlyIn(Dist.CLIENT)
public class PipeModel extends AbstractPipeModel<WoodCacheKey> {

    private static final ResourceLocation loc = GTCEu.id("block/pipe_material");

    public static final PipeModel[] INSTANCES = new PipeModel[7];
    public static final PipeModel[] RESTRICTIVE_INSTANCES = new PipeModel[INSTANCES.length];

    static {
        model(0, wood -> GTCEu.id("blocks/pipe/pipe_tiny_in"));
        model(1, wood -> wood ? GTCEu.id("blocks/pipe/pipe_small_in_wood") : GTCEu.id("blocks/pipe/pipe_small_in"));
        model(2, wood -> wood ? GTCEu.id("blocks/pipe/pipe_normal_in_wood") : GTCEu.id("blocks/pipe/pipe_normal_in"));
        model(3, wood -> wood ? GTCEu.id("blocks/pipe/pipe_large_in_wood") : GTCEu.id("blocks/pipe/pipe_large_in"));
        model(4, wood -> GTCEu.id("blocks/pipe/pipe_huge_in"));
        model(5, wood -> GTCEu.id("blocks/pipe/pipe_quadruple_in"));
        model(6, wood -> GTCEu.id("blocks/pipe/pipe_nonuple_in"));
    }

    private static void model(int i, PipeSpriteWoodClarifier clarifier) {
        INSTANCES[i] = new PipeModel(clarifier, false, i + "_standard");
        RESTRICTIVE_INSTANCES[i] = new PipeModel(clarifier, true, i + "_restrictive");
    }

    private final @NotNull PipeSpriteWoodClarifier inTex;
    private final @NotNull PipeSpriteWoodClarifier sideTex;
    private final @Nullable PipeSpriteWoodClarifier restrictiveTex;
    private final @NotNull PipeSpriteWoodClarifier blockedTex;

    private SpriteInformation inSprite;
    private SpriteInformation sideSprite;
    private SpriteInformation restrictiveSprite;
    private SpriteInformation blockedSprite;

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, @NotNull PipeSpriteWoodClarifier sideTex,
                     @Nullable PipeSpriteWoodClarifier restrictiveTex,
                     @NotNull PipeSpriteWoodClarifier blockedTex, String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.restrictiveTex = restrictiveTex;
        this.blockedTex = blockedTex;
    }

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, @NotNull PipeSpriteWoodClarifier sideTex,
                     boolean restrictive, String variant) {
        this(inTex, sideTex, restrictive ? wood -> GTCEu.id("blocks/pipe/pipe_restrictive") : null,
                wood -> GTCEu.id("blocks/pipe/pipe_blocked"), variant);
    }

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, boolean restrictive, String variant) {
        this(inTex, wood -> wood ? GTCEu.id("block/pipe/pipe_side_wood") : GTCEu.id("block/pipe/pipe_side"),
                restrictive, variant);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideSprite;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return getParticleSprite(null).sprite();
    }

    public @NotNull TextureAtlasSprite getParticleTexture(Material material) {
        return sideSprite.sprite();
    }

    @Override
    protected @NotNull WoodCacheKey toKey(@NotNull ModelData state) {
        return WoodCacheKey.of(state.get(THICKNESS_PROPERTY), state.get(MATERIAL_PROPERTY));
    }

    @Override
    protected StructureQuadCache constructForKey(WoodCacheKey key) {
        if (inSprite == null) {
            inSprite = new SpriteInformation(ModelFactory.getBlockSprite(inTex.getTexture(key.isWood())), 0);
        }
        if (sideSprite == null) {
            sideSprite = new SpriteInformation(ModelFactory.getBlockSprite(sideTex.getTexture(key.isWood())), 0);
        }
        if (restrictiveSprite == null && restrictiveTex != null) {
            restrictiveSprite = new SpriteInformation(
                    ModelFactory.getBlockSprite(restrictiveTex.getTexture(key.isWood())), -1);
        }
        if (blockedSprite == null) {
            blockedSprite = new SpriteInformation(ModelFactory.getBlockSprite(blockedTex.getTexture(key.isWood())), -1);
        }

        if (restrictiveTex != null) {
            return RestrictiveSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite,
                    sideSprite, blockedSprite,
                    restrictiveSprite);
        } else {
            return BlockableSQC.create(PipeQuadHelper.create(key.getThickness()), inSprite,
                    sideSprite, blockedSprite);
        }
    }

    @Override
    protected @Nullable PipeItemModel<WoodCacheKey> getItemModel(@NotNull ItemStack stack, ClientLevel world,
                                                                 LivingEntity entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = null;
        boolean wood = block instanceof MaterialPipeBlock mat && (mater = mat.material) != null &&
                mater.hasProperty(PropertyKey.WOOD);
        return new PipeItemModel<>(this, new WoodCacheKey(block.getStructure().getRenderThickness(), wood),
                new ColorData(mater != null ? GTUtil.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeBlockEntity.DEFAULT_COLOR));
    }

    public static void registerModels(BiConsumer<ModelResourceLocation, BakedModel> registry) {
        for (PipeModel model : INSTANCES) {
            registry.accept(model.getLoc(), model);
        }
        for (PipeModel model : RESTRICTIVE_INSTANCES) {
            registry.accept(model.getLoc(), model);
        }
    }
}
