package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.helpers.IGTDummyBuilder;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;

public class KJSWrappingMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition>
                                          implements IMachineBuilderKJS, IGTDummyBuilder<MultiblockMachineDefinition> {

    @HideFromJS
    @Getter
    private final KJSTieredMultiblockBuilder tieredBuilder;

    public KJSWrappingMultiblockBuilder(ResourceLocation id) {
        super(id);
        this.tieredBuilder = new KJSTieredMultiblockBuilder(this.id);
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    public KJSWrappingMultiblockBuilder tiers(int... tiers) {
        tieredBuilder.tiers(tiers);
        return this;
    }

    public KJSWrappingMultiblockBuilder machine(KJSTieredMultiblockBuilder.TieredCreationFunction machine) {
        tieredBuilder.machine(machine);
        return this;
    }

    public KJSWrappingMultiblockBuilder definition(KJSTieredMultiblockBuilder.DefinitionFunction definition) {
        tieredBuilder.definition(definition);
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
    public MultiblockMachineDefinition createObject() {
        return tieredBuilder.createObject();
    }
}
