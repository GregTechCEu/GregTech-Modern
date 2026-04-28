package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4fc;

import java.util.List;
import java.util.Optional;

public record LegacyCustomItemModel(Identifier modelLocation) implements ItemModel.Unbaked {

    public static final Identifier ID = GTCEu.id("legacy_item_model");
    public static final MapCodec<LegacyCustomItemModel> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Identifier.CODEC.fieldOf("model").forGetter(LegacyCustomItemModel::modelLocation))
            .apply(instance, LegacyCustomItemModel::new));

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
        return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.modelLocation);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(this.modelLocation);
        UnbakedModel unbakedModel = resolvedModel.wrapped();
        if (unbakedModel instanceof IUnbakedGeometry<?> legacyGeometry) {
            var bakingContext = new LegacyCustomBlockStateModel.LegacyGeometryBakingContext(resolvedModel);
            legacyGeometry.resolveParents(id -> baker.getModel(id).wrapped(), bakingContext);
            return legacyGeometry.bake(bakingContext, baker,
                    material -> baker.materials().get(material, resolvedModel).sprite(),
                    new LegacyCustomBlockStateModel.DelegatingModelState(BlockModelRotation.IDENTITY),
                    ItemOverrides.EMPTY);
        }

        return new CuboidItemModelWrapper.Unbaked(this.modelLocation, Optional.empty(), List.of())
                .bake(context, transformation);
    }

    public static JsonObject itemDefinitionJson(Identifier modelLocation) {
        JsonObject model = new JsonObject();
        model.addProperty("type", ID.toString());
        model.addProperty("model", modelLocation.toString());

        JsonObject root = new JsonObject();
        root.add("model", model);
        return root;
    }
}
