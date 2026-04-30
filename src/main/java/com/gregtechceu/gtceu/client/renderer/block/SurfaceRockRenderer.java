package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashSet;
import java.util.Set;

public class SurfaceRockRenderer {

    private static final Set<SurfaceRockRenderer> MODELS = new HashSet<>();

    public static void create(Block block) {
        MODELS.add(new SurfaceRockRenderer(block));
    }

    public static void reinitModels() {
        for (SurfaceRockRenderer model : MODELS) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            Identifier modelId = blockId.withPrefix("block/");

            GTDynamicResourcePack.addBlockModel(blockId, new DelegatedModel(GTCEu.id("block/surface_rock")));
            GTDynamicResourcePack.addBlockState(MultiVariantGenerator
                    .dispatch(model.block, BlockModelGenerators.plainVariant(modelId))
                    .with(PropertyDispatch.modify(BlockStateProperties.FACING)
                            .generate(SurfaceRockRenderer::facingMutator)));
            // Surface rock model uses tintindex 1 on its ore_bit elements; emit gtceu:item_color
            // tints so the runtime GTItemColors handler is invoked for the item form.
            GTDynamicResourcePack.addTintedItemModel(blockId, new DelegatedModel(modelId).get(), 2);
        }
    }

    private static VariantMutator facingMutator(Direction facing) {
        return switch (facing) {
            case DOWN -> GTBlockstateProvider.createRotationMutator(0, 0, 0);
            case UP -> GTBlockstateProvider.createRotationMutator(180, 0, 0);
            case NORTH -> GTBlockstateProvider.createRotationMutator(0, 90, 0);
            case SOUTH -> GTBlockstateProvider.createRotationMutator(0, 270, 0);
            case WEST -> GTBlockstateProvider.createRotationMutator(270, 0, 0);
            case EAST -> GTBlockstateProvider.createRotationMutator(90, 0, 0);
        };
    }

    private final Block block;

    protected SurfaceRockRenderer(Block block) {
        this.block = block;
    }
}
