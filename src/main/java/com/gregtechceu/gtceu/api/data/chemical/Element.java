package com.gregtechceu.gtceu.api.data.chemical;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

/**
 * This is some kind of Periodic Table, which can be used to determine "Properties" of the Materials.
 *
 * @param protons         Amount of Protons
 * @param neutrons        Amount of Neutrons
 * @param halfLifeSeconds Amount of Half Life this Material has in Seconds. -1 for stable Materials
 * @param decayTo         String representing the Elements this element decays to. Separated by an '&' Character
 * @param name            Name of the Element
 * @param symbol          Symbol of the Element
 * @param isIsotope       Is this element an isotope?
 */
@Accessors(fluent = true, chain = false)
public record Element(long protons, long neutrons, double halfLifeSeconds, @Nullable String decayTo, String name,
                      String symbol, boolean isIsotope) {

    public long mass() {
        return protons + neutrons;
    }
}
