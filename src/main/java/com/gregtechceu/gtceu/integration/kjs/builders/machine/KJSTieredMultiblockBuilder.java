package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTRegistryInfo;

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
public class KJSTieredMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition> implements IMachineBuilderKJS {

    private final @Nullable MultiblockMachineBuilder<?, ?>[] builders = new MultiblockMachineBuilder[TIER_COUNT];
    private final @Nullable MultiblockMachineDefinition[] machines = new MultiblockMachineDefinition[TIER_COUNT];
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
        super(id);
        this.machine = machine;

        this.dummyBuilder = true;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
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
            MachineDefinition definition = this.machines[tier];
            if (definition == null) continue;

            final ResourceLocation id = definition.getId();
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
        }
    }

    @Override
    public String getTranslationKeyGroup() {
        return "block";
    }

    @Override
    public void generateLang(LangEventJS lang) {
        for (int tier : tiers) {
            MachineDefinition def = machines[tier];
            if (def != null && def.getLangValue() != null) {
                lang.add(def.getId().getNamespace(), def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public MultiblockMachineDefinition createObject() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");

        MultiblockMachineDefinition anyDefinition = null;

        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MultiblockMachineBuilder<?, ?> builder = GTRegistrate
                    .createIgnoringListenerErrors(this.id.getNamespace())
                    .multiblock(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.create(holder, tier));

            builder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);
            this.definition.apply(tier, builder);
            this.builders[tier] = builder;
            this.machines[tier] = builder.createEntry();
            anyDefinition = this.machines[tier];
        }
        return Objects.requireNonNull(anyDefinition);
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
