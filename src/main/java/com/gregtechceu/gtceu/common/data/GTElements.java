package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

public class GTElements {

    public static final Element H = createAndRegister(GTCEu.id("Hydrogen"), 1, 0, -1, null, "Hydrogen", "H", false);
    public static final Element D = createAndRegister(GTCEu.id("Deuterium"), 1, 1, -1, "H", "Deuterium", "D", true);
    public static final Element T = createAndRegister(GTCEu.id("Tritium"), 1, 2, -1, "D", "Tritium", "T", true);
    public static final Element He = createAndRegister(GTCEu.id("Helium"), 2, 2, -1, null, "Helium", "He", false);
    public static final Element He3 = createAndRegister(GTCEu.id("Helium-3"), 2, 1, -1, "H&D", "Helium-3", "He-3",
            true);
    public static final Element Li = createAndRegister(GTCEu.id("Lithium"), 3, 4, -1, null, "Lithium", "Li", false);
    public static final Element Be = createAndRegister(GTCEu.id("Beryllium"), 4, 5, -1, null, "Beryllium", "Be", false);
    public static final Element B = createAndRegister(GTCEu.id("Boron"), 5, 5, -1, null, "Boron", "B", false);
    public static final Element C = createAndRegister(GTCEu.id("Carbon"), 6, 6, -1, null, "Carbon", "C", false);
    public static final Element N = createAndRegister(GTCEu.id("Nitrogen"), 7, 7, -1, null, "Nitrogen", "N", false);
    public static final Element O = createAndRegister(GTCEu.id("Oxygen"), 8, 8, -1, null, "Oxygen", "O", false);
    public static final Element F = createAndRegister(GTCEu.id("Fluorine"), 9, 9, -1, null, "Fluorine", "F", false);
    public static final Element Ne = createAndRegister(GTCEu.id("Neon"), 10, 10, -1, null, "Neon", "Ne", false);
    public static final Element Na = createAndRegister(GTCEu.id("Sodium"), 11, 11, -1, null, "Sodium", "Na", false);
    public static final Element Mg = createAndRegister(GTCEu.id("Magnesium"), 12, 12, -1, null, "Magnesium", "Mg",
            false);
    public static final Element Al = createAndRegister(GTCEu.id("Aluminium"), 13, 13, -1, null, "Aluminium", "Al",
            false);
    public static final Element Si = createAndRegister(GTCEu.id("Silicon"), 14, 14, -1, null, "Silicon", "Si", false);
    public static final Element P = createAndRegister(GTCEu.id("Phosphorus"), 15, 15, -1, null, "Phosphorus", "P",
            false);
    public static final Element S = createAndRegister(GTCEu.id("Sulfur"), 16, 16, -1, null, "Sulfur", "S", false);
    public static final Element Cl = createAndRegister(GTCEu.id("Chlorine"), 17, 18, -1, null, "Chlorine", "Cl", false);
    public static final Element Ar = createAndRegister(GTCEu.id("Argon"), 18, 22, -1, null, "Argon", "Ar", false);
    public static final Element K = createAndRegister(GTCEu.id("Potassium"), 19, 20, -1, null, "Potassium", "K", false);
    public static final Element Ca = createAndRegister(GTCEu.id("Calcium"), 20, 20, -1, null, "Calcium", "Ca", false);
    public static final Element Sc = createAndRegister(GTCEu.id("Scandium"), 21, 24, -1, null, "Scandium", "Sc", false);
    public static final Element Ti = createAndRegister(GTCEu.id("Titanium"), 22, 26, -1, null, "Titanium", "Ti", false);
    public static final Element V = createAndRegister(GTCEu.id("Vanadium"), 23, 28, -1, null, "Vanadium", "V", false);
    public static final Element Cr = createAndRegister(GTCEu.id("Chrome"), 24, 28, -1, null, "Chrome", "Cr", false);
    public static final Element Mn = createAndRegister(GTCEu.id("Manganese"), 25, 30, -1, null, "Manganese", "Mn",
            false);
    public static final Element Fe = createAndRegister(GTCEu.id("Iron"), 26, 30, -1, null, "Iron", "Fe", false);
    public static final Element Co = createAndRegister(GTCEu.id("Cobalt"), 27, 32, -1, null, "Cobalt", "Co", false);
    public static final Element Ni = createAndRegister(GTCEu.id("Nickel"), 28, 30, -1, null, "Nickel", "Ni", false);
    public static final Element Cu = createAndRegister(GTCEu.id("Copper"), 29, 34, -1, null, "Copper", "Cu", false);
    public static final Element Zn = createAndRegister(GTCEu.id("Zinc"), 30, 35, -1, null, "Zinc", "Zn", false);
    public static final Element Ga = createAndRegister(GTCEu.id("Gallium"), 31, 39, -1, null, "Gallium", "Ga", false);
    public static final Element Ge = createAndRegister(GTCEu.id("Germanium"), 32, 40, -1, null, "Germanium", "Ge",
            false);
    public static final Element As = createAndRegister(GTCEu.id("Arsenic"), 33, 42, -1, null, "Arsenic", "As", false);
    public static final Element Se = createAndRegister(GTCEu.id("Selenium"), 34, 45, -1, null, "Selenium", "Se", false);
    public static final Element Br = createAndRegister(GTCEu.id("Bromine"), 35, 45, -1, null, "Bromine", "Br", false);
    public static final Element Kr = createAndRegister(GTCEu.id("Krypton"), 36, 48, -1, null, "Krypton", "Kr", false);
    public static final Element Rb = createAndRegister(GTCEu.id("Rubidium"), 37, 48, -1, null, "Rubidium", "Rb", false);
    public static final Element Sr = createAndRegister(GTCEu.id("Strontium"), 38, 49, -1, null, "Strontium", "Sr",
            false);
    public static final Element Y = createAndRegister(GTCEu.id("Yttrium"), 39, 50, -1, null, "Yttrium", "Y", false);
    public static final Element Zr = createAndRegister(GTCEu.id("Zirconium"), 40, 51, -1, null, "Zirconium", "Zr",
            false);
    public static final Element Nb = createAndRegister(GTCEu.id("Niobium"), 41, 53, -1, null, "Niobium", "Nb", false);
    public static final Element Mo = createAndRegister(GTCEu.id("Molybdenum"), 42, 53, -1, null, "Molybdenum", "Mo",
            false);
    public static final Element Tc = createAndRegister(GTCEu.id("Technetium"), 43, 55, -1, null, "Technetium", "Tc",
            false);
    public static final Element Ru = createAndRegister(GTCEu.id("Ruthenium"), 44, 57, -1, null, "Ruthenium", "Ru",
            false);
    public static final Element Rh = createAndRegister(GTCEu.id("Rhodium"), 45, 58, -1, null, "Rhodium", "Rh", false);
    public static final Element Pd = createAndRegister(GTCEu.id("Palladium"), 46, 60, -1, null, "Palladium", "Pd",
            false);
    public static final Element Ag = createAndRegister(GTCEu.id("Silver"), 47, 60, -1, null, "Silver", "Ag", false);
    public static final Element Cd = createAndRegister(GTCEu.id("Cadmium"), 48, 64, -1, null, "Cadmium", "Cd", false);
    public static final Element In = createAndRegister(GTCEu.id("Indium"), 49, 65, -1, null, "Indium", "In", false);
    public static final Element Sn = createAndRegister(GTCEu.id("Tin"), 50, 68, -1, null, "Tin", "Sn", false);
    public static final Element Sb = createAndRegister(GTCEu.id("Antimony"), 51, 70, -1, null, "Antimony", "Sb", false);
    public static final Element Te = createAndRegister(GTCEu.id("Tellurium"), 52, 75, -1, null, "Tellurium", "Te",
            false);
    public static final Element I = createAndRegister(GTCEu.id("Iodine"), 53, 74, -1, null, "Iodine", "I", false);
    public static final Element Xe = createAndRegister(GTCEu.id("Xenon"), 54, 77, -1, null, "Xenon", "Xe", false);
    public static final Element Cs = createAndRegister(GTCEu.id("Caesium"), 55, 77, -1, null, "Caesium", "Cs", false);
    public static final Element Ba = createAndRegister(GTCEu.id("Barium"), 56, 81, -1, null, "Barium", "Ba", false);
    public static final Element La = createAndRegister(GTCEu.id("Lanthanum"), 57, 81, -1, null, "Lanthanum", "La",
            false);
    public static final Element Ce = createAndRegister(GTCEu.id("Cerium"), 58, 82, -1, null, "Cerium", "Ce", false);
    public static final Element Pr = createAndRegister(GTCEu.id("Praseodymium"), 59, 81, -1, null, "Praseodymium", "Pr",
            false);
    public static final Element Nd = createAndRegister(GTCEu.id("Neodymium"), 60, 84, -1, null, "Neodymium", "Nd",
            false);
    public static final Element Pm = createAndRegister(GTCEu.id("Promethium"), 61, 83, -1, null, "Promethium", "Pm",
            false);
    public static final Element Sm = createAndRegister(GTCEu.id("Samarium"), 62, 88, -1, null, "Samarium", "Sm", false);
    public static final Element Eu = createAndRegister(GTCEu.id("Europium"), 63, 88, -1, null, "Europium", "Eu", false);
    public static final Element Gd = createAndRegister(GTCEu.id("Gadolinium"), 64, 93, -1, null, "Gadolinium", "Gd",
            false);
    public static final Element Tb = createAndRegister(GTCEu.id("Terbium"), 65, 93, -1, null, "Terbium", "Tb", false);
    public static final Element Dy = createAndRegister(GTCEu.id("Dysprosium"), 66, 96, -1, null, "Dysprosium", "Dy",
            false);
    public static final Element Ho = createAndRegister(GTCEu.id("Holmium"), 67, 97, -1, null, "Holmium", "Ho", false);
    public static final Element Er = createAndRegister(GTCEu.id("Erbium"), 68, 99, -1, null, "Erbium", "Er", false);
    public static final Element Tm = createAndRegister(GTCEu.id("Thulium"), 69, 99, -1, null, "Thulium", "Tm", false);
    public static final Element Yb = createAndRegister(GTCEu.id("Ytterbium"), 70, 103, -1, null, "Ytterbium", "Yb",
            false);
    public static final Element Lu = createAndRegister(GTCEu.id("Lutetium"), 71, 103, -1, null, "Lutetium", "Lu",
            false);
    public static final Element Hf = createAndRegister(GTCEu.id("Hafnium"), 72, 106, -1, null, "Hafnium", "Hf", false);
    public static final Element Ta = createAndRegister(GTCEu.id("Tantalum"), 73, 107, -1, null, "Tantalum", "Ta",
            false);
    public static final Element W = createAndRegister(GTCEu.id("Tungsten"), 74, 109, -1, null, "Tungsten", "W", false);
    public static final Element Re = createAndRegister(GTCEu.id("Rhenium"), 75, 111, -1, null, "Rhenium", "Re", false);
    public static final Element Os = createAndRegister(GTCEu.id("Osmium"), 76, 114, -1, null, "Osmium", "Os", false);
    public static final Element Ir = createAndRegister(GTCEu.id("Iridium"), 77, 115, -1, null, "Iridium", "Ir", false);
    public static final Element Pt = createAndRegister(GTCEu.id("Platinum"), 78, 117, -1, null, "Platinum", "Pt",
            false);
    public static final Element Au = createAndRegister(GTCEu.id("Gold"), 79, 117, -1, null, "Gold", "Au", false);
    public static final Element Hg = createAndRegister(GTCEu.id("Mercury"), 80, 120, -1, null, "Mercury", "Hg", false);
    public static final Element Tl = createAndRegister(GTCEu.id("Thallium"), 81, 123, -1, null, "Thallium", "Tl",
            false);
    public static final Element Pb = createAndRegister(GTCEu.id("Lead"), 82, 125, -1, null, "Lead", "Pb", false);
    public static final Element Bi = createAndRegister(GTCEu.id("Bismuth"), 83, 125, -1, null, "Bismuth", "Bi", false);
    public static final Element Po = createAndRegister(GTCEu.id("Polonium"), 84, 124, -1, null, "Polonium", "Po",
            false);
    public static final Element At = createAndRegister(GTCEu.id("Astatine"), 85, 124, -1, null, "Astatine", "At",
            false);
    public static final Element Rn = createAndRegister(GTCEu.id("Radon"), 86, 134, -1, null, "Radon", "Rn", false);
    public static final Element Fr = createAndRegister(GTCEu.id("Francium"), 87, 134, -1, null, "Francium", "Fr",
            false);
    public static final Element Ra = createAndRegister(GTCEu.id("Radium"), 88, 136, -1, null, "Radium", "Ra", false);
    public static final Element Ac = createAndRegister(GTCEu.id("Actinium"), 89, 136, -1, null, "Actinium", "Ac",
            false);
    public static final Element Th = createAndRegister(GTCEu.id("Thorium"), 90, 140, -1, null, "Thorium", "Th", false);
    public static final Element Pa = createAndRegister(GTCEu.id("Protactinium"), 91, 138, -1, null, "Protactinium",
            "Pa", false);
    public static final Element U = createAndRegister(GTCEu.id("Uranium"), 92, 146, -1, null, "Uranium", "U", false);
    public static final Element U238 = createAndRegister(GTCEu.id("Uranium-238"), 92, 146, -1, null, "Uranium-238",
            "U-238", false);
    public static final Element U235 = createAndRegister(GTCEu.id("Uranium-235"), 92, 143, -1, null, "Uranium-235",
            "U-235", true);
    public static final Element Np = createAndRegister(GTCEu.id("Neptunium"), 93, 144, -1, null, "Neptunium", "Np",
            false);
    public static final Element Pu = createAndRegister(GTCEu.id("Plutonium"), 94, 152, -1, null, "Plutonium", "Pu",
            false);
    public static final Element Pu239 = createAndRegister(GTCEu.id("Plutonium-239"), 94, 145, -1, null, "Plutonium-239",
            "Pu-239", false);
    public static final Element Pu241 = createAndRegister(GTCEu.id("Plutonium-241"), 94, 149, -1, null, "Plutonium-241",
            "Pu-241", true);
    public static final Element Am = createAndRegister(GTCEu.id("Americium"), 95, 150, -1, null, "Americium", "Am",
            false);
    public static final Element Cm = createAndRegister(GTCEu.id("Curium"), 96, 153, -1, null, "Curium", "Cm", false);
    public static final Element Bk = createAndRegister(GTCEu.id("Berkelium"), 97, 152, -1, null, "Berkelium", "Bk",
            false);
    public static final Element Cf = createAndRegister(GTCEu.id("Californium"), 98, 153, -1, null, "Californium", "Cf",
            false);
    public static final Element Es = createAndRegister(GTCEu.id("Einsteinium"), 99, 153, -1, null, "Einsteinium", "Es",
            false);
    public static final Element Fm = createAndRegister(GTCEu.id("Fermium"), 100, 157, -1, null, "Fermium", "Fm", false);
    public static final Element Md = createAndRegister(GTCEu.id("Mendelevium"), 101, 157, -1, null, "Mendelevium", "Md",
            false);
    public static final Element No = createAndRegister(GTCEu.id("Nobelium"), 102, 157, -1, null, "Nobelium", "No",
            false);
    public static final Element Lr = createAndRegister(GTCEu.id("Lawrencium"), 103, 159, -1, null, "Lawrencium", "Lr",
            false);
    public static final Element Rf = createAndRegister(GTCEu.id("Rutherfordium"), 104, 161, -1, null, "Rutherfordium",
            "Rf", false);
    public static final Element Db = createAndRegister(GTCEu.id("Dubnium"), 105, 163, -1, null, "Dubnium", "Db", false);
    public static final Element Sg = createAndRegister(GTCEu.id("Seaborgium"), 106, 165, -1, null, "Seaborgium", "Sg",
            false);
    public static final Element Bh = createAndRegister(GTCEu.id("Bohrium"), 107, 163, -1, null, "Bohrium", "Bh", false);
    public static final Element Hs = createAndRegister(GTCEu.id("Hassium"), 108, 169, -1, null, "Hassium", "Hs", false);
    public static final Element Mt = createAndRegister(GTCEu.id("Meitnerium"), 109, 167, -1, null, "Meitnerium", "Mt",
            false);
    public static final Element Ds = createAndRegister(GTCEu.id("Darmstadtium"), 110, 171, -1, null, "Darmstadtium",
            "Ds", false);
    public static final Element Rg = createAndRegister(GTCEu.id("Roentgenium"), 111, 169, -1, null, "Roentgenium", "Rg",
            false);
    public static final Element Cn = createAndRegister(GTCEu.id("Copernicium"), 112, 173, -1, null, "Copernicium", "Cn",
            false);
    public static final Element Nh = createAndRegister(GTCEu.id("Nihonium"), 113, 171, -1, null, "Nihonium", "Nh",
            false);
    public static final Element Fl = createAndRegister(GTCEu.id("Flerovium"), 114, 175, -1, null, "Flerovium", "Fl",
            false);
    public static final Element Mc = createAndRegister(GTCEu.id("Moscovium"), 115, 173, -1, null, "Moscovium", "Mc",
            false);
    public static final Element Lv = createAndRegister(GTCEu.id("Livermorium"), 116, 177, -1, null, "Livermorium", "Lv",
            false);
    public static final Element Ts = createAndRegister(GTCEu.id("Tennessine"), 117, 177, -1, null, "Tennessine", "Ts",
            false);
    public static final Element Og = createAndRegister(GTCEu.id("Oganesson"), 118, 176, -1, null, "Oganesson", "Og",
            false);
    public static final Element Tr = createAndRegister(GTCEu.id("Tritanium"), 119, 178, -1, null, "Tritanium", "Tr",
            false);
    public static final Element Dr = createAndRegister(GTCEu.id("Duranium"), 120, 180, -1, null, "Duranium", "Dr",
            false);
    public static final Element Ke = createAndRegister(GTCEu.id("Trinium"), 125, 198, -1, null, "Trinium", "Ke", false);
    public static final Element Nq = createAndRegister(GTCEu.id("Naquadah"), 174, 352, 140, null, "Naquadah", "Nq",
            true);
    public static final Element Nq1 = createAndRegister(GTCEu.id("NaquadahEnriched"), 174, 354, 140, null,
            "NaquadahEnriched", "Nq+", true);
    public static final Element Nq2 = createAndRegister(GTCEu.id("Naquadria"), 174, 348, 140, null, "Naquadria", "*Nq*",
            true);
    public static final Element Nt = createAndRegister(GTCEu.id("Neutronium"), 0, 1000, -1, null, "Neutronium", "Nt",
            false);
    public static final Element Sp = createAndRegister(GTCEu.id("Space"), 1, 0, -1, null, "Space", "Sp", false);
    public static final Element Ma = createAndRegister(GTCEu.id("Magic"), 1, 0, -1, null, "Magic", "Ma", false);

    /**
     * @deprecated Use
     *             {@link GTElements#createAndRegister(ResourceLocation, long, long, long, String, String, String, boolean)}
     */
    @Deprecated
    public static Element createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo,
                                            String name, String symbol, boolean isIsotope) {
        return createAndRegister(GTCEu.id(name), protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }

    public static Element createAndRegister(ResourceLocation id, long protons, long neutrons, long halfLifeSeconds,
                                            String decayTo,
                                            String name, String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        GTRegistries.register(GTRegistries.ELEMENTS, GTCEu.id(name), element);
        return element;
    }

    public static void init() {}

    /**
     * @deprecated Use {@code GTRegistries.ELEMENTS.get(name)} instead
     */
    @Deprecated(since = "8.0.0")
    public static Element get(String name) {
        return GTRegistries.ELEMENTS.get(GTCEu.id(name));
    }
}
