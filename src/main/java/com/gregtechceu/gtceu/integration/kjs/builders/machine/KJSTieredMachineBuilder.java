package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.helpers.IGTDummyBuilder;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@Accessors(fluent = true, chain = true)
public class KJSTieredMachineBuilder extends BuilderBase<MachineDefinition>
                                     implements IMachineBuilderKJS, IGTDummyBuilder<MachineDefinition> {

    private final @Nullable MachineBuilder<?, ?>[] builders = new MachineBuilder[TIER_COUNT];
    private final @Nullable MachineDefinition[] machines = new MachineDefinition[TIER_COUNT];

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

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine,
                                   boolean isGenerator) {
        super(id);
        this.machine = machine;
        this.isGenerator = isGenerator;
    }

    @Override
    public void generateMachineModels() {
        for (int tier : this.tiers) {
            generateMachineModel(this.builders[tier], this.machines[tier]);
        }
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        for (int tier : this.tiers) {
            var definition = this.machines[tier];
            if (definition == null) continue;

            final ResourceLocation id = definition.getId();
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
        }
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        for (int tier : this.tiers) {
            MachineDefinition def = this.machines[tier];
            if (def != null) {
                lang.add(GTCEu.MOD_ID, def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public MachineDefinition createObject() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");

        MachineDefinition anyDefinition = null;

        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            final Int2IntFunction tankFunction = Objects.requireNonNullElse(tankScalingFunction,
                    GTMachineUtils.defaultTankSizeFunction);

            MachineBuilder<?, ?> builder = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
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

            this.builders[tier] = builder;
            this.machines[tier] = builder.register();
            anyDefinition = this.machines[tier];
        }
        return Objects.requireNonNull(anyDefinition);
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

        void apply(int tier, MachineBuilder<?, ?> builder);
    }
}
