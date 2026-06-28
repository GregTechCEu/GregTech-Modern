package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.helpers.IGTDummyBuilder;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class KJSWrappingMachineBuilder extends BuilderBase<MachineDefinition> implements IMachineBuilderKJS, IGTDummyBuilder<MachineDefinition> {

    @HideFromJS
    @Getter
    private final KJSTieredMachineBuilder tieredBuilder;

    public KJSWrappingMachineBuilder(ResourceLocation id, KJSTieredMachineBuilder tieredBuilder) {
        super(id);
        this.tieredBuilder = tieredBuilder;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    public KJSWrappingMachineBuilder tiers(int... tiers) {
        tieredBuilder.tiers(tiers);
        return this;
    }

    public KJSWrappingMachineBuilder machine(KJSTieredMachineBuilder.TieredCreationFunction machine) {
        tieredBuilder.machine(machine);
        return this;
    }

    public KJSWrappingMachineBuilder definition(KJSTieredMachineBuilder.DefinitionFunction definition) {
        tieredBuilder.definition(definition);
        return this;
    }

    public KJSWrappingMachineBuilder tankScalingFunction(Int2IntFunction tankScalingFunction) {
        tieredBuilder.tankScalingFunction(tankScalingFunction);
        return this;
    }

    public KJSWrappingMachineBuilder addDefaultTooltips(boolean addDefaultTooltips) {
        tieredBuilder.addDefaultTooltips(addDefaultTooltips);
        return this;
    }

    public KJSWrappingMachineBuilder addDefaultModel(boolean addDefaultModel) {
        tieredBuilder.addDefaultModel(addDefaultModel);
        return this;
    }

    public KJSWrappingMachineBuilder isGenerator(boolean isGenerator) {
        tieredBuilder.isGenerator(isGenerator);
        return this;
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        tieredBuilder.generateDataJsons(generator);
    }

    @Override
    public void generateMachineModels() {
        tieredBuilder.generateMachineModels();
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        tieredBuilder.generateAssetJsons(generator);
    }

    @Override
    public void generateLang(LangEventJS lang) {
        tieredBuilder.generateLang(lang);
    }

    @Override
    public MachineDefinition createObject() {
        return tieredBuilder.createObject();
    }
}
