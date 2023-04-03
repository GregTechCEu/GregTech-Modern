package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;

public class ElementBuilder extends BuilderBase<Element> {
    public transient long protons, neutrons, halfLifeSeconds;
    public transient String decayTo, name, symbol;
    public transient boolean isIsotope;


    public ElementBuilder(ResourceLocation i) {
        super(i);
        protons = -1;
        neutrons = -1;
        halfLifeSeconds = -1;
        decayTo = null;
        name = i.getPath();
        symbol = "";
        isIsotope = false;
    }

    public ElementBuilder protons(long protons) {
        this.protons = protons;
        return this;
    }

    public ElementBuilder neutrons(long neutrons) {
        this.neutrons = neutrons;
        return this;
    }

    public ElementBuilder halfLifeSeconds(long halfLifeSeconds) {
        this.halfLifeSeconds = halfLifeSeconds;
        return this;
    }

    public ElementBuilder decayTo(String decayTo) {
        this.decayTo = decayTo;
        return this;
    }

    public ElementBuilder symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super Element> getRegistryType() {
        return GregTechKubeJSPlugin.ELEMENT;
    }

    @Override
    public Element createObject() {
        return new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }
}
