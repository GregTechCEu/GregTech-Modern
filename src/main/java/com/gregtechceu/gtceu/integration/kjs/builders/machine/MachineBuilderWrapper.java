package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class MachineBuilderWrapper<D extends MachineDefinition, B extends MachineBuilder<D, ?, B>> extends BuilderBase<D> implements IMachineBuilderKJS {

    protected final B builder;

    public MachineBuilderWrapper(B builder) {
        super(builder.getOwner().makeResourceLocation(builder.getName()));
        this.builder = builder;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
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
    public void generateAssetJsons(AssetJsonGenerator generator) {
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
    }

    @Override
    public void generateLang(LangEventJS lang) {
        D value = get();
        if (value != null && value.getLangValue() != null) {
            lang.add(id.getNamespace(), value.getDescriptionId(), value.getLangValue());
        }
    }
}
