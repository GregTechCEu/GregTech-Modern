package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@Accessors(fluent = true, chain = true)
public class KJSTieredMachineBuilder extends BuilderBase<MachineDefinition> {

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);
    @Setter
    public transient @Nullable Int2IntFunction tankScalingFunction = GTMachineUtils.defaultTankSizeFunction;
    @Setter
    public transient boolean addDefaultTooltips = true;
    @Setter
    public transient boolean addDefaultModel = true;
    @Setter
    public transient boolean isGenerator = false;

    public KJSTieredMachineBuilder(ResourceLocation id) {
        super(id);
        this.addDefaultTooltips = false;
        this.addDefaultModel = false;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine,
                                   boolean isGenerator) {
        super(id);
        this.machine = machine;
        this.isGenerator = isGenerator;
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
            final Int2IntFunction tankFunction = Objects.requireNonNullElse(tankScalingFunction,
                    GTMachineUtils.defaultTankSizeFunction);

            var childId = this.id.withPath("%s_%s".formatted(tierName, this.id.getPath()));

            MachineBuilder<MachineDefinition, ?, ?> internalBuilder = GTRegistrate
                    .createIgnoringListenerErrors(childId.getNamespace())
                    .machine(childId.getPath(),
                            holder -> machine.create(holder, tier, tankFunction));

            var builder = new MachineBuilderJS<>(childId, internalBuilder);

            builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(this.id.getPath()), VLVT[tier]))
                    .tier(tier);

            if (this.addDefaultModel) {
                builder.workableTieredHullModel(id.withPrefix("block/machines/"));
            }

            this.definition.apply(tier, builder);

            if (internalBuilder.recipeTypes().length != 0) {
                if (tankScalingFunction != null && addDefaultTooltips) {
                    internalBuilder.tooltips(
                            GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                                    Arrays.stream(internalBuilder.recipeTypes()).findFirst().orElseThrow(),
                                    tankScalingFunction.applyAsInt(tier), !isGenerator));
                }
            }
            if (internalBuilder.ui() == null) {
                internalBuilder.ui(GTSingleblockMachinePanels.GENERAL_MACHINE);
            }

            GTRegistryInfo.MACHINE.addBuilder(builder);
        }
    }

    @Override
    public @Nullable MachineDefinition register() {
        return null;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScaling);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilderJS<?> builder);
    }
}
