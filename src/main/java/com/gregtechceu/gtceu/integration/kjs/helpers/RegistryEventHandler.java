package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTRegistryKubeEvent;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;

@SuppressWarnings({ "rawtypes", "unchecked" })
@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class RegistryEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerAll(RegisterEvent event) {
        // only post the GT registry event for GT registries
        if (!GTRegistries.getRegistries().contains(event.getRegistry())) {
            return;
        }

        handleRegistryEvent((ResourceKey) event.getRegistryKey(), event);
    }

    private static <T> void handleRegistryEvent(ResourceKey<Registry<T>> registryKey, RegisterEvent event) {
        GTCEuStartupEvents.REGISTRY.post(ScriptType.STARTUP, (ResourceKey) registryKey,
                new GTRegistryKubeEvent<>(registryKey));

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
            if (!builder.dummyBuilder) {
                event.register(registryKey, builder.id, builder::createTransformedObject);

                if (DevProperties.get().logRegistryEventObjects) {
                    ConsoleJS.STARTUP.info("+ " + registryKey.location() + " | " + builder.id);
                }

                added++;
            }
        }

        if (!objStorage.objects.isEmpty() && DevProperties.get().logRegistryEventObjects) {
            KubeJS.LOGGER.info("Registered {}/{} objects of {}", added, objStorage.objects.size(),
                    registryKey.location());
        }
    }
}
