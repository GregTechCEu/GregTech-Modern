package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTElements;
import dev.latvian.mods.kubejs.event.EventJS;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote MaterialEventJS
 */
public class ElementEventJS extends EventJS {

    public Element createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo, String name, String symbol, boolean isIsotope) {
        return GTElements.createAndRegister(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }

    public boolean remove(String name) {
        return GTRegistries.ELEMENTS.remove(name);
    }

    @Nullable
    public Element get(String name) {
        return GTRegistries.ELEMENTS.get(name);
    }
}
