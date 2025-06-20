package com.gregtechceu.gtceu.client.model.machine;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartSelector;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
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
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockModel.class, new ExtendedBlockModelDeserializer())
            .registerTypeAdapter(MultiPartSelector.class, new MultiPartSelector.Deserializer())
            .create();
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);

    private static final Logger LOGGER = LogManager.getLogger("GT MACHINE MODEL LOADER");

    private static final Map<ResourceLocation, List<DynamicRender<?, ?>>> DYNAMIC_RENDERERS = new HashMap<>();

    private MachineModelLoader() {}

    @SubscribeEvent
    public static void loadDynamicModels(ModelEvent.ModifyBakingResult event) {
        if (DYNAMIC_RENDERERS.isEmpty()) return;

        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (var entry : DYNAMIC_RENDERERS.entrySet()) {
            ResourceLocation machineId = entry.getKey();
            for (DynamicRender<?, ?> renderer : entry.getValue()) {
                String rendererName = renderer.getType().getId().getPath();

                String fakeModelPath = DynamicRenderManager.MODEL_ID_FORMATTER.apply(machineId.getPath(), rendererName);
                models.put(machineId.withPath(fakeModelPath), renderer);
            }
        }
    }

    @Override
    public UnbakedMachineModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation machineId = new ResourceLocation(GsonHelper.getAsString(json, "machine"));
        MachineDefinition definition = GTRegistries.MACHINES.get(machineId);
        Preconditions.checkNotNull(definition, "A machine with id " + machineId + " does not exist.");

        JsonObject variantsJson = GsonHelper.getAsJsonObject(json, "variants");

        Map<String, Either<ResourceLocation, UnbakedModel>> variants = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : variantsJson.entrySet()) {
            variants.put(entry.getKey(), parseVariant(entry.getValue()));
        }
        @Nullable MultiPartUnbakedModel multiPart = null;
        if (json.has("multipart")) {
            JsonArray multipartJson = GsonHelper.getAsJsonArray(json, "multipart");
            multiPart = MultiPartUnbakedModel.deserialize(definition, multipartJson, context);
        }
        if (variants.isEmpty() && (multiPart == null || multiPart.getModels().isEmpty())) {
            throw new JsonParseException("Model for machine %s doesn't have 'variants' or 'multipart' defined"
                    .formatted(machineId));
        }

        List<DynamicRender<?, ?>> dynamicRenders = new ArrayList<>();
        JsonArray renderList = GsonHelper.getAsJsonArray(json, "dynamic_renders", null);
        if (renderList != null) {
            for (JsonElement entry : renderList) {
                var render = DynamicRender.CODEC.parse(JsonOps.INSTANCE, entry)
                        .getOrThrow(false, LOGGER::error);
                dynamicRenders.add(render);
            }
            DYNAMIC_RENDERERS.put(machineId, dynamicRenders);
        }

        return new UnbakedMachineModel(definition, variants, multiPart, dynamicRenders);
    }

    protected static Map<MachineRenderState, UnbakedModel> resolveStateModels(UnbakedMachineModel model,
                                                                              Function<ResourceLocation, UnbakedModel> resolver) {
        UnbakedModel missingModel = resolver.apply(ModelBakery.MISSING_MODEL_LOCATION);

        ResourceLocation machineId = model.getDefinition().getId();
        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = model.getDefinition()
                .getStateDefinition();
        ImmutableList<MachineRenderState> possibleStates = stateDefinition.getPossibleStates();
        Map<MachineRenderState, UnbakedModel> statesToModels = new IdentityHashMap<>();

        MultiPartUnbakedModel multiPartModel;
        if (model.getMultiPart() != null) {
            multiPartModel = model.getMultiPart();
            multiPartModel.resolveParents(resolver);
            possibleStates.forEach((state) -> statesToModels.put(state, multiPartModel));
        } else {
            multiPartModel = null;
        }

        Map<ModelResourceLocation, MachineRenderState> modelsToStates = new HashMap<>();
        possibleStates.forEach((state) -> {
            modelsToStates.put(stateToModelLocation(machineId, state), state);
        });

        try {
            model.getUnresolvedModels().forEach((key, either) -> {
                try {
                    possibleStates.stream().filter(predicate(stateDefinition, key)).forEach((state) -> {
                        UnbakedModel curModel = either.map(resolver, Function.identity());

                        UnbakedModel prevModel = statesToModels.put(state, curModel);
                        if (prevModel != null && prevModel != multiPartModel) {
                            statesToModels.put(state, missingModel);
                            throw new IllegalStateException(
                                    "Overlapping definition with: " + model.getUnresolvedModels().entrySet().stream()
                                            .filter((entry) -> entry.getValue() == prevModel)
                                            .findFirst()
                                            .map(Map.Entry::getKey)
                                            .orElse("Invalid key?"));
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
                    statesToModels.put(state, missingModel);
                }
            });
        }
        return statesToModels;
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

    public static Either<ResourceLocation, UnbakedModel> parseVariant(JsonElement value) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            String modelName = value.getAsString();
            return Either.left(new ResourceLocation(modelName));
        } else {
            return Either.right(GSON.fromJson(value, BlockModel.class));
        }
    }
}
