package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.BuilderBase;
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
        super(GTResourceLocation.implicitAsGtceu(id));
        name = id.getPath();
        translatableName = Component.translatable(id.toLanguageKey("element"));
    }

    @Override
    public Element createObject() {
        return GTElements.createAndRegister(protons, neutrons, halfLifeSeconds, decayTo, name, symbol,
                isIsotope);
    }
}
