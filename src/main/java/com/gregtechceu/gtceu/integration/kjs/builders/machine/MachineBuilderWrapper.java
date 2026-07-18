package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;

public class MachineBuilderWrapper<D extends MachineDefinition, B extends MachineBuilder<D, ?, ?, B>> extends BuilderBase<D> implements IMachineBuilderKJS {

    protected final B builder;

    public MachineBuilderWrapper(B builder) {
        super(builder.getOwner().makeResourceLocation(builder.getName()));
        this.builder = builder;
    }

    @Override
    public D createObject() {
        return builder.createEntry();
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(builder, get());
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/")));
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        D value = get();
        if (value != null && value.getLangValue() != null) {
            lang.add(id.getNamespace(), value.getDescriptionId(), value.getLangValue());
        }
    }
}
