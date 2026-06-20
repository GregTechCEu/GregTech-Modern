package com.gregtechceu.gtceu.integration.forestry.items;

import forestry.api.core.IBlockSubtype;
import forestry.api.core.IItemSubtype;
import net.minecraft.util.StringRepresentable;


import java.util.Locale;

public enum GTCombType implements StringRepresentable, IItemSubtype, IBlockSubtype {

    //todo make "comb" a material property
    // todo use mui for colors

    // Organic
    COAL(new Color(0x525252), new Color(0x666666)),
    COKE(new Color(0x4B4B4B), new Color(0x7D7D7D)),
    STICKY(new Color(0x2E8F5B), new Color(0xDCC289)),
    OIL(new Color(0x333333), new Color(0x4C4C4C)),
    APATITE(new Color(0xC1C1F6), new Color(0x676784)),
    ASH(new Color(0x1E1A18), new Color(0xC6C6C6)),
    BIOMASS(new Color(0x17AF0E), new Color(0x21E118)),
    PHOSPHORUS(new Color(0xC1C1F6), new Color(0xFFC826)),

    // Industrial
    ENERGY(new Color(0xC11F1F), new Color(0xEBB9B9)),
    LAPOTRON(new Color(0x1414FF), new Color(0x6478FF)),

    // Alloy
    REDALLOY(new Color(0xE60000), new Color(0xB80000)),
    STAINLESSSTEEL(new Color(0x778899), new Color(0xC8C8DC)),

    // Gem
    STONE(new Color(0x808080), new Color(0x999999)),
    CERTUS(new Color(0x57CFFB), new Color(0xBBEEFF)),
    REDSTONE(new Color(0x7D0F0F), new Color(0xD11919)),
    RAREEARTH(new Color(0x555643), new Color(0x343428)),
    LAPIS(new Color(0x1947D1), new Color(0x476CDA)),
    RUBY(new Color(0xE6005C), new Color(0xCC0052)),
    SAPPHIRE(new Color(0x0033CC), new Color(0x00248F)),
    DIAMOND(new Color(0xCCFFFF), new Color(0xA3CCCC)),
    OLIVINE(new Color(0x248F24), new Color(0xCCFFCC)),
    EMERALD(new Color(0x248F24), new Color(0x2EB82E)),
    PYROPE(new Color(0x763162), new Color(0x8B8B8B)),
    GROSSULAR(new Color(0x9B4E00), new Color(0x8B8B8B)),

    // Metal
    SLAG(new Color(0xD4D4D4), new Color(0x58300B)),
    COPPER(new Color(0xFF6600), new Color(0xE65C00)),
    TIN(new Color(0xD4D4D4), new Color(0xDDDDDD)),
    LEAD(new Color(0x666699), new Color(0xA3A3CC)),
    IRON(new Color(0xDA9147), new Color(0xDE9C59)),
    STEEL(new Color(0x808080), new Color(0x999999)),
    NICKEL(new Color(0x8585AD), new Color(0x9D9DBD)),
    ZINC(new Color(0xF0DEF0), new Color(0xF2E1F2)),
    SILVER(new Color(0xC2C2D6), new Color(0xCECEDE)),
    GOLD(new Color(0xE6B800), new Color(0xCFA600)),
    SULFUR(new Color(0x6F6F01), new Color(0x8B8B8B)),
    GALLIUM(new Color(0x8B8B8B), new Color(0xC5C5E4)),
    ARSENIC(new Color(0x736C52), new Color(0x292412)),

    // Rare Metal
    BAUXITE(new Color(0x6B3600), new Color(0x8B8B8B)),
    ALUMINIUM(new Color(0x008AB8), new Color(0xD6D6FF)),
    MANGANESE(new Color(0xD5D5D5), new Color(0xCDE1B9)),
    MAGNESIUM(new Color(0xF1D9D9), new Color(0x8B8B8B)),
    TITANIUM(new Color(0xCC99FF), new Color(0xDBB8FF)),
    CHROME(new Color(0xEBA1EB), new Color(0xF2C3F2)),
    TUNGSTEN(new Color(0x62626D), new Color(0x161620)),
    PLATINUM(new Color(0xE6E6E6), new Color(0xFFFFCC)),
    IRIDIUM(new Color(0xDADADA), new Color(0xA1E4E4)),
    MOLYBDENUM(new Color(0xAEAED4), new Color(0x8B8B8B)),
    OSMIUM(new Color(0x2B2BDA), new Color(0x8B8B8B)),
    LITHIUM(new Color(0xF0328C), new Color(0xE1DCFF)),
    SALT(new Color(0xF0C8C8), new Color(0xFAFAFA)),
    ELECTROTINE(new Color(0x1E90FF), new Color(0x3CB4C8)),
    ALMANDINE(new Color(0xC60000), new Color(0x8B8B8B)),
    INDIUM(new Color(0x8F5D99), new Color(0xFFA9FF)),

    // Radioactive
    URANIUM(new Color(0x19AF19), new Color(0x169E16)),
    PLUTONIUM(new Color(0x240000), new Color(0x570000)),
    NAQUADAH(new Color(0x000000), new Color(0x004400)),
    NAQUADRIA(new Color(0x000000), new Color(0x002400)),
    TRINIUM(new Color(0x9973BD), new Color(0xC8C8D2)),
    THORIUM(new Color(0x001E00), new Color(0x005000)),
    LUTETIUM(new Color(0x0059FF), new Color(0x00AAFF)),
    AMERICIUM(new Color(0x0C453A), new Color(0x287869)),
    NEUTRONIUM(new Color(0xFFF0F0), new Color(0xFAFAFA)),

    // Noble Gas
    HELIUM(new Color(0xFFA9FF), new Color(0xFFFFC3)),
    ARGON(new Color(0x00FF00), new Color(0x160822)),
    XENON(new Color(0x160822), new Color(0x8A97B0)),
    NEON(new Color(0xFAB4B4), new Color(0xFFC826)),
    KRYPTON(new Color(0x80FF80), new Color(0xFFFFC3)),
    NITROGEN(new Color(0x00BFC1), new Color(0xFFFFFF)),
    OXYGEN(new Color(0x8F8FFF), new Color(0xFFFFFF)),
    HYDROGEN(new Color(0x0000B5), new Color(0xFFFFFF)),
    FLUORINE(new Color(0xFF6D00), new Color(0x86AFF0));

    public static final GTCombType[] VALUES = values();

    public final String name;
    public final int primaryColor;
    public final int secondaryColor;

    GTCombType(Color primary, Color secondary) {
        this(primary, secondary, null);
    }

    GTCombType(Color primary, Color secondary, String compatName) {
        this.name = toString().toLowerCase(Locale.ROOT);
        this.primaryColor = primary.getRGB();
        this.secondaryColor = secondary.getRGB();
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    //todo do not reference a concept of "metadata" anywhere. If this is used, change how it works.
    public static GTCombType get(int meta) {
        if (meta >= VALUES.length) {
            meta = 0;
        }
        return VALUES[meta];
    }
}
