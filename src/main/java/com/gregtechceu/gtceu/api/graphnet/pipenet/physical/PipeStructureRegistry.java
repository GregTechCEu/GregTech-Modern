package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import com.gregtechceu.gtceu.utils.GTUtil;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class PipeStructureRegistry {

    private static final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();

    public static <T extends IPipeStructure> void register(@NotNull T structure) {
        Set<T> structures = (Set<T>) REGISTRY.computeIfAbsent(structure.getClass(),
                k -> new ObjectLinkedOpenHashSet<>());
        structures.add(structure);
    }

    /**
     * Do not modify the returned set.
     */
    public static <T extends IPipeStructure> @NotNull Set<T> getStructures(Class<T> structureClass) {
        GTUtil.forceInitialization(structureClass);
        return (Set<T>) REGISTRY.getOrDefault(structureClass, Collections.emptySet());
    }
}
