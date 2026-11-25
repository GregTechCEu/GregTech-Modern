package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
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
    }

    @Override
    public void registerAdditional() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");

        for (final int tier : tiers) {
            String tierName = VNL[tier];

            var childId = this.id.withPath(String.format("%s_%s", tierName, this.id.getPath()));

            MultiblockMachineBuilder<?, ?, ?> internalBuilder = GTRegistrate
                    .createIgnoringListenerErrors(childId.getNamespace())
                    .multiblock(childId.getPath(),
                            holder -> machine.create(holder, tier));

            var builder = new MultiblockMachineBuilderJS(childId, internalBuilder);

            internalBuilder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);

            this.definition.apply(tier, builder);
            GTRegistryInfo.MACHINE.addBuilder(builder);

        }
    }

    @Override
    public @Nullable MultiblockMachineDefinition register() {
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
