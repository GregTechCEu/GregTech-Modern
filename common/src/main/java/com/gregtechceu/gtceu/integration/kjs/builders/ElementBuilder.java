package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;

public class ElementBuilder extends BuilderBase<Element> {
    public transient long protons, neutrons, halfLifeSeconds;
    public transient String decayTo, name, symbol;
    public transient boolean isIsotope;

    public ElementBuilder(ResourceLocation i, Object... args) {
        super(i);
        protons = ((Double)args[0]).intValue();
        neutrons = ((Double)args[1]).intValue();
        halfLifeSeconds = ((Double)args[2]).intValue();
        decayTo = args[3] == null ? null : args[3].toString();
        name = i.getPath();
        symbol = args[4] == null ? "" : args[4].toString();
        isIsotope = (Boolean) args[5];
    }

    @Override
    public Element register() {
        return value = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }
}
