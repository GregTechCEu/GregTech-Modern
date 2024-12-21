package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTElements;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class ElementBuilder extends BuilderBase<Element> {

    public transient final String name;

    @Setter
    public transient Component translatableName;
    @Setter
    public transient long protons, neutrons, halfLifeSeconds = -1;
    @Setter
    public transient String decayTo, symbol;
    @Setter
    public transient boolean isIsotope;

    public ElementBuilder(ResourceLocation id) {
        super(id);
        name = id.getPath();
        translatableName = Component.translatable(id.toLanguageKey("element"));
    }

    @Override
    public Element register() {
        return value = GTElements.createAndRegister(protons, neutrons, halfLifeSeconds, decayTo, name, symbol,
                isIsotope);
    }
}
