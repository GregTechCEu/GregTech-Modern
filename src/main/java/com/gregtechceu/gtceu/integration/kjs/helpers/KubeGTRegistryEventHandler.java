package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryKubeEvent;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.plugin.builtin.event.StartupEvents;
import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * This is a copy of KubeJS's {@link dev.latvian.mods.kubejs.registry.RegistryEventHandler} with minor modifications,
 * licensed LGPL 3. <a
 * href=
 * "https://github.com/KubeJS-Mods/KubeJS/blob/4829f956837d50d44dc20e06d06c01028373e57c/src/main/java/dev/latvian/mods/kubejs/registry/RegistryEventHandler.java">Source</a>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class KubeGTRegistryEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerAll(RegisterEvent event) {
        // only post the GT registry event for GT registries
        if (event.getRegistryKey().equals(Registries.RECIPE_TYPE) ||
                !GTRegistries.getRegistryOrder().contains(event.getRegistryKey().location())) {
            return;
        }

        handleRegistryEvent((ResourceKey) event.getRegistryKey(), event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void postRecipeTypeEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, (ResourceKey) Registries.RECIPE_TYPE,
                    new GTRegistryKubeEvent<>(Registries.RECIPE_TYPE));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipeTypes(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            return;
        }

        for (var builder : RegistryObjectStorage.of(Registries.RECIPE_TYPE)) {
            addNamespace(builder.id.getNamespace());
        }

        var registryKey = GTRegistries.Keys.RECIPE_TYPE;
        GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, (ResourceKey) registryKey,
                new GTRegistryKubeEvent<>(registryKey));
        StartupEvents.REGISTRY.post(ScriptType.STARTUP, (ResourceKey) registryKey,
                new RegistryKubeEvent<>(registryKey));
        registerObjects(registryKey, event);
    }

    private static <T> void handleRegistryEvent(ResourceKey<Registry<T>> registryKey, RegisterEvent event) {
        GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, (ResourceKey) registryKey,
                new GTRegistryKubeEvent<>(registryKey));

        registerObjects(registryKey, event);
    }

    private static <T> void registerObjects(ResourceKey<Registry<T>> registryKey, RegisterEvent event) {
        var objStorage = RegistryObjectStorage.of(registryKey);

        if (objStorage.objects.isEmpty()) {
            if (DevProperties.get().logRegistryEventObjects) {
                GTCEu.LOGGER.info("Skipping {} registry - no objects to build", registryKey.location());
            }

            return;
        }

        if (DevProperties.get().logRegistryEventObjects) {
            GTCEu.LOGGER.info("Building {} objects of {} registry", objStorage.objects.size(), registryKey.location());
        }

        int added = 0;

        for (var builder : objStorage) {
            if (builder.dummyBuilder) {
                // don't actually register anything here, the wrapper builders register themselves with Registrate
                builder.createTransformedObject();
            } else {
                event.register((ResourceKey) event.getRegistryKey(), builder.id, builder::createTransformedObject);
            }

            if (DevProperties.get().logRegistryEventObjects) {
                ConsoleJS.STARTUP.info("+ " + registryKey.location() + " | " + builder.id);
            }
            added++;

            // add all registry objects' namespaces to the dynamic packs so their resources are listed as expected.
            // although usually only one namespace is used, it's easier and faster to
            // just always add them to the set than to check if they're already added.
            addNamespace(builder.id.getNamespace());
        }

        if (!objStorage.objects.isEmpty() && DevProperties.get().logRegistryEventObjects) {
            KubeJS.LOGGER.info("Registered {}/{} objects of {}", added, objStorage.objects.size(),
                    registryKey.location());
        }
    }

    private static void addNamespace(String namespace) {
        if (GTCEu.isClientSide()) GTDynamicResourcePack.addNamespace(namespace);
        GTDynamicDataPack.addNamespace(namespace);
    }
}
