package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;

import java.util.Arrays;

public class KJSWrappingMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition>
                                          implements IMachineBuilderKJS {

    @HideFromJS
    @Getter
    private final KJSTieredMultiblockBuilder tieredBuilder;

    public KJSWrappingMultiblockBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.tieredBuilder = new KJSTieredMultiblockBuilder(this.id);
        this.dummyBuilder = true;
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
    public MultiblockMachineDefinition createObject() {
        for (var def : tieredBuilder.createTransformedObject()) {
            if (def != null) {
                return def;
            }
        }
        // should never happen.
        throw new IllegalStateException("Empty tiered multiblock builder " + Arrays.toString(tieredBuilder.get()) +
                " With id " + tieredBuilder.id);
    }

    public static MultiblockMachineBuilder<?, ?> createKJSMulti(ResourceLocation id) {
        return new MultiblockMachineBuilder<>(GTRegistration.REGISTRATE, id.getPath(),
                WorkableElectricMultiblockMachine::new,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
    }

    public static MultiblockMachineBuilder<?, ?> createKJSMulti(ResourceLocation id,
                                                                KJSTieredMachineBuilder.CreationFunction<? extends MultiblockControllerMachine> machine) {
        return new MultiblockMachineBuilder<>(GTRegistration.REGISTRATE, id.getPath(),
                machine::create,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
    }
}
