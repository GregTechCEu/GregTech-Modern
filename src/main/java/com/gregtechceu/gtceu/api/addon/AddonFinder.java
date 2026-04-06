package com.gregtechceu.gtceu.api.addon;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.util.*;

public final class AddonFinder {

    private static final Logger LOGGER = LogManager.getLogger("GTCEu Addon Finder");
    private static final List<IGTAddon> cache = new ArrayList<>();
    private static Map<String, IGTAddon> modIdMap = null;

    @UnmodifiableView
    public static List<IGTAddon> getAddonList() {
        ensureInitialized();
        return Collections.unmodifiableList(cache);
    }

    @UnmodifiableView
    public static Map<String, IGTAddon> getAddons() {
        ensureInitialized();
        return Collections.unmodifiableMap(modIdMap);
    }

    @Nullable
    public static IGTAddon getAddon(String modId) {
        return modIdMap.get(modId);
    }

    private static void ensureInitialized() {
        if (modIdMap == null) {
            modIdMap = getInstances();
            cache.addAll(modIdMap.values());
        }
    }

    private static Map<String, IGTAddon> getInstances() {
        List<IModInfo> allMods = ModList.get().getMods();
        Map<String, String> addonClassNames = new LinkedHashMap<>();
        for (IModInfo modInfo : allMods) {
            ModFileScanData scanData = modInfo.getOwningFile().getFile().getScanResult();
            scanData.getAnnotatedBy(GTAddon.class, ElementType.TYPE)
                    .filter(data -> Objects.equals(data.annotationData().get("value"), modInfo.getModId()))
                    .map(ModFileScanData.AnnotationData::memberName)
                    .forEach(className -> addonClassNames.put(modInfo.getModId(), className));
        }
        Map<String, IGTAddon> instances = new LinkedHashMap<>();
        for (var entry : addonClassNames.entrySet()) {
            String modId = entry.getKey();
            String className = entry.getValue();
            try {
                Class<?> asmClass = Class.forName(className);
                Class<? extends IGTAddon> asmInstanceClass = asmClass.asSubclass(IGTAddon.class);
                try {
                    Constructor<? extends IGTAddon> constructor = asmInstanceClass.getDeclaredConstructor();
                    IGTAddon instance = constructor.newInstance();
                    instances.put(modId, instance);
                } catch (ReflectiveOperationException e) {
                    LOGGER.error(
                            "GT addon class {} for addon {} must have a public constructor with no arguments, found {}",
                            className, modId, Arrays.toString(asmInstanceClass.getConstructors()));
                }
            } catch (ClassCastException e) {
                LOGGER.error("Failed to load: {} from {}, does not extend IGTAddon!", className, modId, e);
            } catch (ClassNotFoundException | LinkageError e) {
                LOGGER.error("Failed to load: {} from {}", className, modId, e);
            }
        }
        return instances;
    }
}
