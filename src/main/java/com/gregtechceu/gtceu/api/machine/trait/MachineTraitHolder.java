package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MachineTraitHolder {

    private final MetaMachine machine;
    private final List<MachineTrait> traits;
    private final Map<MachineTraitType<?>, List<MachineTrait>> traitsByType;

    private final Map<String, MachineTrait> traitsToSave;

    public MachineTraitHolder(MetaMachine machine) {
        this.machine = machine;
        this.traits = new ObjectArrayList<>();
        this.traitsByType = new Object2ObjectOpenHashMap<>();
        this.traitsToSave = new Object2ObjectOpenHashMap<>();
    }

    public @UnmodifiableView List<MachineTrait> getAllTraits() {
        return traits;
    }

    public <T extends MachineTrait> T attachTrait(T trait) {
        var traitType = trait.getTraitType();

        var list = traitsByType.computeIfAbsent(traitType, $ -> new ObjectArrayList<>(1));
        if (!traitType.allowsMultipleInstances() && !list.isEmpty()) {
            throw new IllegalArgumentException("Attempted to add multiple traits of type: " + trait.getClass());
        }

        list.add(trait);
        traits.add(trait);

        trait.setMachine(machine);
        return trait;
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.
     *
     * @param traitName Unique identifier for this trait.
     * @param trait     The trait to register
     */
    public MachineTraitHolder registerPersistentTrait(String traitName, MachineTrait trait) {
        if (trait.getMachine() != machine) throw new IllegalArgumentException("Trait does not belong to this machine.");
        if (traitsToSave.containsKey(traitName))
            throw new IllegalArgumentException("Attempted to register duplicate trait save key \"" + traitName + "\"");
        traitsToSave.put(traitName, trait);
        return this;
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends MachineTrait> T getPersistentTrait(String traitName) {
        MachineTrait trait = traitsToSave.get(traitName);
        return trait == null ? null : (T) trait;
    }

    /**
     * Gets the first trait with the specified type.
     */
    public <T extends MachineTrait> @Nullable T getTrait(MachineTraitType<T> type) {
        List<MachineTrait> traitList = traitsByType.get(type);
        if (traitList == null || traitList.isEmpty()) return null;
        return type.castTrait(traitList.get(0));
    }

    public <T extends MachineTrait> Optional<T> getTraitOptional(MachineTraitType<T> type) {
        return Optional.ofNullable(getTrait(type));
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

    private static class MachineTraitHolderTransformer implements ValueTransformer<MachineTraitHolder> {

        @Override
        public Tag serializeNBT(MachineTraitHolder value, TransformerContext<MachineTraitHolder> context) {
            CompoundTag tag = new CompoundTag();

            value.traitsToSave.forEach((k, v) -> tag.put(k,
                    v.getSyncDataHolder().serializeNBT(context.isClientSync(), context.isClientFullSyncUpdate())));

            return tag;
        }

        @Override
        public @Nullable MachineTraitHolder deserializeNBT(Tag tag, TransformerContext<MachineTraitHolder> context) {
            var traitHolder = Objects.requireNonNull(context.currentValue());
            var compoundTag = (CompoundTag) tag;

            for (var key : compoundTag.getAllKeys()) {
                var trait = traitHolder.getPersistentTrait(key);
                if (trait == null) {
                    GTCEu.LOGGER.warn("Attempted to deserialise syncable trait '{}', but no syncable trait has that ID",
                            key);
                    continue;
                }
                trait.getSyncDataHolder().deserializeNBT(compoundTag.getCompound(key), context.isClientSync());
            }

            return null;
        }
    }

    static {
        ValueTransformers.registerTransformer(MachineTraitHolder.class, new MachineTraitHolderTransformer());
    }
}
