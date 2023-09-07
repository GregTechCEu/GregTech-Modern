package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GCMBMaterials {

    public static void register() {

        TantalumCarbide = new Material.Builder("tantalum_carbide")
                .ingot(4).fluid()
                .color(0x999900).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, BLAST_ALLOY_CRAFTABLE)
                .components(Tantalum, 1, Carbon, 1)
                .blastTemp(4120, GasTier.MID, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();

        HSLASteel = new Material.Builder("hsla_steel")
                .ingot(3).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME, BLAST_ALLOY_CRAFTABLE)
                .components(Invar, 2, Vanadium, 1, Titanium, 1, Molybdenum, 1)
                .blastTemp(1711, GasTier.LOW, GTValues.VA[GTValues.HV], 1000)
                .buildAndRegister();

        MolybdenumDisilicide = new Material.Builder("molybdenum_disilicide")
                .ingot(2).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_SPRING, BLAST_ALLOY_CRAFTABLE)
                .components(Molybdenum, 1, Silicon, 2)
                .blastTemp(2300, GasTier.MID, GTValues.VA[GTValues.EV], 800)
                .buildAndRegister();

        Zeron100 = new Material.Builder("zeron_100")
                .ingot(5).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, BLAST_ALLOY_CRAFTABLE)
                .components(Iron, 10, Nickel, 2, Tungsten, 2, Niobium, 1, Cobalt, 1)
                .blastTemp(3693, GasTier.MID, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();

        WatertightSteel = new Material.Builder("watertight_steel")
                .ingot(4)
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME)
                .components(Iron, 7, Aluminium, 4, Nickel, 2, Chromium, 1, Sulfur, 1)
                .blastTemp(3850, GasTier.MID, GTValues.VA[GTValues.EV], 800)
                .buildAndRegister();

        IncoloyMA956 = new Material.Builder("incoloy_ma_956")
                .ingot(5).fluid()
                .color(0x2D9B66).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME, BLAST_ALLOY_CRAFTABLE)
                .components(VanadiumSteel, 4, Manganese, 2, Aluminium, 5, Yttrium, 2)
                .blastTemp(3652, GasTier.MID, GTValues.VA[GTValues.EV], 800)
                .buildAndRegister();

        MaragingSteel300 = new Material.Builder("maraging_steel_300")
                .ingot(4).fluid()
                .color(0x2B4B56).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_ROD, GENERATE_FRAME, BLAST_ALLOY_CRAFTABLE)
                .components(Iron, 16, Titanium, 1, Aluminium, 1, Nickel, 4, Cobalt, 2)
                .blastTemp(4000, GasTier.HIGH, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();

        HastelloyX = new Material.Builder("hastelloy_x")
                .ingot(5).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE,  GENERATE_FRAME, BLAST_ALLOY_CRAFTABLE)
                .components(Nickel, 8, Iron, 3, Tungsten, 4, Molybdenum, 2, Chromium, 1, Niobium, 1)
                .blastTemp(4200, GasTier.HIGH, GTValues.VA[GTValues.EV], 900)
                .buildAndRegister();

        Stellite100 = new Material.Builder("stellite_100")
                .ingot(4).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, BLAST_ALLOY_CRAFTABLE)
                .components(Iron, 4, Chromium, 3, Tungsten, 2, Molybdenum, 1)
                .blastTemp(3790, GasTier.HIGH, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();

        TitaniumCarbide = new Material.Builder("titanium_carbide")
                .ingot(3).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, BLAST_ALLOY_CRAFTABLE)
                .components(Titanium, 1, Carbon, 1)
                .blastTemp(3430, GasTier.MID, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();

        TitaniumTungstenCarbide = new Material.Builder("titanium_tungsten_carbide")
                .ingot(6).fluid()
                .color(0x294972).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_PLATE, BLAST_ALLOY_CRAFTABLE)
                .components(TitaniumCarbide, 2, TungstenCarbide, 1)
                .blastTemp(3800, GasTier.HIGH, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();
    }
}
