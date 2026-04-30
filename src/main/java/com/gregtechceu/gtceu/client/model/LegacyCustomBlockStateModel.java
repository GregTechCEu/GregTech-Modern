package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ItemTransforms;
import com.gregtechceu.gtceu.client.model.compat.ModelState;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4fc;

public record LegacyCustomBlockStateModel(Identifier modelLocation, Variant.SimpleModelState modelState)
        implements CustomUnbakedBlockStateModel {

    public static final Identifier ID = GTCEu.id("legacy_model");
    public static final MapCodec<LegacyCustomBlockStateModel> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                    .group(Identifier.CODEC.fieldOf("model").forGetter(LegacyCustomBlockStateModel::modelLocation),
                            Variant.SimpleModelState.MAP_CODEC.forGetter(LegacyCustomBlockStateModel::modelState))
                    .apply(instance, LegacyCustomBlockStateModel::new));

    public LegacyCustomBlockStateModel(Identifier modelLocation) {
        this(modelLocation, Variant.SimpleModelState.DEFAULT);
    }

    public LegacyCustomBlockStateModel with(VariantMutator mutator) {
        Variant variant = mutator.apply(new Variant(this.modelLocation, this.modelState));
        return new LegacyCustomBlockStateModel(variant.modelLocation(), variant.modelState());
    }

    public static Builder builder(Identifier modelLocation) {
        return new Builder(new LegacyCustomBlockStateModel(modelLocation));
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        ResolvedModel resolvedModel = baker.getModel(this.modelLocation);
        UnbakedModel unbakedModel = resolvedModel.wrapped();
        net.minecraft.client.renderer.block.dispatch.ModelState bakedState = this.modelState.asModelState();
        if (unbakedModel instanceof IUnbakedGeometry<?> legacyGeometry) {
            LegacyGeometryBakingContext context = new LegacyGeometryBakingContext(resolvedModel);
            legacyGeometry.resolveParents(id -> baker.getModel(id).wrapped(), context);
            return legacyGeometry.bake(context, baker,
                    material -> baker.materials().get(material, resolvedModel).sprite(),
                    new DelegatingModelState(bakedState), ItemOverrides.EMPTY);
        }

        BlockStateModelPart modelPart = SimpleModelWrapper.bake(baker, resolvedModel, bakedState);
        return new SingleVariant(modelPart);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.modelLocation);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return MAP_CODEC;
    }

    public static JsonObject singleVariantJson(Identifier modelLocation) {
        JsonObject variants = new JsonObject();
        variants.add("", variantJson(modelLocation));

        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    public static JsonObject activeVariantJson(Identifier inactiveModelLocation, Identifier activeModelLocation) {
        JsonObject variants = new JsonObject();
        variants.add("active=false", variantJson(inactiveModelLocation));
        variants.add("active=true", variantJson(activeModelLocation));

        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    /**
     * Build a blockstate variants JSON keyed by the machine's facing (and upwards-facing if extended).
     * Mirrors {@code GTBlockstateProvider.createFacingDispatch} so dynamically-registered machine blocks
     * get the same per-FACING rotation as datagen produces.
     */
    public static JsonObject facingVariantsJson(Identifier modelLocation,
                                                com.gregtechceu.gtceu.api.data.RotationState rotationState,
                                                boolean allowExtendedFacing) {
        if (rotationState == com.gregtechceu.gtceu.api.data.RotationState.NONE) {
            return singleVariantJson(modelLocation);
        }

        JsonObject variants = new JsonObject();
        String facingKey = rotationState.property.getName();
        for (net.minecraft.core.Direction front : rotationState.property.getPossibleValues()) {
            if (allowExtendedFacing) {
                for (net.minecraft.core.Direction up : com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties.UPWARDS_FACING
                        .getPossibleValues()) {
                    var orient = com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation.get(front, up);
                    String key = facingKey + "=" + front.getName() + ",upwards_facing=" + up.getName();
                    variants.add(key, variantJsonWithRotation(modelLocation,
                            orient.getAngleX(), orient.getAngleY(), orient.getAngleZ()));
                }
            } else {
                var orient = com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation.get(front,
                        net.minecraft.core.Direction.NORTH);
                String key = facingKey + "=" + front.getName();
                variants.add(key, variantJsonWithRotation(modelLocation,
                        orient.getAngleX(), orient.getAngleY(), orient.getAngleZ()));
            }
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject variantJson(Identifier modelLocation) {
        JsonObject variant = new JsonObject();
        variant.addProperty("type", ID.toString());
        variant.addProperty("model", modelLocation.toString());
        return variant;
    }

    private static JsonObject variantJsonWithRotation(Identifier modelLocation, int x, int y, int z) {
        JsonObject variant = variantJson(modelLocation);
        if (x != 0) variant.addProperty("x", x);
        if (y != 0) variant.addProperty("y", y);
        if (z != 0) variant.addProperty("z", z);
        return variant;
    }

    public static final class Builder extends CustomBlockStateModelBuilder {

        private final LegacyCustomBlockStateModel blockStateModel;

        private Builder(LegacyCustomBlockStateModel blockStateModel) {
            this.blockStateModel = blockStateModel;
        }

        @Override
        public Builder with(VariantMutator variantMutator) {
            return new Builder(this.blockStateModel.with(variantMutator));
        }

        @Override
        public Builder with(UnbakedMutator variantMutator) {
            return new Builder(variantMutator.apply(this.blockStateModel));
        }

        @Override
        public LegacyCustomBlockStateModel toUnbaked() {
            return this.blockStateModel;
        }
    }

    public record LegacyGeometryBakingContext(ResolvedModel model) implements IGeometryBakingContext {

        @Override
        public ItemTransforms getTransforms() {
            return new ItemTransforms(this.model.getTopTransforms());
        }

        @Override
        public Transformation getRootTransform() {
            ContextMap properties = this.model.getTopAdditionalProperties();
            return properties.getOrDefault(NeoForgeModelProperties.TRANSFORM, Transformation.IDENTITY);
        }

        @Override
        public boolean useBlockLight() {
            return this.model.getTopGuiLight().lightLikeBlock();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.model.getTopAmbientOcclusion();
        }

        @Override
        public boolean hasMaterial(String name) {
            return this.model.getTopTextureSlots().getMaterial(name) != null;
        }

        @Override
        public Material getMaterial(String name) {
            TextureSlots textureSlots = this.model.getTopTextureSlots();
            Material material = textureSlots.getMaterial(name);
            if (material == null) {
                throw new IllegalArgumentException("No material named " + name + " in " + this.model.debugName());
            }
            return material;
        }
    }

    public record DelegatingModelState(net.minecraft.client.renderer.block.dispatch.ModelState delegate)
            implements ModelState {

        @Override
        public Transformation transformation() {
            return this.delegate.transformation();
        }

        @Override
        public Matrix4fc faceTransformation(Direction direction) {
            return this.delegate.faceTransformation(direction);
        }

        @Override
        public Matrix4fc inverseFaceTransformation(Direction direction) {
            return this.delegate.inverseFaceTransformation(direction);
        }

        @Override
        public boolean mayApplyArbitraryRotation() {
            return this.delegate.mayApplyArbitraryRotation();
        }
    }
}
