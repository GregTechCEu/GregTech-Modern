package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import com.gregtechceu.gtceu.utils.GTUtil;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraftforge.common.MinecraftForge;
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
        MinecraftForge.EVENT_BUS.post(event);
        return event.getRegistry();
    }
}
