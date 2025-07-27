package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Accessors(fluent = true, chain = true)
public class KJSTieredMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition[]> {

    private final MultiblockMachineBuilder[] builders = new MultiblockMachineBuilder[TIER_COUNT];

    @Setter
    public volatile int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public volatile TieredCreationFunction machine;
    @Setter
    public volatile DefinitionFunction definition = (tier, def) -> def.tier(tier);

    public KJSTieredMultiblockBuilder(ResourceLocation id) {
        super(id);
    }

    public KJSTieredMultiblockBuilder(ResourceLocation id, TieredCreationFunction machine) {
        super(id);
        this.machine = machine;
    }

    @Override
    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {
        super.generateAssetJsons(generator);
        for (int tier : this.tiers) {
            MultiblockMachineBuilder builder = this.builders[tier];
            if (builder != null) {
                builder.generateAssetJsons(generator);
            }
        }
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        for (int tier : tiers) {
            MultiblockMachineBuilder builder = this.builders[tier];
            if (builder != null) {
                builder.generateLang(lang);
            }
        }
    }

    @Override
    public @Nullable MultiblockMachineDefinition @NotNull [] register() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[TIER_COUNT];
        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MultiblockMachineBuilder builder = GTRegistration.REGISTRATE.multiblock(
                    String.format("%s_%s", tierName, this.id.getPath()),
                    holder -> machine.create(holder, tier));

            builder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);
            this.definition.apply(tier, builder);
            this.builders[tier] = builder;
            definitions[tier] = builder.register();
        }
        return value = definitions;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MultiblockControllerMachine create(IMachineBlockEntity holder, int tier);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?> builder);
    }
}
