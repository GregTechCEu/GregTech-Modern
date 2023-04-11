package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class GTRegistryObjectBuilderTypes<K, V> {
    @FunctionalInterface
    public interface BuilderFactory<T> {
        BuilderBase<? extends T> createBuilder(ResourceLocation id, Object... args);

    }
    public record BuilderType<T>(String type, Class<? extends BuilderBase<? extends T>> builderClass, BuilderFactory<T> factory) {
    }

    public static final Map<GTRegistry<?, ?>, GTRegistryObjectBuilderTypes<?, ?>> MAP = new LinkedHashMap<>();
    public static final Map<GTRegistry<?, ?>, List<GTRegistryObjectBuilderTypes<?, ?>>> POST_AT = new HashMap<>();
    public static final List<BuilderBase<?>> ALL_BUILDERS = new ArrayList<>();

    public static final GTRegistryObjectBuilderTypes<String, Element> ELEMENT = add(GTRegistries.ELEMENTS, Element.class);
    public static final GTRegistryObjectBuilderTypes<String, Material> MATERIAL = add(GTRegistries.MATERIALS, Material.class);
    public static final GTRegistryObjectBuilderTypes<ResourceLocation, GTRecipeType> RECIPE_TYPE = add(GTRegistries.RECIPE_TYPES, GTRecipeType.class);
    public static final GTRegistryObjectBuilderTypes<ResourceLocation, MachineDefinition> MACHINE = add(GTRegistries.MACHINES, MachineDefinition.class);
    /*public static final GTRegistryObjectBuilderTypes<String, RecipeCapability<?>> RECIPE_CAPABILITY = add(GTRegistries.RECIPE_CAPABILITIES, RecipeCapability.class);
    public static final GTRegistryObjectBuilderTypes<String, Class<? extends RecipeCondition>> RECIPE_CONDITION = add(GTRegistries.RECIPE_CONDITIONS, RecipeCondition.class);
    public static final GTRegistryObjectBuilderTypes<ResourceLocation, SoundEntry> SOUND = add(GTRegistries.SOUNDS, SoundEntry.class);*/

    public final ResourceLocation registryKey;
    public final Class<V> objectBaseClass;
    public final GTRegistry<K, V> registry;
    public final Map<String, BuilderType<V>> types;
    public final Map<ResourceLocation, BuilderBase<? extends V>> objects;
    private BuilderType<V> defaultType;
    public BuilderBase<? extends V> current;

    private GTRegistryObjectBuilderTypes(GTRegistry<K, V> registry, Class<V> baseClass) {
        registryKey = registry.getRegistryName();
        objectBaseClass = baseClass;
        this.registry = registry;
        types = new LinkedHashMap<>();
        objects = new LinkedHashMap<>();
        current = null;
    }

    public static <K, V> GTRegistryObjectBuilderTypes<K, V> add(GTRegistry<K, V> key, Class<?> baseClass) {
        var types = new GTRegistryObjectBuilderTypes<>(key, UtilsJS.cast(baseClass));

        if (MAP.put(key, types) != null) {
            throw new IllegalStateException("Registry with id '" + key + "' already exists!");
        }

        POST_AT.computeIfAbsent(key, (k) -> new LinkedList<>()).add(types);

        return types;
    }

    public void addType(String type, Class<? extends BuilderBase<? extends V>> builderType, BuilderFactory<V> factory, boolean isDefault) {
        var b = new BuilderType<>(type, builderType, factory);
        types.put(type, b);

        if (isDefault) {
            if (defaultType != null) {
                ConsoleJS.STARTUP.warn("Previous default type '" + defaultType.type + "' for registry '" + registryKey + "' replaced with '" + type + "'!");
            }

            defaultType = b;
        }
    }

    public void addBuilder(BuilderBase<? extends V> builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Can't add null builder in registry '" + registryKey + "'!");
        }

        if (CommonProperties.get().debugInfo) {
            ConsoleJS.STARTUP.info("~ " + registryKey + " | " + builder.id);
        }

        if (objects.containsKey(builder.id)) {
            throw new IllegalArgumentException("Duplicate key '" + builder.id + "' in registry '" + registryKey + "'!");
        }

        objects.put(builder.id, builder);
        ALL_BUILDERS.add(builder);
    }

    public BuilderType<V> getDefaultType() {
        if (types.isEmpty()) {
            return null;
        } else if (defaultType == null) {
            defaultType = types.values().iterator().next();
        }

        return defaultType;
    }

    public void postEvent() {
        GTCEuStartupEvents.REGISTRY.post(registryKey, new GTRegistryEventJS<>(this));
    }

    public static void registerFor(GTRegistry<?, ?> registry) {
        for (var type : POST_AT.getOrDefault(registry, List.of())) {
            type.postEvent();

            for (var builder : type.objects.values()) {
                builder.register();
            }
        }
    }
}
