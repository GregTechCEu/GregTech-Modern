package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicMachineRendererRegistry;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicMachineRendererType;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MachineModelLoader implements IGeometryLoader<UnbakedMachineModel> {

    public static final MachineModelLoader INSTANCE = new MachineModelLoader();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer())
            .registerTypeAdapter(Variant.class, new Variant.Deserializer())
            .registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer())
            .create();
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);

    private static final Logger LOGGER = LogManager.getLogger("GT MACHINE MODEL LOADER");

    private MachineModelLoader() {}

    @Override
    public UnbakedMachineModel read(JsonObject jsonObject,
                                    JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        ResourceLocation machineId = new ResourceLocation(GsonHelper.getAsString(jsonObject, "machine"));

        JsonObject properties = GsonHelper.getAsJsonObject(jsonObject, "properties");

        Map<String, MultiVariant> variants = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            variants.put(entry.getKey(), GSON.fromJson(entry.getValue(), MultiVariant.class));
        }

        List<DynamicMachineRendererType> dynamicRenderers = new ArrayList<>();

        JsonArray renderersJson = GsonHelper.getAsJsonArray(jsonObject, "dynamic_renderers", null);
        if (renderersJson != null) {
            for (JsonElement typeName : renderersJson) {
                try {
                    ResourceLocation name = new ResourceLocation(typeName.getAsString());
                    DynamicMachineRendererType rendererType = DynamicMachineRendererRegistry.getType(name);
                    if (rendererType == null) {
                        throw new IllegalArgumentException("A Dynamic Renderer type named " + name + " does not exist");
                    }
                    dynamicRenderers.add(rendererType);
                } catch (UnsupportedOperationException | ResourceLocationException | IllegalArgumentException e) {
                    throw new JsonParseException("Entry " + typeName +
                            " is not a valid Dynamic Renderer", e);
                }
            }
        }

        return new UnbakedMachineModel(machineId, variants, dynamicRenderers);
    }

    public static Map<MachineRenderState, UnbakedModel> resolveStateModels(UnbakedMachineModel model) {
        UnbakedModel missingModel = BaseBakedModel.getModelBakery().getModel(ModelBakery.MISSING_MODEL_LOCATION);

        ResourceLocation machineId = model.getDefinition().getId();
        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = model.getDefinition()
                .getStateDefinition();
        ImmutableList<MachineRenderState> possibleStates = stateDefinition.getPossibleStates();
        Map<MachineRenderState, UnbakedModel> statesToModels = new IdentityHashMap<>();

        Map<ModelResourceLocation, MachineRenderState> modelsToStates = new HashMap<>();
        possibleStates.forEach((state) -> {
            modelsToStates.put(stateToModelLocation(machineId, state), state);
        });

        try {
            Map<MachineRenderState, UnbakedModel> curStatesToModels = new IdentityHashMap<>();

            model.getUnresolvedModels().forEach((key, multiVariantModel) -> {
                try {
                    possibleStates.stream().filter(predicate(stateDefinition, key)).forEach((state) -> {
                        UnbakedModel lastModel = curStatesToModels.put(state, multiVariantModel);
                        if (lastModel != null) {
                            curStatesToModels.put(state, missingModel);
                            throw new RuntimeException(
                                    "Overlapping definition with: " + model.getUnresolvedModels().entrySet()
                                            .stream().filter((entry) -> entry.getValue() == lastModel).findFirst().get()
                                            .getKey());
                        }
                    });
                } catch (Exception e) {
                    LOGGER.warn("Exception loading model for machine: '{}' for variant: '{}': {}", machineId, key, e);
                }
            });
            statesToModels.putAll(curStatesToModels);
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
}
