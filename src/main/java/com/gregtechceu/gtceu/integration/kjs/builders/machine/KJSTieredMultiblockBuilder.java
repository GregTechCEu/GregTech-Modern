package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Accessors(fluent = true, chain = true)
public class KJSTieredMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition> {

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);

    public KJSTieredMultiblockBuilder(ResourceLocation id) {
        super(id);
        this.dummyBuilder = true;
    }

    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");

        for (final int tier : tiers) {
            String tierName = VNL[tier];

            var childId = this.id.withPath(String.format("%s_%s", tierName, this.id.getPath()));

            MultiblockMachineBuilder<?> internalBuilder = GTRegistrate
                    .createIgnoringListenerErrors(childId.getNamespace())
                    .multiblock(childId.getPath(),
                            holder -> machine.create(holder, tier));

            var builder = new MultiblockMachineBuilderJS(childId, internalBuilder);

            internalBuilder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);

            this.definition.apply(tier, builder);
            registry.add(GTRegistries.Keys.MACHINE, builder);

        }
    }

    @Override
    public @Nullable MultiblockMachineDefinition createObject() {
        return null;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MultiblockControllerMachine create(BlockEntityCreationInfo info, int tier);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MultiblockMachineBuilderJS builder);
    }
}
