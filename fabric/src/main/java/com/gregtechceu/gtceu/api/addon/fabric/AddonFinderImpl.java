package com.gregtechceu.gtceu.api.addon.fabric;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;
import java.util.stream.Collectors;

public class AddonFinderImpl extends AddonFinder {
    private AddonFinderImpl() {

    }

    public static List<IGTAddon> getAddons() {
        if (cache == null) {
            cache = getInstances("gtceu_addon", IGTAddon.class);
        }

        return cache;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> getInstances(String entrypointContainerKey, Class<T> instanceClass) {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        List<EntrypointContainer<T>> pluginContainers = fabricLoader.getEntrypointContainers(entrypointContainerKey, instanceClass);
        return pluginContainers.stream()
                .map(EntrypointContainer::getEntrypoint)
                .collect(Collectors.toList());
    }
}
