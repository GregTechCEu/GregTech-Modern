package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.machine.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
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
public class KJSTieredMachineBuilder extends BuilderBase<@Nullable MachineDefinition @NotNull []>
                                     implements IMachineBuilderKJS {

    private final MachineBuilder<?, ?>[] builders = new MachineBuilder[TIER_COUNT];

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);
    @Setter
    @Nullable
    public transient Int2IntFunction tankScalingFunction = GTMachineUtils.defaultTankSizeFunction;
    @Setter
    public transient boolean addDefaultTooltips = true;
    @Setter
    public transient boolean addDefaultModel = true;
    @Setter
    public transient boolean isGenerator = false;

    @Nullable
    public transient BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> editableUI;

    public KJSTieredMachineBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.addDefaultTooltips = false;
        this.addDefaultModel = false;
        this.dummyBuilder = true;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine,
                                   BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> editableUI,
                                   boolean isGenerator) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.machine = machine;
        this.editableUI = editableUI;
        this.isGenerator = isGenerator;
        this.dummyBuilder = true;
    }

    @Override
    public void generateMachineModels() {
        for (int tier : this.tiers) {
            generateMachineModel(this.builders[tier], this.object[tier]);
        }
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        for (int tier : this.tiers) {
            MachineDefinition definition = this.object[tier];
            if (definition == null) continue;

            final ResourceLocation id = definition.getId();
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/")));
        }
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        for (int tier : this.tiers) {
            MachineDefinition def = this.object[tier];
            if (def != null && def.getLangValue() != null) {
                lang.add(GTCEu.MOD_ID, def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public @Nullable MachineDefinition @NotNull [] createObject() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");
        @Nullable
        MachineDefinition @NotNull [] definitions = new MachineDefinition[TIER_COUNT];
        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MachineBuilder<?, ?> builder = GTRegistration.REGISTRATE.machine(
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
        return definitions;
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

        void apply(int tier, MachineBuilder<?, ?> builder);
    }
}
