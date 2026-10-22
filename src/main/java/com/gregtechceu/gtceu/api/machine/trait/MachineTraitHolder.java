package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class MachineTraitHolder {

    private final MetaMachine machine;

    private boolean allowTraitAttachment = true;

    private final List<MachineTrait> traits;

    private final Map<String, MachineTrait> traitsToSave;

    private @Nullable List<MachineTrait> allTraits = null;

    private final Map<Class<?>, List<MachineTrait>> traitsByClass;

    public MachineTraitHolder(MetaMachine machine) {
        this.machine = machine;
        this.traits = new ObjectArrayList<>();
        this.traitsByClass = new Object2ObjectOpenHashMap<>();
        this.traitsToSave = new Object2ObjectOpenHashMap<>();
    }

    /**
     * @return An unmodifiable list of all traits attached to this machine.
     */
    public @Unmodifiable List<MachineTrait> getAllTraits() {
        return allTraits != null ? allTraits : Collections.unmodifiableList(traits);
    }

    /**
     * Gets all traits implementing a specific interface/class
     */
    @SuppressWarnings("unchecked")
    public <T> @Unmodifiable List<T> getTraitsByInterface(Class<T> clazz) {
        if (traitsByClass.containsKey(clazz)) return (List<T>) traitsByClass.get(clazz);

        List<T> list = new ObjectArrayList<>();

        for (var t : getAllTraits()) {
            if (clazz.isAssignableFrom(t.getClass())) list.add(clazz.cast(t));
        }

        if (!allowTraitAttachment) traitsByClass.put(clazz, (List<MachineTrait>) Collections.unmodifiableList(list));
        return list;
    }

    /**
     * Attaches a trait to this machine, with the default trait callback priority of 1.
     * 
     * @param trait The trait to attach
     * @return The attached trait
     */
    public <T extends MachineTrait> T attachTrait(T trait) {
        return attachTrait(trait, 1);
    }

    /**
     * Attaches a trait to this machine. Traits must be attached before {@link MetaMachine#onLoad()}.
     * 
     * @param trait            The trait to attach
     * @param callbackPriority The trait's callback priority. Traits with a higher priority will have their events fired
     *                         first, which may prevent traits with a lower priority from handling some events.
     * @return The attached trait
     */
    public <T extends MachineTrait> T attachTrait(T trait, int callbackPriority) {
        if (!allowTraitAttachment)
            throw new IllegalStateException("Cannot add traits to machine after machine has been loaded.");

        trait.setTraitPriority(callbackPriority);

        addTraitToClassMap(trait.getClass(), trait);
        traits.add(trait);
        traits.sort(Comparator.comparingInt(MachineTrait::getTraitPriority).reversed());

        trait.setMachine(machine);
        trait.onTraitAttached();
        return trait;
    }

    @SuppressWarnings("unchecked")
    private void addTraitToClassMap(Class<? extends MachineTrait> clazz, MachineTrait trait) {
        var list = traitsByClass.computeIfAbsent(clazz, $ -> new ObjectArrayList<>(1));

        list.add(trait);
        list.sort(Comparator.comparingInt(MachineTrait::getTraitPriority).reversed());

        if (!clazz.getSuperclass().equals(MachineTrait.class))
            addTraitToClassMap((Class<? extends MachineTrait>) clazz.getSuperclass(), trait);
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.<br>
     * Note: Persistent traits must be attached before data load, or they will not be loaded correctly.
     *
     * @param traitName Unique identifier for this trait.
     * @param trait     The trait to register
     */
    public void attachPersistentTrait(String traitName, MachineTrait trait) {
        attachTrait(trait);
        registerPersistentTrait(traitName, trait);
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.<br>
     * Note: Persistent traits must be attached before data load, or they will not be loaded correctly.
     *
     * @param traitName        Unique identifier for this trait.
     * @param callbackPriority The trait's callback priority. Traits with a higher priority will have their events fired
     *                         first, which may prevent traits with a lower priority from handling some events.
     * @param trait            The trait to register
     */
    public void attachPersistentTrait(String traitName, MachineTrait trait, int callbackPriority) {
        attachTrait(trait, callbackPriority);
        registerPersistentTrait(traitName, trait);
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
        trait.setTraitName(traitName);
        traitsToSave.put(traitName, trait);
        return this;
    }

    /**
     * Gets a trait registered by {@code registerPersistentTrait}
     * 
     * @param traitName the unique identifier for the trait
     * @return the trait, or null if not present
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T extends MachineTrait> T getPersistentTrait(String traitName) {
        MachineTrait trait = traitsToSave.get(traitName);
        return trait == null ? null : (T) trait;
    }

    /**
     * Gets the first trait (trait with highest priority) with the specified class.
     * Also includes traits that are a subtype of the specified class.
     *
     * @param type The trait class to get
     * @return The trait, or null if no traits of the given type are present.
     */
    public <T extends MachineTrait> @Nullable T getTrait(Class<T> type) {
        List<MachineTrait> traitList = traitsByClass.get(type);
        if (traitList == null || traitList.isEmpty()) return null;
        return type.cast(traitList.get(0));
    }

    /**
     * Gets the first trait (trait with highest priority) with the specified class.
     * Also includes traits that are a subtype of the specified class.
     *
     * @param type The trait class to get
     * @return An optional result containing the trait if present.
     */
    public <T extends MachineTrait> Optional<T> getTraitOptional(Class<T> type) {
        return Optional.ofNullable(getTrait(type));
    }

    /**
     * Get all traits with the specified class.
     * Also includes traits that are a subtype of the specified class.
     * 
     * @return An unmodifiable list containing all traits of the specified class.
     */
    @SuppressWarnings("unchecked")
    public <T extends MachineTrait> @UnmodifiableView List<T> getTraits(Class<T> type) {
        List<T> traitList = (List<T>) traitsByClass.get(type);
        if (traitList == null) return List.of();
        return Collections.unmodifiableList(traitList);
    }

    public void machineLoaded() {
        allowTraitAttachment = false;

        // Cache traits so that repeated unmodifiableList calls aren't required.

        allTraits = Collections.unmodifiableList(traits);
    }

    private static class MachineTraitHolderTransformer implements ValueTransformer<MachineTraitHolder> {

        @Override
        public Tag serializeNBT(MachineTraitHolder value, TransformerContext<MachineTraitHolder> context) {
            CompoundTag tag = new CompoundTag();

            value.traitsToSave.forEach((k, v) -> tag.put(k, v.getSyncDataHolder().serializeNBT(context.lookup())));

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

                trait.getSyncDataHolder().deserializeNBT(context.lookup(), compoundTag.getCompound(key));
            }

            return traitHolder;
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, MachineTraitHolder value,
                                  TransformerContext<MachineTraitHolder> context) {
            buf.writeVarInt(value.traitsToSave.size());
            value.traitsToSave.forEach((name, trait) -> {
                buf.writeUtf(name);
                if (context.isClientFullSyncUpdate()) trait.getSyncDataHolder().resyncAllFields();
                trait.getSyncDataHolder().writeClientPacket(context.lookup(), buf);
            });
        }

        @Override
        public @Nullable MachineTraitHolder readFromPacket(FriendlyByteBuf buf,
                                                           TransformerContext<MachineTraitHolder> context) {
            var traitHolder = Objects.requireNonNull(context.currentValue());
            int length = buf.readVarInt();
            for (int i = 0; i < length; i++) {
                String name = buf.readUtf();

                var trait = traitHolder.getPersistentTrait(name);
                if (trait == null) {
                    throw new IllegalStateException(
                            "Reading packet data for syncable trait '%s', but no client-side syncable trait has that ID"
                                    .formatted(name));
                } else {
                    trait.getSyncDataHolder().readClientPacket(context.lookup(), buf);
                }
            }

            return traitHolder;
        }
    }

    static {
        ValueTransformers.registerTransformer(MachineTraitHolder.class, new MachineTraitHolderTransformer());
    }
}
