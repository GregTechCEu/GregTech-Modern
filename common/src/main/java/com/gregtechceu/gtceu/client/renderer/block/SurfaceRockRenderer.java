package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceRockRenderer {
    private static final Set<SurfaceRockRenderer> MODELS = new HashSet<>();

    public static void create(Block block) {
        MODELS.add(new SurfaceRockRenderer(block));
    }

    public static void reinitModels() {
        for (SurfaceRockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = blockId.withPrefix("block/");

            GTDynamicResourcePack.addBlockModel(modelId, new DelegatedModel(GTCEu.id("block/surface_rock")));
            GTDynamicResourcePack.addBlockState(blockId, MultiVariantGenerator
                    .multiVariant(model.block, Variant.variant().with(VariantProperties.MODEL, modelId))
                    .with(BlockModelGenerators.createFacingDispatch()));
        }
    }

    private final Block block;

    protected SurfaceRockRenderer(Block block) {
        this.block = block;
    }
}
