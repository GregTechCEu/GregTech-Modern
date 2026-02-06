package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MachineTraitHolder {

    private final MetaMachine machine;
    private final List<MachineTrait> traits;
    private final Map<MachineTraitType<?>, List<MachineTrait>> traitsByType;

    public MachineTraitHolder(MetaMachine machine) {
        this.machine = machine;
        this.traits = new ObjectArrayList<>();
        this.traitsByType = new Object2ObjectOpenHashMap<>();
    }

    public @UnmodifiableView List<MachineTrait> getAllTraits() {
        return traits;
    }

    public void attachTrait(MachineTrait trait) {
        var traitType = trait.getTraitType();

        var list = traitsByType.computeIfAbsent(traitType, $ -> new ObjectArrayList<>(1));
        if (!traitType.allowsMultipleInstances() && !list.isEmpty()) {
            throw new IllegalArgumentException("Attempted to add multiple traits of type: " + trait.getClass());
        }

        list.add(trait);
        traits.add(trait);
    }

    /**
     * Gets the first trait with the specified type.
     */
    public <T extends MachineTrait> @Nullable T getTrait(MachineTraitType<T> type) {
        List<MachineTrait> traitList = traitsByType.get(type);
        if (traitList == null || traitList.isEmpty()) return null;
        return type.castTrait(traitList.get(0));
    }

    /**
     * Get all traits with the specified type.
     */
    @SuppressWarnings("unchecked")
    public <T extends MachineTrait> @UnmodifiableView List<T> getTraits(MachineTraitType<T> type) {
        List<T> traitList = (List<T>) traitsByType.get(type);
        if (traitList == null) return List.of();
        return Collections.unmodifiableList(traitList);
    }
}
