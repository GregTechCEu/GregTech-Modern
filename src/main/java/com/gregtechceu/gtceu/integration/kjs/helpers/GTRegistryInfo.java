package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.mixins.kjs.BuilderBaseAccessor;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryEventJS;

import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.RegistryCallback;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;

public class GTRegistryInfo {

    public static final RegistryInfo<Element> ELEMENT = RegistryInfo.of(GTRegistries.Keys.ELEMENT, Element.class);
    public static final RegistryInfo<MaterialIconSet> MATERIAL_ICON_SET = RegistryInfo
            .of(GTRegistries.Keys.MATERIAL_ICON_SET, MaterialIconSet.class);
    public static final RegistryInfo<Material> MATERIAL = RegistryInfo.of(GTRegistries.Keys.MATERIAL, Material.class);
    public static final RegistryInfo<GTRecipeType> RECIPE_TYPE = RegistryInfo.of(GTRegistries.Keys.RECIPE_TYPE,
            GTRecipeType.class);
    public static final RegistryInfo<GTRecipeCategory> RECIPE_CATEGORY = RegistryInfo
            .of(GTRegistries.Keys.RECIPE_CATEGORY, GTRecipeCategory.class);

    public static final RegistryInfo<IWorldGenLayer> WORLD_GEN_LAYER = RegistryInfo
            .of(GTRegistries.Keys.WORLD_GEN_LAYER, IWorldGenLayer.class);
    public static final RegistryInfo<TagPrefix> TAG_PREFIX = RegistryInfo.of(GTRegistries.Keys.TAG_PREFIX,
            TagPrefix.class);
    public static final RegistryInfo<DimensionMarker> DIMENSION_MARKER = RegistryInfo
            .of(GTRegistries.Keys.DIMENSION_MARKER, DimensionMarker.class);
    public static final RegistryInfo<MachineDefinition> MACHINE = RegistryInfo.of(GTRegistries.Keys.MACHINE,
            MachineDefinition.class);

    @SuppressWarnings("unchecked")
    public static <T> int registerObjects(RegistryInfo<T> registryInfo, RegistryCallback<T> function) {
        GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, registryInfo.key, new GTRegistryEventJS<>(registryInfo));

        if (DevProperties.get().debugInfo) {
            if (registryInfo.objects.isEmpty()) {
                KubeJS.LOGGER.info("Skipping {} registry", registryInfo);
            } else {
                KubeJS.LOGGER.info("Building {} objects of {} registry", registryInfo.objects.size(), registryInfo);
            }
        }

        if (registryInfo.objects.isEmpty()) {
            return 0;
        }

        int added = 0;

        for (var builder : registryInfo) {
            BuilderBaseAccessor<T> accessor = (BuilderBaseAccessor<T>) builder;
            if (builder.dummyBuilder) {
                // don't actually register anything here, the wrapper builders register themselves with Registrate
                accessor.gtceu$createTransformedObject();
            } else if (builder.getRegistryType().bypassServerOnly || !CommonProperties.get().serverOnly) {
                function.accept(builder.id, accessor::gtceu$createTransformedObject);

                if (DevProperties.get().debugInfo) {
                    ConsoleJS.STARTUP.info("+ " + registryInfo + " | " + builder.id);
                }

                added++;
            }
        }

        if (!registryInfo.objects.isEmpty() && DevProperties.get().debugInfo) {
            KubeJS.LOGGER.info("Registered {}/{} objects of {}", added, registryInfo.objects.size(), registryInfo);
        }

        return added;
    }
}
