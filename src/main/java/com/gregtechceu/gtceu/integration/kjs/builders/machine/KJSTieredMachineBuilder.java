package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
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

        this.dummyBuilder = true;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine,
                                   boolean isGenerator) {
        super(id);
        this.machine = machine;
        this.isGenerator = isGenerator;

        this.dummyBuilder = true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MachineDefinition createObject() {
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
            final Int2IntFunction tankFunction = Objects.requireNonNullElse(tankScalingFunction,
                    GTMachineUtils.defaultTankSizeFunction);

            var builder = GTRegistrate.create(this.id.getNamespace(), false)
                    .machine(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.create(holder, tier, tankFunction));

            builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(this.id.getPath()), VLVT[tier]))
                    .tier(tier);
            if (this.addDefaultModel) {
                builder.workableTieredHullModel(id.withPrefix("block/machines/"));
            }
            this.definition.apply(tier, builder);

            if (builder.recipeTypes().length > 0) {
                GTRecipeType recipeType = builder.recipeTypes()[0];
                if (tankScalingFunction != null && addDefaultTooltips) {
                    builder.tooltips(
                            GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.applyAsInt(tier), !isGenerator));
                }
            }

            registry.add(GTRegistries.Keys.MACHINE, new MachineBuilderWrapper<>(builder));
        }
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScaling);
    }

    @FunctionalInterface
    public interface CreationFunction<T extends MetaMachine> {

        T create(BlockEntityCreationInfo info);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?, ?, ?> builder);
    }
}
