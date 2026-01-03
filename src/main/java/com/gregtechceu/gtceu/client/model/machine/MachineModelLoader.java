package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.model.BasicUnbakedModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartSelector;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;
import com.gregtechceu.gtceu.client.model.machine.variant.VariantState;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.common.util.TransformationHelper;
import net.minecraftforge.fml.common.Mod;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MachineModelLoader implements IGeometryLoader<UnbakedMachineModel> {

    public static final MachineModelLoader INSTANCE = new MachineModelLoader();
    public static final ResourceLocation ID = GTCEu.id("machine");
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockModel.class, new ExtendedBlockModelDeserializer())
            .registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
            .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
            .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())

            .registerTypeAdapter(MultiVariantModel.class, new MultiVariantModel.Deserializer())
            .registerTypeAdapter(VariantState.class, new VariantState.Deserializer())
            .registerTypeAdapter(MultiPartSelector.class, new MultiPartSelector.Deserializer())
            .create();
    private static final Logger LOGGER = LogManager.getLogger("GT MACHINE MODEL LOADER");

    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    private static final UnbakedModel MISSING_MARKER = new BasicUnbakedModel();

    private MachineModelLoader() {}

    @Override
    public @Nullable UnbakedMachineModel read(JsonObject json,
                                              JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation machineId = new ResourceLocation(GsonHelper.getAsString(json, "machine"));
        MachineDefinition definition = GTRegistries.MACHINES.get(machineId);
        if (definition == null) return null;

        // load the inner models
        final Map<String, UnbakedModel> variants = new HashMap<>();
        if (json.has("variants")) {
            JsonObject variantsJson = GsonHelper.getAsJsonObject(json, "variants");
            for (Map.Entry<String, JsonElement> entry : variantsJson.entrySet()) {
                variants.put(entry.getKey(), GSON.fromJson(entry.getValue(), MultiVariantModel.class));
            }
        }
        final @Nullable MultiPartUnbakedModel multiPart;
        if (json.has("multipart")) {
            JsonArray multipartJson = GsonHelper.getAsJsonArray(json, "multipart");
            multiPart = MultiPartUnbakedModel.deserialize(definition, multipartJson);
        } else {
            multiPart = null;
        }
        if (variants.isEmpty() && (multiPart == null || multiPart.getModels().isEmpty())) {
            throw new JsonParseException("Model for machine %s doesn't have 'variants' or 'multipart' defined"
                    .formatted(machineId));
        }

        // resolve the state -> variant map
        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = definition.getStateDefinition();
        ImmutableList<MachineRenderState> possibleStates = stateDefinition.getPossibleStates();
        Map<MachineRenderState, UnbakedModel> statesToModels = new IdentityHashMap<>();
        if (multiPart != null) {
            possibleStates.forEach((state) -> statesToModels.put(state, multiPart));
        }

        Map<ModelResourceLocation, MachineRenderState> modelsToStates = new HashMap<>();
        possibleStates.forEach((state) -> {
            modelsToStates.put(stateToModelLocation(machineId, state), state);
        });

        try {
            variants.forEach((key, curModel) -> {
                try {
                    possibleStates.stream().filter(predicate(stateDefinition, key)).forEach((state) -> {
                        UnbakedModel prevModel = statesToModels.put(state, curModel);
                        if (prevModel != null && prevModel != multiPart) {
                            statesToModels.put(state, MISSING_MARKER);
                            throw new IllegalStateException(
                                    "Overlapping definition with: " + variants.entrySet().stream()
                                            .filter((entry) -> entry.getValue() == prevModel)
                                            .findFirst()
                                            .map(Map.Entry::getKey)
                                            .orElse("Invalid key? This shouldn't happen"));
                        }
                    });
                } catch (Exception e) {
                    LOGGER.warn("Exception loading model for machine: '{}' for variant: '{}': {}", machineId, key, e);
                }
            });
        } finally {
            modelsToStates.forEach((modelLoc, state) -> {
                UnbakedModel unbaked = statesToModels.get(state);
                if (unbaked == null) {
                    LOGGER.warn("Exception loading model for machine: '{}' missing model for variant: '{}'", machineId,
                            modelLoc);
                    statesToModels.put(state, MISSING_MARKER);
                }
            });
        }

        // load dynamic renders
        List<DynamicRender<?, ?>> dynamicRenders = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(json, "dynamic_renders", null);
        if (array != null) {
            for (JsonElement entry : array) {
                var render = DynamicRender.CODEC.parse(JsonOps.INSTANCE, entry)
                        .getOrThrow(true, LOGGER::error);
                dynamicRenders.add(render);
            }
        }

        // CTM info etc.
        Set<String> replaceableTextures = new HashSet<>();
        array = GsonHelper.getAsJsonArray(json, "replaceable_textures", null);
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                String entry = GsonHelper.convertToString(array.get(i), "replaceable_textures[%s]".formatted(i));
                replaceableTextures.add(entry);
            }
        }
        Map<String, ResourceLocation> textureOverrides = new HashMap<>();
        JsonObject overrideJson = GsonHelper.getAsJsonObject(json, "texture_overrides", null);
        if (overrideJson != null) {
            for (var entry : overrideJson.asMap().entrySet()) {
                String value = GsonHelper.convertToString(entry.getValue(), entry.getKey());
                textureOverrides.put(entry.getKey(), new ResourceLocation(value));
            }
        }

        return new UnbakedMachineModel(definition, statesToModels, multiPart, dynamicRenders,
                replaceableTextures, textureOverrides);
    }

    protected static void resolveStateModels(UnbakedMachineModel model,
                                             Function<ResourceLocation, UnbakedModel> resolver) {
        UnbakedModel missingModel = resolver.apply(ModelBakery.MISSING_MODEL_LOCATION);

        final MultiPartUnbakedModel multiPart = model.getMultiPart();
        if (multiPart != null) {
            multiPart.resolveParents(resolver);
        }
        Map<MachineRenderState, UnbakedModel> modelsCopy = new IdentityHashMap<>(model.getModels());
        modelsCopy.forEach((state, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                // replace null & markers with the actual missing model
                model.getModels().put(state, missingModel);
            } else {
                variant.resolveParents(resolver);
                model.getModels().put(state, variant);
            }
        });
    }

    private static Predicate<MachineRenderState> predicate(StateDefinition<MachineDefinition, MachineRenderState> container,
                                                           String variant) {
        Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();

        for (String propertyEntry : COMMA_SPLITTER.split(variant)) {
            Iterator<String> keyValue = EQUAL_SPLITTER.split(propertyEntry).iterator();
            if (keyValue.hasNext()) {
                String key = keyValue.next();
                Property<?> property = container.getProperty(key);
                if (property != null && keyValue.hasNext()) {
                    String value = keyValue.next();
                    Comparable<?> comparable = getValueHelper(property, value);
                    if (comparable == null) {
                        throw new RuntimeException("Unknown value: '" + value +
                                "' for machine model state property: '" + key + "' " + property.getPossibleValues());
                    }

                    properties.put(property, comparable);
                } else if (!key.isEmpty()) {
                    throw new RuntimeException("Unknown machine model state property: '" + key + "'");
                }
            }
        }

        MachineDefinition machine = container.getOwner();
        return (state) -> {
            if (state == null || !state.is(machine)) {
                return false;
            }
            for (var entry : properties.entrySet()) {
                if (!Objects.equals(state.getValue(entry.getKey()), entry.getValue())) {
                    return false;
                }
            }
            return true;
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> property, String value) {
        return property.getValue(value).orElse(null);
    }

    public static ModelResourceLocation stateToModelLocation(ResourceLocation location, MachineRenderState state) {
        return new ModelResourceLocation(location, BlockModelShaper.statePropertiesToString(state.getValues()));
    }

    public static Either<ResourceLocation, UnbakedModel> parseVariant(JsonElement value,
                                                                      JsonDeserializationContext context) throws JsonParseException {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            String modelName = value.getAsString();
            return Either.left(new ResourceLocation(modelName));
        } else {
            return Either.right(context.deserialize(value, BlockModel.class));
        }
    }
}
