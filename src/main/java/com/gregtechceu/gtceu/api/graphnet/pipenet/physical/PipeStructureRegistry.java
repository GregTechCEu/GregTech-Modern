package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import com.gregtechceu.gtceu.utils.GTUtil;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class PipeStructureRegistry {

    private static final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();

    public static <T extends IPipeStructure> void register(@NotNull T structure) {
        Set<T> structures = (Set<T>) REGISTRY.get(structure.getClass());
        if (structures == null) {
            structures = new LinkedHashSet<>();
            REGISTRY.put(structure.getClass(), structures);
        }
        structures.add(structure);
    }

    /**
     * Do not modify the returned set.
     */
    public static <T extends IPipeStructure> @NotNull Set<T> getStructures(Class<T> structureClass) {
        GTUtil.forceInitialization(structureClass);
        Set<T> structures = (Set<T>) REGISTRY.get(structureClass);
        if (structures == null) return Collections.emptySet();
        return structures;
    }
}
