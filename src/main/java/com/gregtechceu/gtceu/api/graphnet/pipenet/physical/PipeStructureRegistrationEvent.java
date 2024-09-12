package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public final class PipeStructureRegistrationEvent extends Event implements IModBusEvent {

    private final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> registry = new Object2ObjectLinkedOpenHashMap<>();

    public <T extends IPipeStructure> void register(@NotNull T structure) {
        //noinspection unchecked
        Set<T> structures = (Set<T>) registry.computeIfAbsent(structure.getClass(),
                k -> new ObjectLinkedOpenHashSet<>());
        structures.add(structure);
    }

    Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> getRegistry() {
        return registry;
    }
}
