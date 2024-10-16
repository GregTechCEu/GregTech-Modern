package com.gregtechceu.gtceu.integration.jsonthings.serializers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveFancyUIWorkableMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jsonthings.JsonThingsCompat;
import com.gregtechceu.gtceu.integration.jsonthings.builders.MachineBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.ModLoader;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MachineBuilderType<D extends MachineDefinition, M extends MetaMachine> {

    static {
        JsonThingsCompat.MACHINE_BUILDER_TYPES.unfreeze();
    }

    public static final MachineBuilderType<MachineDefinition, SimpleTieredMachine> PLAIN = register("plain",
            (data) -> new IMachineFactory<>() {

                @Override
                public MachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MachineDefinition.createDefinition(id);
                }

                @Override
                public SimpleTieredMachine create(IMachineBlockEntity holder, MachineDefinition definition) {
                    return new SimpleTieredMachine(holder, definition.getTier(), GTMachines.defaultTankSizeFunction);
                }
            });
    public static final MachineBuilderType<MachineDefinition, SimpleSteamMachine> STEAM = register("steam",
            (data) -> new IMachineFactory<>() {

                @Override
                public MachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MachineDefinition.createDefinition(id);
                }

                @Override
                public SimpleSteamMachine create(IMachineBlockEntity holder, MachineDefinition definition) {
                    boolean isHp = GsonHelper.getAsBoolean(data, "is_hp", false);
                    return new SimpleSteamMachine(holder, isHp);
                }
            });
    public static final MachineBuilderType<MachineDefinition, SimpleGeneratorMachine> GENERATOR = register("generator",
            (data) -> new IMachineFactory<>() {

                @Override
                public MachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MachineDefinition.createDefinition(id);
                }

                @Override
                public SimpleGeneratorMachine create(IMachineBlockEntity holder, MachineDefinition definition) {
                    float hazardStrength = GsonHelper.getAsFloat(data, "hazard_strength", 0.25f);
                    return new SimpleGeneratorMachine(holder, definition.getTier(), hazardStrength,
                            GTMachines.defaultTankSizeFunction);
                }
            });

    public static final MachineBuilderType<MultiblockMachineDefinition, WorkableElectricMultiblockMachine> MULTIBLOCK = register(
            "multiblock", (data) -> new IMachineFactory<>() {

                @Override
                public MultiblockMachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MultiblockMachineDefinition.createDefinition(id);
                }

                @Override
                public WorkableElectricMultiblockMachine create(IMachineBlockEntity holder,
                                                                MultiblockMachineDefinition definition) {
                    return new WorkableElectricMultiblockMachine(holder);
                }
            });

    public static final MachineBuilderType<MultiblockMachineDefinition, PrimitiveFancyUIWorkableMachine> PRIMITIVE = register(
            "primitive", (data) -> new IMachineFactory<>() {

                @Override
                public MultiblockMachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MultiblockMachineDefinition.createDefinition(id);
                }

                @Override
                public PrimitiveFancyUIWorkableMachine create(IMachineBlockEntity holder,
                                                              MultiblockMachineDefinition definition) {
                    return new PrimitiveFancyUIWorkableMachine(holder);
                }
            });

    public static final MachineBuilderType<MultiblockMachineDefinition, SteamParallelMultiblockMachine> STEAM_MULTI = register(
            "steam_multiblock", (data) -> new IMachineFactory<>() {

                @Override
                public MultiblockMachineDefinition construct(ResourceLocation id, MachineBuilder builder) {
                    return MultiblockMachineDefinition.createDefinition(id);
                }

                @Override
                public SteamParallelMultiblockMachine create(IMachineBlockEntity holder,
                                                             MultiblockMachineDefinition definition) {
                    int parallelAmount = GsonHelper.getAsInt(data, "parallel_amount",
                            ConfigHolder.INSTANCE.machines.steamMultiParallelAmount);
                    return new SteamParallelMultiblockMachine(holder, parallelAmount, new Object[0]);
                }
            });

    public static <D extends MachineDefinition,
            M extends MetaMachine> MachineBuilderType<D, M> register(String name, IMachineSerializer<D, M> factory) {
        return JsonThingsCompat.MACHINE_BUILDER_TYPES.register(name, new MachineBuilderType<>(factory));
    }

    private final IMachineSerializer<D, M> factory;

    public IMachineFactory<D, M> getFactory(JsonObject data) {
        return factory.createFactory(data);
    }

    public static void init() {
        // noinspection unchecked
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(JsonThingsCompat.MACHINE_BUILDER_TYPES,
                (Class<MachineBuilderType<?, ?>>) (Class<?>) MachineBuilderType.class));
        JsonThingsCompat.MACHINE_BUILDER_TYPES.freeze();
    }
}
