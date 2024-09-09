package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import com.gregtechceu.gtceu.client.renderer.pipe.util.MaterialModelOverride;
import com.gregtechceu.gtceu.client.renderer.pipe.util.MaterialModelSupplier;
import com.gregtechceu.gtceu.client.renderer.pipe.util.TextureInformation;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class PipeModelRegistry {

    public static final Map<ModelResourceLocation, PipeModelRedirector> MODELS = new HashMap<>();

    public static final int PIPE_MODEL_COUNT = 7;
    private static final Object2ObjectOpenHashMap<Material, PipeModel[]> PIPE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] PIPE_MODELS = new PipeModelRedirector[PIPE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<PipeModel>> PIPE_OVERRIDES = new ObjectLinkedOpenHashSet<>();
    private static final Object2ObjectOpenHashMap<Material, PipeModel[]> PIPE_RESTRICTIVE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] PIPE_RESTRICTIVE_MODELS = new PipeModelRedirector[PIPE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<PipeModel>> PIPE_RESTRICTIVE_OVERRIDES = new ObjectLinkedOpenHashSet<>();

    public static final int CABLE_MODEL_COUNT = 6;
    private static final Object2ObjectOpenHashMap<Material, CableModel[]> CABLE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] CABLE_MODELS = new PipeModelRedirector[CABLE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<CableModel>> CABLE_OVERRIDES = new ObjectLinkedOpenHashSet<>();

    private static final ActivablePipeModel OPTICAL;
    private static final PipeModelRedirector OPTICAL_MODEL;

    private static final ActivablePipeModel LASER;
    private static final PipeModelRedirector LASER_MODEL;

    private static final DuctPipeModel DUCT;
    private static final PipeModelRedirector DUCT_MODEL;

    static {
        initPipes();
        initCables();
        ResourceLocation loc = GTCEu.id("pipe_activable");
        OPTICAL = new ActivablePipeModel(
                new TextureInformation(GTCEu.id("block/pipe/pipe_optical_in"), -1),
                new TextureInformation(GTCEu.id("block/pipe/pipe_optical_side"), -1),
                new TextureInformation(GTCEu.id("block/pipe/pipe_optical_side_overlay"), 0),
                new TextureInformation(GTCEu.id("block/pipe/pipe_optical_side_overlay_active"), 0),
                false);
        OPTICAL_MODEL = new PipeModelRedirector(new ModelResourceLocation(loc, "optical"), m -> OPTICAL, s -> null);
        LASER = new ActivablePipeModel(
                new TextureInformation(GTCEu.id("block/pipe/pipe_laser_in"), -1),
                new TextureInformation(GTCEu.id("block/pipe/pipe_laser_side"), -1),
                new TextureInformation(GTCEu.id("block/pipe/pipe_laser_side_overlay"), 0),
                new TextureInformation(GTCEu.id("block/pipe/pipe_laser_side_overlay_emissive"), 0),
                true);
        LASER_MODEL = new PipeModelRedirector(new ModelResourceLocation(loc, "laser"), m -> LASER, s -> null);
        DUCT = new DuctPipeModel();
        DUCT_MODEL = new PipeModelRedirector(new ModelResourceLocation(GTCEu.id("pipe_duct"), ""), m -> DUCT,
                s -> null);
    }

    public static void registerPipeOverride(@NotNull MaterialModelOverride<PipeModel> override) {
        PIPE_OVERRIDES.addAndMoveToFirst(override);
        PIPE.clear();
        PIPE.trim(16);
    }

    public static void registerPipeRestrictiveOverride(@NotNull MaterialModelOverride<PipeModel> override) {
        PIPE_RESTRICTIVE_OVERRIDES.addAndMoveToFirst(override);
        PIPE_RESTRICTIVE.clear();
        PIPE_RESTRICTIVE.trim(16);
    }

    public static void registerCableOverride(@NotNull MaterialModelOverride<CableModel> override) {
        CABLE_OVERRIDES.addAndMoveToFirst(override);
        CABLE.clear();
        CABLE.trim(16);
    }

    public static PipeModelRedirector getPipeModel(@Range(from = 0, to = PIPE_MODEL_COUNT - 1) int i) {
        return PIPE_MODELS[i];
    }

    public static PipeModelRedirector getPipeRestrictiveModel(@Range(from = 0, to = PIPE_MODEL_COUNT - 1) int i) {
        return PIPE_RESTRICTIVE_MODELS[i];
    }

    public static PipeModelRedirector getCableModel(@Range(from = 0, to = CABLE_MODEL_COUNT - 1) int i) {
        return CABLE_MODELS[i];
    }

    public static PipeModelRedirector getOpticalModel() {
        return OPTICAL_MODEL;
    }

    public static PipeModelRedirector getLaserModel() {
        return LASER_MODEL;
    }

    public static PipeModelRedirector getDuctModel() {
        return DUCT_MODEL;
    }

    public static void registerModels(@NotNull BiConsumer<ModelResourceLocation, BakedModel> registry) {
        for (PipeModelRedirector redirector : PIPE_MODELS) {
            registry.accept(redirector.getLoc(), redirector);
        }
        for (PipeModelRedirector redirector : PIPE_RESTRICTIVE_MODELS) {
            registry.accept(redirector.getLoc(), redirector);
        }
        for (PipeModelRedirector redirector : CABLE_MODELS) {
            registry.accept(redirector.getLoc(), redirector);
        }
        registry.accept(OPTICAL_MODEL.getLoc(), OPTICAL_MODEL);
        registry.accept(LASER_MODEL.getLoc(), LASER_MODEL);
    }

    public static PipeModelRedirector materialModel(@NotNull ResourceLocation loc, MaterialModelSupplier supplier,
                                                    @NotNull String variant,
                                                    PipeModelRedirector.@NotNull ModelRedirectorSupplier redirectorSupplier) {
        return redirectorSupplier.create(new ModelResourceLocation(loc, variant), supplier,
                stack -> {
                    PipeMaterialBlock pipe = PipeMaterialBlock.getBlockFromItem(stack);
                    if (pipe == null) return null;
                    else return pipe.material;
                });
    }

    public static PipeModelRedirector materialModel(@NotNull ResourceLocation loc, MaterialModelSupplier supplier,
                                                    @NotNull String variant) {
        return new PipeModelRedirector(new ModelResourceLocation(loc, variant), supplier,
                stack -> {
                    PipeMaterialBlock pipe = PipeMaterialBlock.getBlockFromItem(stack);
                    if (pipe == null) return null;
                    else return pipe.material;
                });
    }

    private static void initPipes() {
        TextureInformation pipeTiny = new TextureInformation(GTCEu.id("block/pipe/pipe_tiny_in"), 0);
        TextureInformation pipeSmall = new TextureInformation(GTCEu.id("block/pipe/pipe_small_in"), 0);
        TextureInformation pipeNormal = new TextureInformation(GTCEu.id("block/pipe/pipe_normal_in"), 0);
        TextureInformation pipeLarge = new TextureInformation(GTCEu.id("block/pipe/pipe_large_in"), 0);
        TextureInformation pipeHuge = new TextureInformation(GTCEu.id("block/pipe/pipe_huge_in"), 0);
        TextureInformation pipeQuadruple = new TextureInformation(GTCEu.id("block/pipe/pipe_quadruple_in"), 0);
        TextureInformation pipeNonuple = new TextureInformation(GTCEu.id("block/pipe/pipe_nonuple_in"), 0);
        TextureInformation pipeSide = new TextureInformation(GTCEu.id("block/pipe/pipe_side"), 0);

        TextureInformation pipeSmallWood = new TextureInformation(GTCEu.id("block/pipe/pipe_small_in_wood"), 0);
        TextureInformation pipeNormalWood = new TextureInformation(GTCEu.id("block/pipe/pipe_normal_in_wood"), 0);
        TextureInformation pipeLargeWood = new TextureInformation(GTCEu.id("block/pipe/pipe_large_in_wood"), 0);
        TextureInformation pipeSideWood = new TextureInformation(GTCEu.id("block/pipe/pipe_side_wood"), 0);

        PipeModel[] array = new PipeModel[PIPE_MODEL_COUNT];
        // standard
        array[0] = new PipeModel(pipeTiny, pipeSide, false);
        array[1] = new PipeModel(pipeSmall, pipeSide, false);
        array[2] = new PipeModel(pipeNormal, pipeSide, false);
        array[3] = new PipeModel(pipeLarge, pipeSide, false);
        array[4] = new PipeModel(pipeHuge, pipeSide, false);
        array[5] = new PipeModel(pipeQuadruple, pipeSide, false);
        array[6] = new PipeModel(pipeNonuple, pipeSide, false);
        PIPE_OVERRIDES.addAndMoveToLast(new MaterialModelOverride.StandardOverride<>(array, m -> true));

        array = new PipeModel[PIPE_MODEL_COUNT];
        array[1] = new PipeModel(pipeSmallWood, pipeSideWood, false);
        array[2] = new PipeModel(pipeNormalWood, pipeSideWood, false);
        array[3] = new PipeModel(pipeLargeWood, pipeSideWood, false);
        registerPipeOverride(
                new MaterialModelOverride.StandardOverride<>(array, m -> m != null && m.hasProperty(PropertyKey.WOOD)));

        array = new PipeModel[PIPE_MODEL_COUNT];
        array[0] = new PipeModel(pipeTiny, pipeSide, true);
        array[1] = new PipeModel(pipeSmall, pipeSide, true);
        array[2] = new PipeModel(pipeNormal, pipeSide, true);
        array[3] = new PipeModel(pipeLarge, pipeSide, true);
        array[4] = new PipeModel(pipeHuge, pipeSide, true);
        array[5] = new PipeModel(pipeQuadruple, pipeSide, true);
        array[6] = new PipeModel(pipeNonuple, pipeSide, true);
        PIPE_RESTRICTIVE_OVERRIDES.addAndMoveToLast(new MaterialModelOverride.StandardOverride<>(array, m -> true));

        ResourceLocation loc = GTCEu.id("pipe_material");
        for (int i = 0; i < PIPE_MODEL_COUNT; i++) {
            int finalI = i;
            PIPE_MODELS[i] = materialModel(loc, m -> getOrCachePipeModel(m, finalI), String.valueOf(i));
            PIPE_RESTRICTIVE_MODELS[i] = materialModel(loc, m -> getOrCachePipeRestrictiveModel(m, finalI),
                    "restrictive_" + i);
        }
    }

    private static PipeModel getOrCachePipeModel(@Nullable Material m, int i) {
        if (m == null) return PIPE_OVERRIDES.last().getModel(null, i);
        PipeModel[] cached = PIPE.computeIfAbsent(m, k -> new PipeModel[PIPE_MODEL_COUNT]);
        PipeModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<PipeModel> override : PIPE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }

    private static PipeModel getOrCachePipeRestrictiveModel(Material m, int i) {
        if (m == null) return PIPE_RESTRICTIVE_OVERRIDES.last().getModel(null, i);
        PipeModel[] cached = PIPE_RESTRICTIVE.computeIfAbsent(m, k -> new PipeModel[PIPE_MODEL_COUNT]);
        PipeModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<PipeModel> override : PIPE_RESTRICTIVE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }

    private static void initCables() {
        CABLE_OVERRIDES.addAndMoveToLast(new MaterialModelOverride.PerMaterialOverride<>(
                Tables.newCustomTable(new IdentityHashMap<>(), Int2ObjectOpenHashMap::new), (material, insulation) -> {
                    if (insulation == 0) {
                        return new CableModel(material);
                    }
                    return new CableModel(material, CableModel.INSULATION[insulation - 1], CableModel.INSULATION_FULL);
                }, m -> true));

        ResourceLocation loc = GTCEu.id("cable");
        for (int i = 0; i < CABLE_MODEL_COUNT; i++) {
            int finalI = i;
            CABLE_MODELS[i] = materialModel(loc, m -> getOrCacheCableModel(m, finalI), String.valueOf(i));
        }
    }

    private static CableModel getOrCacheCableModel(@Nullable Material m, int i) {
        if (m == null) return CABLE_OVERRIDES.last().getModel(null, i);
        CableModel[] cached = CABLE.computeIfAbsent(m, k -> new CableModel[CABLE_MODEL_COUNT]);
        CableModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<CableModel> override : CABLE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }
}
