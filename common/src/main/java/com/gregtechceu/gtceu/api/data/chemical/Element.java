package com.gregtechceu.gtceu.api.data.chemical;


import lombok.Setter;

/**
 * This is some kind of Periodic Table, which can be used to determine Properties of the Materials.
 * @param protons         Amount of Protons
 * @param neutrons        Amount of Neutrons (I could have made mistakes with the Neutron amount calculation, please tell me if I did something wrong)
 * @param halfLifeSeconds Amount of Half Life this Material has in Seconds. -1 for stable Materials
 * @param decayTo         String representing the Elements it decays to. Separated by an '&' Character
 * @param name            Name of the Element
 * @param symbol          Symbol of the Element
 */
public record Element(@Setter long protons, @Setter long neutrons, @Setter long halfLifeSeconds, @Setter String decayTo, @Setter String name, @Setter String symbol, @Setter boolean isIsotope) {
    public long mass() {
        return protons + neutrons;
    }

}
