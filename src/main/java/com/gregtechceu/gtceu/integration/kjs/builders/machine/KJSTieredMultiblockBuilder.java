package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Accessors(fluent = true, chain = true)
public class KJSTieredMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition> {

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient MachineInstanceFactory.Tiered<? extends MultiblockControllerMachine> machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);

    public KJSTieredMultiblockBuilder(ResourceLocation id) {
        super(id);

        this.dummyBuilder = true;
    }

    public KJSTieredMultiblockBuilder(ResourceLocation id,
                                      MachineInstanceFactory.Tiered<MultiblockControllerMachine> machine) {
        this(id);
        this.machine = machine;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MultiblockMachineDefinition createObject() {
        // this method is never called on dummy builders (which this class is)
        return null;
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
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MultiblockMachineBuilder<?, ?> builder = GregTechKubeJSPlugin.KUBEJS_DUMMY_REGISTRATE
                    .multiblock(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.buildMachine(holder, tier));

            builder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);
            this.definition.apply(tier, builder);
            registry.add(GTRegistries.Keys.MACHINE, new MachineBuilderWrapper<>(builder));
        }
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MultiblockMachineBuilder<?, ?> builder);
    }
}
