package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraftforge.fml.ModLoader;

public class GTElements {

    static {
        GTRegistries.ELEMENTS.unfreeze();
    }

    public static final Element H = createAndRegister(1, 0, -1, null, "Hydrogen", "H", false);
    public static final Element D = createAndRegister(1, 1, -1, "H", "Deuterium", "D", true);
    public static final Element T = createAndRegister(1, 2, -1, "D", "Tritium", "T", true);
    public static final Element He = createAndRegister(2, 2, -1, null, "Helium", "He", false);
    public static final Element He3 = createAndRegister(2, 1, -1, "H&D", "Helium-3", "He-3", true);
    public static final Element Li = createAndRegister(3, 4, -1, null, "Lithium", "Li", false);
    public static final Element Be = createAndRegister(4, 5, -1, null, "Beryllium", "Be", false);
    public static final Element B = createAndRegister(5, 5, -1, null, "Boron", "B", false);
    public static final Element C = createAndRegister(6, 6, -1, null, "Carbon", "C", false);
    public static final Element N = createAndRegister(7, 7, -1, null, "Nitrogen", "N", false);
    public static final Element O = createAndRegister(8, 8, -1, null, "Oxygen", "O", false);
    public static final Element F = createAndRegister(9, 9, -1, null, "Fluorine", "F", false);
    public static final Element Ne = createAndRegister(10, 10, -1, null, "Neon", "Ne", false);
    public static final Element Na = createAndRegister(11, 11, -1, null, "Sodium", "Na", false);
    public static final Element Mg = createAndRegister(12, 12, -1, null, "Magnesium", "Mg", false);
    public static final Element Al = createAndRegister(13, 13, -1, null, "Aluminium", "Al", false);
    public static final Element Si = createAndRegister(14, 14, -1, null, "Silicon", "Si", false);
    public static final Element P = createAndRegister(15, 15, -1, null, "Phosphorus", "P", false);
    public static final Element S = createAndRegister(16, 16, -1, null, "Sulfur", "S", false);
    public static final Element Cl = createAndRegister(17, 18, -1, null, "Chlorine", "Cl", false);
    public static final Element Ar = createAndRegister(18, 22, -1, null, "Argon", "Ar", false);
    public static final Element K = createAndRegister(19, 20, -1, null, "Potassium", "K", false);
    public static final Element Ca = createAndRegister(20, 20, -1, null, "Calcium", "Ca", false);
    public static final Element Sc = createAndRegister(21, 24, -1, null, "Scandium", "Sc", false);
    public static final Element Ti = createAndRegister(22, 26, -1, null, "Titanium", "Ti", false);
    public static final Element V = createAndRegister(23, 28, -1, null, "Vanadium", "V", false);
    public static final Element Cr = createAndRegister(24, 28, -1, null, "Chrome", "Cr", false);
    public static final Element Mn = createAndRegister(25, 30, -1, null, "Manganese", "Mn", false);
    public static final Element Fe = createAndRegister(26, 30, -1, null, "Iron", "Fe", false);
    public static final Element Co = createAndRegister(27, 32, -1, null, "Cobalt", "Co", false);
    public static final Element Ni = createAndRegister(28, 30, -1, null, "Nickel", "Ni", false);
    public static final Element Cu = createAndRegister(29, 34, -1, null, "Copper", "Cu", false);
    public static final Element Zn = createAndRegister(30, 35, -1, null, "Zinc", "Zn", false);
    public static final Element Ga = createAndRegister(31, 39, -1, null, "Gallium", "Ga", false);
    public static final Element Ge = createAndRegister(32, 40, -1, null, "Germanium", "Ge", false);
    public static final Element As = createAndRegister(33, 42, -1, null, "Arsenic", "As", false);
    public static final Element Se = createAndRegister(34, 45, -1, null, "Selenium", "Se", false);
    public static final Element Br = createAndRegister(35, 45, -1, null, "Bromine", "Br", false);
    public static final Element Kr = createAndRegister(36, 48, -1, null, "Krypton", "Kr", false);
    public static final Element Rb = createAndRegister(37, 48, -1, null, "Rubidium", "Rb", false);
    public static final Element Sr = createAndRegister(38, 49, -1, null, "Strontium", "Sr", false);
    public static final Element Y = createAndRegister(39, 50, -1, null, "Yttrium", "Y", false);
    public static final Element Zr = createAndRegister(40, 51, -1, null, "Zirconium", "Zr", false);
    public static final Element Nb = createAndRegister(41, 53, -1, null, "Niobium", "Nb", false);
    public static final Element Mo = createAndRegister(42, 53, -1, null, "Molybdenum", "Mo", false);
    public static final Element Tc = createAndRegister(43, 55, -1, null, "Technetium", "Tc", false);
    public static final Element Ru = createAndRegister(44, 57, -1, null, "Ruthenium", "Ru", false);
    public static final Element Rh = createAndRegister(45, 58, -1, null, "Rhodium", "Rh", false);
    public static final Element Pd = createAndRegister(46, 60, -1, null, "Palladium", "Pd", false);
    public static final Element Ag = createAndRegister(47, 60, -1, null, "Silver", "Ag", false);
    public static final Element Cd = createAndRegister(48, 64, -1, null, "Cadmium", "Cd", false);
    public static final Element In = createAndRegister(49, 65, -1, null, "Indium", "In", false);
    public static final Element Sn = createAndRegister(50, 68, -1, null, "Tin", "Sn", false);
    public static final Element Sb = createAndRegister(51, 70, -1, null, "Antimony", "Sb", false);
    public static final Element Te = createAndRegister(52, 75, -1, null, "Tellurium", "Te", false);
    public static final Element I = createAndRegister(53, 74, -1, null, "Iodine", "I", false);
    public static final Element Xe = createAndRegister(54, 77, -1, null, "Xenon", "Xe", false);
    public static final Element Cs = createAndRegister(55, 77, -1, null, "Caesium", "Cs", false);
    public static final Element Ba = createAndRegister(56, 81, -1, null, "Barium", "Ba", false);
    public static final Element La = createAndRegister(57, 81, -1, null, "Lanthanum", "La", false);
    public static final Element Ce = createAndRegister(58, 82, -1, null, "Cerium", "Ce", false);
    public static final Element Pr = createAndRegister(59, 81, -1, null, "Praseodymium", "Pr", false);
    public static final Element Nd = createAndRegister(60, 84, -1, null, "Neodymium", "Nd", false);
    public static final Element Pm = createAndRegister(61, 83, -1, null, "Promethium", "Pm", false);
    public static final Element Sm = createAndRegister(62, 88, -1, null, "Samarium", "Sm", false);
    public static final Element Eu = createAndRegister(63, 88, -1, null, "Europium", "Eu", false);
    public static final Element Gd = createAndRegister(64, 93, -1, null, "Gadolinium", "Gd", false);
    public static final Element Tb = createAndRegister(65, 93, -1, null, "Terbium", "Tb", false);
    public static final Element Dy = createAndRegister(66, 96, -1, null, "Dysprosium", "Dy", false);
    public static final Element Ho = createAndRegister(67, 97, -1, null, "Holmium", "Ho", false);
    public static final Element Er = createAndRegister(68, 99, -1, null, "Erbium", "Er", false);
    public static final Element Tm = createAndRegister(69, 99, -1, null, "Thulium", "Tm", false);
    public static final Element Yb = createAndRegister(70, 103, -1, null, "Ytterbium", "Yb", false);
    public static final Element Lu = createAndRegister(71, 103, -1, null, "Lutetium", "Lu", false);
    public static final Element Hf = createAndRegister(72, 106, -1, null, "Hafnium", "Hf", false);
    public static final Element Ta = createAndRegister(73, 107, -1, null, "Tantalum", "Ta", false);
    public static final Element W = createAndRegister(74, 109, -1, null, "Tungsten", "W", false);
    public static final Element Re = createAndRegister(75, 111, -1, null, "Rhenium", "Re", false);
    public static final Element Os = createAndRegister(76, 114, -1, null, "Osmium", "Os", false);
    public static final Element Ir = createAndRegister(77, 115, -1, null, "Iridium", "Ir", false);
    public static final Element Pt = createAndRegister(78, 117, -1, null, "Platinum", "Pt", false);
    public static final Element Au = createAndRegister(79, 117, -1, null, "Gold", "Au", false);
    public static final Element Hg = createAndRegister(80, 120, -1, null, "Mercury", "Hg", false);
    public static final Element Tl = createAndRegister(81, 123, -1, null, "Thallium", "Tl", false);
    public static final Element Pb = createAndRegister(82, 125, -1, null, "Lead", "Pb", false);
    public static final Element Bi = createAndRegister(83, 125, -1, null, "Bismuth", "Bi", false);
    public static final Element Po = createAndRegister(84, 124, -1, null, "Polonium", "Po", false);
    public static final Element At = createAndRegister(85, 124, -1, null, "Astatine", "At", false);
    public static final Element Rn = createAndRegister(86, 134, -1, null, "Radon", "Rn", false);
    public static final Element Fr = createAndRegister(87, 134, -1, null, "Francium", "Fr", false);
    public static final Element Ra = createAndRegister(88, 136, -1, null, "Radium", "Ra", false);
    public static final Element Ac = createAndRegister(89, 136, -1, null, "Actinium", "Ac", false);
    public static final Element Th = createAndRegister(90, 140, -1, null, "Thorium", "Th", false);
    public static final Element Pa = createAndRegister(91, 138, -1, null, "Protactinium", "Pa", false);
    public static final Element U = createAndRegister(92, 146, -1, null, "Uranium", "U", false);
    public static final Element U238 = createAndRegister(92, 146, -1, null, "Uranium-238", "U-238", false);
    public static final Element U235 = createAndRegister(92, 143, -1, null, "Uranium-235", "U-235", true);
    public static final Element Np = createAndRegister(93, 144, -1, null, "Neptunium", "Np", false);
    public static final Element Pu = createAndRegister(94, 152, -1, null, "Plutonium", "Pu", false);
    public static final Element Pu239 = createAndRegister(94, 145, -1, null, "Plutonium-239", "Pu-239", false);
    public static final Element Pu241 = createAndRegister(94, 149, -1, null, "Plutonium-241", "Pu-241", true);
    public static final Element Am = createAndRegister(95, 150, -1, null, "Americium", "Am", false);
    public static final Element Cm = createAndRegister(96, 153, -1, null, "Curium", "Cm", false);
    public static final Element Bk = createAndRegister(97, 152, -1, null, "Berkelium", "Bk", false);
    public static final Element Cf = createAndRegister(98, 153, -1, null, "Californium", "Cf", false);
    public static final Element Es = createAndRegister(99, 153, -1, null, "Einsteinium", "Es", false);
    public static final Element Fm = createAndRegister(100, 157, -1, null, "Fermium", "Fm", false);
    public static final Element Md = createAndRegister(101, 157, -1, null, "Mendelevium", "Md", false);
    public static final Element No = createAndRegister(102, 157, -1, null, "Nobelium", "No", false);
    public static final Element Lr = createAndRegister(103, 159, -1, null, "Lawrencium", "Lr", false);
    public static final Element Rf = createAndRegister(104, 161, -1, null, "Rutherfordium", "Rf", false);
    public static final Element Db = createAndRegister(105, 163, -1, null, "Dubnium", "Db", false);
    public static final Element Sg = createAndRegister(106, 165, -1, null, "Seaborgium", "Sg", false);
    public static final Element Bh = createAndRegister(107, 163, -1, null, "Bohrium", "Bh", false);
    public static final Element Hs = createAndRegister(108, 169, -1, null, "Hassium", "Hs", false);
    public static final Element Mt = createAndRegister(109, 167, -1, null, "Meitnerium", "Mt", false);
    public static final Element Ds = createAndRegister(110, 171, -1, null, "Darmstadtium", "Ds", false);
    public static final Element Rg = createAndRegister(111, 169, -1, null, "Roentgenium", "Rg", false);
    public static final Element Cn = createAndRegister(112, 173, -1, null, "Copernicium", "Cn", false);
    public static final Element Nh = createAndRegister(113, 171, -1, null, "Nihonium", "Nh", false);
    public static final Element Fl = createAndRegister(114, 175, -1, null, "Flerovium", "Fl", false);
    public static final Element Mc = createAndRegister(115, 173, -1, null, "Moscovium", "Mc", false);
    public static final Element Lv = createAndRegister(116, 177, -1, null, "Livermorium", "Lv", false);
    public static final Element Ts = createAndRegister(117, 177, -1, null, "Tennessine", "Ts", false);
    public static final Element Og = createAndRegister(118, 176, -1, null, "Oganesson", "Og", false);
    public static final Element Tr = createAndRegister(119, 178, -1, null, "Tritanium", "Tr", false);
    public static final Element Dr = createAndRegister(120, 180, -1, null, "Duranium", "Dr", false);
    public static final Element Ke = createAndRegister(125, 198, -1, null, "Trinium", "Ke", false);
    public static final Element Nq = createAndRegister(174, 352, 140, null, "Naquadah", "Nq", true);
    public static final Element Nq1 = createAndRegister(174, 354, 140, null, "NaquadahEnriched", "Nq+", true);
    public static final Element Nq2 = createAndRegister(174, 348, 140, null, "Naquadria", "*Nq*", true);
    public static final Element Nt = createAndRegister(0, 1000, -1, null, "Neutronium", "Nt", false);
    public static final Element Sp = createAndRegister(1, 0, -1, null, "Space", "Sp", false);
    public static final Element Ma = createAndRegister(1, 0, -1, null, "Magic", "Ma", false);

    public static Element createAndRegister(long protons, long neutrons, long halfLifeSeconds, String decayTo,
                                            String name, String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        GTRegistries.ELEMENTS.register(name, element);
        return element;
    }

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerElements);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.ELEMENTS, Element.class));
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.ELEMENTS.getRegistryName());
        }
        GTRegistries.ELEMENTS.freeze();
    }

    public static Element get(String name) {
        return GTRegistries.ELEMENTS.get(name);
    }
}
