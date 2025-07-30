package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@Accessors(fluent = true, chain = true)
public class KJSTieredMachineBuilder extends BuilderBase<MachineDefinition[]> {

    private final MachineBuilder<?>[] builders = new MachineBuilder[TIER_COUNT];

    @Setter
    public volatile int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public volatile TieredCreationFunction machine;
    @Setter
    public volatile DefinitionFunction definition = (tier, def) -> def.tier(tier);
    @Setter
    public volatile Int2IntFunction tankScalingFunction = GTMachineUtils.defaultTankSizeFunction;
    @Setter
    public volatile boolean addDefaultTooltips = true;
    @Setter
    public volatile boolean addDefaultModel = true;
    @Setter
    public volatile boolean isGenerator = false;

    public volatile BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> editableUI;

    public KJSTieredMachineBuilder(ResourceLocation id) {
        super(id);
        this.addDefaultTooltips = false;
        this.addDefaultModel = false;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine,
                                   BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> editableUI,
                                   boolean isGenerator) {
        super(id);
        this.machine = machine;
        this.editableUI = editableUI;
        this.isGenerator = isGenerator;
    }

    @Override
    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {
        super.generateAssetJsons(generator);
        for (int tier : this.tiers) {
            MachineBuilder<?> builder = this.builders[tier];
            if (builder != null) {
                builder.generateAssetJsons(generator);
            }
        }
    }

    @Override
    public void generateLang(@NotNull LangEventJS lang) {
        super.generateLang(lang);
        for (int tier : this.tiers) {
            MachineBuilder<?> builder = this.builders[tier];
            if (builder != null) {
                builder.generateLang(lang);
            }
        }
    }

    @Override
    public @Nullable MachineDefinition @NotNull [] register() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");
        MachineDefinition[] definitions = new MachineDefinition[TIER_COUNT];
        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MachineBuilder<?> builder = GTRegistration.REGISTRATE.machine(
                    String.format("%s_%s", tierName, this.id.getPath()),
                    holder -> machine.create(holder, tier, tankScalingFunction));

            builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(this.id.getPath()), VLVT[tier]))
                    .tier(tier);
            if (this.addDefaultModel) {
                builder.workableTieredHullModel(id.withPrefix("block/machines/"));
            }
            this.definition.apply(tier, builder);

            if (builder.recipeTypes().length > 0) {
                GTRecipeType recipeType = builder.recipeTypes()[0];
                if (this.editableUI != null && builder.editableUI() == null) {
                    builder.editableUI(this.editableUI.apply(this.id, recipeType));
                }
                if (tankScalingFunction != null && addDefaultTooltips) {
                    builder.tooltips(
                            GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.applyAsInt(tier), !isGenerator));
                }
            }

            this.builders[tier] = builder;
            definitions[tier] = builder.register();
        }
        return value = definitions;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MetaMachine create(IMachineBlockEntity holder, int tier, Int2IntFunction tankScaling);
    }

    @FunctionalInterface
    public interface CreationFunction<T extends MetaMachine> {

        T create(IMachineBlockEntity holder);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?> builder);
    }
}
