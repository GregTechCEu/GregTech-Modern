package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.TIER_COUNT;
import static com.gregtechceu.gtceu.api.GTValues.VN;

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

    public KJSTieredMultiblockBuilder(ResourceLocation id, TieredCreationFunction machine) {
        this(id);
        this.machine = machine;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MultiblockMachineDefinition createObject() {
        // this method is never called on dummy builders (which this class is)
        return null;
    }

    @Override
    public void createAdditionalObjects() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");

        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MultiblockMachineBuilder<?, ?> builder = GTRegistrate
                    .createIgnoringListenerErrors(this.id.getNamespace())
                    .multiblock(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.create(holder, tier));

            builder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);
            this.definition.apply(tier, builder);
            GTRegistryInfo.MACHINE.addBuilder(new MachineBuilderWrapper<>(builder));
        }
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MultiblockControllerMachine create(BlockEntityCreationInfo info, int tier);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MultiblockMachineBuilder<?, ?> builder);
    }
}
