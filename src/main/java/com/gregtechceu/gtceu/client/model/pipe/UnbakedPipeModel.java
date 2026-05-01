package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.client.model.ModelBakingUtil;
import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.gregtechceu.gtceu.client.model.machine.MachineModelLoader.MISSING_MARKER;

public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    @Getter
    private final Map<@Nullable Direction, UnbakedModel> parts;
    @Getter
    private final Map<@NotNull Direction, UnbakedModel> restrictors;
    @Nullable
    private final Identifier parent;

    public UnbakedPipeModel(Map<@Nullable Direction, UnbakedModel> parts,
                            Map<@NotNull Direction, UnbakedModel> restrictors,
                            @Nullable Identifier parent) {
        this.parts = parts;
        this.restrictors = restrictors;
        this.parent = parent;
    }

    @Override
    @Nullable
    public Identifier parent() {
        return this.parent;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<Direction, BakedModel> bakedParts = new IdentityHashMap<>();
        this.parts.forEach((side, unbakedModel) -> bakedParts.put(side,
                ModelBakingUtil.bake(unbakedModel, context, baker, spriteGetter, modelState, overrides)));
        Map<Direction, BakedModel> bakedRestrictors = new IdentityHashMap<>();
        this.restrictors.forEach((side, unbakedModel) -> bakedRestrictors.put(side,
                ModelBakingUtil.bake(unbakedModel, context, baker, spriteGetter, modelState, overrides)));
        return new BakedPipeModel(bakedParts, bakedRestrictors);
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {
        UnbakedModel missingModel = resolver.apply(Identifier.withDefaultNamespace("missing"));

        Map<Direction, UnbakedModel> copy = new IdentityHashMap<>(this.parts);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.parts.put(side, missingModel);
            } else this.parts.put(side, variant);
        });
        copy = new IdentityHashMap<>(this.restrictors);
        copy.forEach((side, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                this.restrictors.put(side, missingModel);
            } else this.restrictors.put(side, variant);
        });
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(Identifier.withDefaultNamespace("missing"));
        if (this.parent != null) {
            resolver.markDependency(this.parent);
        }
        this.parts.values().forEach(model -> resolveDependency(model, resolver));
        this.restrictors.values().forEach(model -> resolveDependency(model, resolver));
    }

    private static void resolveDependency(@Nullable UnbakedModel model, ResolvableModel.Resolver resolver) {
        if (model == null || model == MISSING_MARKER) {
            return;
        }
        Identifier parent = model.parent();
        if (parent != null) {
            resolver.markDependency(parent);
        }
        model.resolveDependencies(resolver);
    }
}
