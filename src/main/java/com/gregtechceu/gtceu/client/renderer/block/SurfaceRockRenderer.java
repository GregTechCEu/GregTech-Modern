package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

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

            GTDynamicResourcePack.addBlockModel(blockId, new DelegatedModel(GTCEu.id("block/surface_rock")));
            GTDynamicResourcePack.addBlockState(blockId, MultiVariantGenerator
                    .multiVariant(model.block, Variant.variant().with(VariantProperties.MODEL, modelId))
                    .with(PropertyDispatch.property(BlockStateProperties.FACING)
                            .select(Direction.DOWN, Variant.variant())
                            .select(Direction.UP,
                                    Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(Direction.NORTH,
                                    Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.SOUTH,
                                    Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.WEST,
                                    Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.EAST,
                                    Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))));
            GTDynamicResourcePack.addItemModel(blockId, new DelegatedModel(modelId));
        }
    }

    private final Block block;

    protected SurfaceRockRenderer(Block block) {
        this.block = block;
    }
}
