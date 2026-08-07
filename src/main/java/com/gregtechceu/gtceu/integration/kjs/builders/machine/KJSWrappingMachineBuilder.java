package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;

import java.util.Arrays;

@SuppressWarnings("unused")
public class KJSWrappingMachineBuilder extends BuilderBase<MachineDefinition> implements IMachineBuilderKJS {

    @HideFromJS
    @Getter
    private final KJSTieredMachineBuilder tieredBuilder;

    public KJSWrappingMachineBuilder(ResourceLocation id, KJSTieredMachineBuilder tieredBuilder) {
        super(id);
        this.tieredBuilder = tieredBuilder;
        this.dummyBuilder = true;
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
    public void generateData(KubeDataGenerator generator) {
        tieredBuilder.generateData(generator);
    }

    @Override
    public void generateMachineModels() {
        tieredBuilder.generateMachineModels();
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        tieredBuilder.generateAssets(generator);
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        tieredBuilder.generateLang(lang);
    }

    @Override
    public MachineDefinition createObject() {
        for (var def : tieredBuilder.createTransformedObject()) {
            if (def != null) {
                return def;
            }
        }
        // should never happen.
        throw new IllegalStateException("Empty tiered machine builder " + Arrays.toString(tieredBuilder.get()) +
                " With id " + tieredBuilder.id);
    }
}
