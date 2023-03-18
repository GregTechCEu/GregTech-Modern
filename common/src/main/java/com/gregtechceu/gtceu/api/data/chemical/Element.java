package com.gregtechceu.gtceu.api.data.chemical;


/**
 * This is some kind of Periodic Table, which can be used to determine Properties of the Materials.
 * @param protons         Amount of Protons
 * @param neutrons        Amount of Neutrons (I could have made mistakes with the Neutron amount calculation, please tell me if I did something wrong)
 * @param halfLifeSeconds Amount of Half Life this Material has in Seconds. -1 for stable Materials
 * @param decayTo         String representing the Elements it decays to. Separated by an '&' Character
 * @param name            Name of the Element
 * @param symbol          Symbol of the Element
 */
public record Element(long protons, long neutrons, long halfLifeSeconds, String decayTo, String name, String symbol, boolean isIsotope) {
    public long mass() {
        return protons + neutrons;
    }

}
