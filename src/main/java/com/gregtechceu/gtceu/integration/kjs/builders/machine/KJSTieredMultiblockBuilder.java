package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.machine.GTMachineUtils;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Accessors(fluent = true, chain = true)
public class KJSTieredMultiblockBuilder extends BuilderBase<@Nullable MultiblockMachineDefinition @NotNull []>
                                        implements IMachineBuilderKJS {

    private final MultiblockMachineBuilder[] builders = new MultiblockMachineBuilder[TIER_COUNT];

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);

    public KJSTieredMultiblockBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.dummyBuilder = true;
    }

    public KJSTieredMultiblockBuilder(ResourceLocation id, TieredCreationFunction machine) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.machine = machine;
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
        for (int tier : tiers) {
            MachineDefinition def = object[tier];
            if (def != null && def.getLangValue() != null) {
                lang.add(def.getId().getNamespace(), def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public @Nullable MultiblockMachineDefinition @NotNull [] createObject() {
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
        return definitions;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MultiblockControllerMachine create(IMachineBlockEntity holder, int tier);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?, ?> builder);
    }
}
