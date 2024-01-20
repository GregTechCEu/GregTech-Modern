package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class HigherDegreeMaterials {

    public static void register() {

        Electrotine = new Material.Builder(GTCEu.id("electrotine"))
                .dust().ore(5, 1, true)
                .color(0x83cbf5).secondaryColor(0x004585).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Redstone, 1, Electrum, 1)
                .buildAndRegister();

        EnderEye = new Material.Builder(GTCEu.id("ender_eye"))
                .gem(1)
                .color(0xb5e45a).secondaryColor(0x001430).iconSet(SHINY)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        Diatomite = new Material.Builder(GTCEu.id("diatomite"))
                .dust(1).ore()
                .color(0xfffafa)
                .components(Flint, 8, Hematite, 1, Sapphire, 1)
                .buildAndRegister();

        RedSteel = new Material.Builder(GTCEu.id("red_steel"))
                .ingot(3).fluid()
                .color(0xa09191).secondaryColor(0x500404).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_GEAR)
                .components(SterlingSilver, 1, BismuthBronze, 1, Steel, 2, BlackSteel, 4)
                .toolStats(ToolProperty.Builder.of(7.0F, 6.0F, 2560, 3)
                        .attackSpeed(0.1F).enchantability(21).build())
                .blastTemp(1300, GasTier.LOW, GTValues.VA[GTValues.HV], 1000)
                .buildAndRegister();

        BlueSteel = new Material.Builder(GTCEu.id("blue_steel"))
                .ingot(3).fluid()
                .color(0xa5bdda).secondaryColor(0x24245f).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FRAME, GENERATE_GEAR)
                .components(RoseGold, 1, Brass, 1, Steel, 2, BlackSteel, 4)
                .toolStats(ToolProperty.Builder.of(15.0F, 6.0F, 1024, 3)
                        .attackSpeed(0.1F).enchantability(33).build())
                .blastTemp(1400, GasTier.LOW, GTValues.VA[GTValues.HV], 1000)
                .buildAndRegister();

        Basalt = new Material.Builder(GTCEu.id("basalt"))
                .dust(1)
                .color(0x5c5c5c).secondaryColor(0x1b2632).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Olivine, 1, Calcite, 3, Flint, 8, DarkAsh, 4)
                .buildAndRegister();

        GraniticMineralSand = new Material.Builder(GTCEu.id("granitic_mineral_sand"))
                .dust(1).ore()
                .color(0xd69077).secondaryColor(0x71352c).iconSet(SAND)
                .components(Magnetite, 1, Deepslate, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .buildAndRegister();

        Redrock = new Material.Builder(GTCEu.id("redrock"))
                .dust(1)
                .color(0xffa49e).secondaryColor(0x52362a).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 2, Flint, 1)
                .buildAndRegister();

        GarnetSand = new Material.Builder(GTCEu.id("garnet_sand"))
                .dust(1).ore()
                .color(0xcc4c25).secondaryColor(0x510b04).iconSet(SAND)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Almandine, 1, Andradite, 1, Grossular, 1, Pyrope, 1, Spessartine, 1, Uvarovite, 1)
                .buildAndRegister();

        HSSG = new Material.Builder(GTCEu.id("hssg"))
                .ingot(3).fluid()
                .color(0x9cbabe).secondaryColor(0x032550).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_FOIL, GENERATE_GEAR)
                .components(TungstenSteel, 5, Chromium, 1, Molybdenum, 2, Vanadium, 1)
                .rotorStats(10.0f, 5.5f, 4000)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4200, GasTier.MID, GTValues.VA[GTValues.EV], 1300)
                .buildAndRegister();

        RedAlloy = new Material.Builder(GTCEu.id("red_alloy"))
                .ingot(0)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1400))
                .color(0xc55252).secondaryColor(0xC80000).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Copper, 1, Redstone, 4)
                .cableProperties(GTValues.V[0], 1, 0)
                .buildAndRegister();

        BasalticMineralSand = new Material.Builder(GTCEu.id("basaltic_mineral_sand"))
                .dust(1).ore()
                .color(0x5c5c5c).secondaryColor(0x283228).iconSet(SAND)
                .components(Magnetite, 1, Basalt, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .buildAndRegister();

        HSSE = new Material.Builder(GTCEu.id("hsse"))
                .ingot(4).fluid()
                .color(0x9d9cbe).secondaryColor(0x2b0350).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING)
                .components(HSSG, 6, Cobalt, 1, Manganese, 1, Silicon, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 10.0F, 3072, 4)
                        .attackSpeed(0.3F).enchantability(33).build())
                .rotorStats(10.0f, 8.0f, 5120)
                .blastTemp(5000, GasTier.HIGH, GTValues.VA[GTValues.EV], 1400)
                .buildAndRegister();

        HSSS = new Material.Builder(GTCEu.id("hsss"))
                .ingot(4).fluid()
                .color(0xae9abe).secondaryColor(0x66000e).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_ROTOR, GENERATE_ROUND, GENERATE_FOIL, GENERATE_GEAR)
                .components(HSSG, 6, Iridium, 2, Osmium, 1)
                .rotorStats(15.0f, 7.0f, 3000)
                .blastTemp(5000, GasTier.HIGH, GTValues.VA[GTValues.EV], 1500)
                .buildAndRegister();

        IridiumMetalResidue = new Material.Builder(GTCEu.id("iridium_metal_residue"))
                .dust()
                .color(0x5C5D68).secondaryColor(0x462941).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Chlorine, 3, PlatinumSludgeResidue, 1)
                .buildAndRegister();

        Granite = new Material.Builder(GTCEu.id("granite"))
                .dust()
                .color(0xd69077).secondaryColor(0x71352c).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Redrock, 1)
                .buildAndRegister();

        Brick = new Material.Builder(GTCEu.id("brick"))
                .dust()
                .color(0xc76245).secondaryColor(0x2d1610).iconSet(ROUGH)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Clay, 1)
                .buildAndRegister();

        Fireclay = new Material.Builder(GTCEu.id("fireclay"))
                .dust()
                .color(0xffeab6).secondaryColor(0x84581c).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, NO_SMELTING)
                .components(Clay, 1, Brick, 1)
                .buildAndRegister();

        Diorite = new Material.Builder(GTCEu.id("diorite"))
                .dust()
                .color(0xe9e9e9).secondaryColor(0x7b7b7b)
                .iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mirabilite, 2, Clay, 7)
                .buildAndRegister();

        BlueAlloy = new Material.Builder(GTCEu.id("blue_alloy"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1400))
                .color(0x64B4FF).iconSet(METALLIC)
                .flags(GENERATE_PLATE, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Electrotine, 4, Silver, 1)
                .cableProperties(GTValues.V[GTValues.HV], 2, 1)
                .buildAndRegister();
    }
}
