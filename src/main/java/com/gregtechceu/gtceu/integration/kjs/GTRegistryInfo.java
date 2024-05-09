package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.materials.Element;
import com.gregtechceu.gtceu.api.materials.material.Material;
import com.gregtechceu.gtceu.api.materials.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.materials.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.materials.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.machines.MachineDefinition;
import com.gregtechceu.gtceu.api.recipes.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

public class GTRegistryInfo<K, V> {
    @FunctionalInterface
    public interface BuilderFactory<T> {
        BuilderBase<? extends T> createBuilder(ResourceLocation id, Object... args);
    }
    public record BuilderType<T>(String type, Class<? extends BuilderBase<? extends T>> builderClass, BuilderFactory<T> factory) { }
    public static final MaterialRegistry KJS_MATERIAL_REGISTRY = MaterialRegistryManager.getInstance().createRegistry(KubeJS.MOD_ID);

    public static final Map<ResourceLocation, GTRegistryInfo<?, ?>> MAP = new LinkedHashMap<>();
    public static final Set<ResourceLocation> EXTRA_IDS = new HashSet<>();

    public static final Map<ResourceLocation, List<GTRegistryInfo<?, ?>>> POST_AT = new HashMap<>();
    public static final List<BuilderBase<?>> ALL_BUILDERS = new ArrayList<>();

    public static final GTRegistryInfo<String, Element> ELEMENT = add(GTRegistries.ELEMENTS, Element.class);
    public static final GTRegistryInfo<String, Material> MATERIAL = add(KJS_MATERIAL_REGISTRY, Material.class);
    public static final GTRegistryInfo<ResourceLocation, GTRecipeType> RECIPE_TYPE = add(GTRegistries.RECIPE_TYPES, GTRecipeType.class);
    public static final GTRegistryInfo<ResourceLocation, MachineDefinition> MACHINE = add(GTRegistries.MACHINES, MachineDefinition.class);
    public static final GTRegistryInfo<String, MaterialIconSet> MATERIAL_ICON_SET = add(GTCEu.id("material_icon_set"), () -> MaterialIconSet.ICON_SETS, MaterialIconSet.class);
    public static final GTRegistryInfo<String, MaterialIconType> MATERIAL_ICON_TYPE = add(GTCEu.id("material_icon_type"), () -> MaterialIconType.ICON_TYPES, MaterialIconType.class);
    public static final GTRegistryInfo<String, IWorldGenLayer> WORLD_GEN_LAYER = add(GTCEu.id("world_gen_layer"), () -> WorldGeneratorUtils.WORLD_GEN_LAYERS, SimpleWorldGenLayer.class);
    public static final GTRegistryInfo<String, TagPrefix> TAG_PREFIX = add(GTCEu.id("tag_prefix"), () -> TagPrefix.PREFIXES, KJSTagPrefix.class);
    /*public static final GTRegistryInfo<String, RecipeCapability<?>> RECIPE_CAPABILITY = add(GTRegistries.RECIPE_CAPABILITIES, RecipeCapability.class);
    public static final GTRegistryInfo<String, Class<? extends RecipeCondition>> RECIPE_CONDITION = add(GTRegistries.RECIPE_CONDITIONS, RecipeCondition.class);
    public static final GTRegistryInfo<ResourceLocation, SoundEntry> SOUND = add(GTRegistries.SOUNDS, SoundEntry.class);*/

    public final ResourceLocation registryKey;
    public final Class<V> objectBaseClass;
    public final Map<String, BuilderType<V>> types;
    public final Map<ResourceLocation, BuilderBase<? extends V>> objects;
    public final Supplier<Map<K, V>> registryValues;
    private BuilderType<V> defaultType;
    public BuilderBase<? extends V> current;

    private GTRegistryInfo(ResourceLocation key, Supplier<Map<K, V>> registryValues, Class<V> baseClass) {
        registryKey = key;
        objectBaseClass = baseClass;
        types = new LinkedHashMap<>();
        objects = new LinkedHashMap<>();
        this.registryValues = registryValues;
        current = null;
    }


    public static <K, V> GTRegistryInfo<K, V> add(GTRegistry<K, V> key, Class<?> baseClass) {
        ResourceLocation id = key.getRegistryName();
        var types = new GTRegistryInfo<>(id, key::registry, UtilsJS.cast(baseClass));

        if (MAP.put(id, types) != null) {
            throw new IllegalStateException("Registry with id '" + id + "' already exists!");
        }

        POST_AT.computeIfAbsent(key.getRegistryName(), (k) -> new LinkedList<>()).add(types);

        return types;
    }

    public static <K, V> GTRegistryInfo<K, V> add(ResourceLocation id, Supplier<Map<K, V>> registryValues, Class<?> baseClass) {
        var types = new GTRegistryInfo<>(id, registryValues, UtilsJS.cast(baseClass));

        if (MAP.put(id, types) != null || !EXTRA_IDS.add(id)) {
            throw new IllegalStateException("Registry with id '" + id + "' already exists!");
        }

        POST_AT.computeIfAbsent(id, (k) -> new LinkedList<>()).add(types);

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

        if (DevProperties.get().debugInfo) {
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
        GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, registryKey, new GTRegistryEventJS<>(this));
    }

    public static void registerFor(ResourceLocation registry) {
        for (var type : POST_AT.getOrDefault(registry, List.of())) {
            type.postEvent();

            for (var builder : type.objects.values()) {
                if (DevProperties.get().debugInfo) {
                    ConsoleJS.STARTUP.info("+ " + registry + " | " + builder.id);
                }
                builder.register();
            }
        }
    }
}
