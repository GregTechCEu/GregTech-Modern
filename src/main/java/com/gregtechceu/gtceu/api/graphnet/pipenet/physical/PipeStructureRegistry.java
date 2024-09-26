package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import net.minecraftforge.fml.ModLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class PipeStructureRegistry {

    private static final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> REGISTRY = fireEvent();

    public static <T extends IPipeStructure> @NotNull @UnmodifiableView Set<T> getStructures(Class<T> structureClass) {
        return (Set<T>) REGISTRY.getOrDefault(structureClass, Collections.emptySet());
    }

    private static Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> fireEvent() {
        PipeStructureRegistrationEvent event = new PipeStructureRegistrationEvent();
        ModLoader.get().postEvent(event);
        return event.getRegistry();
    }
}
