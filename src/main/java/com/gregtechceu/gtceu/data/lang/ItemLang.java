package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(GTLangProvider provider) {
        initGeneratedNames(provider);
        initItemNames(provider);
        initItemTooltips(provider);
        generateBehaviorKeys(provider);

        provider.add("item.invalid.name", "Invalid item");
    }

    private static void initGeneratedNames(GTLangProvider provider) {
        // All TagPrefixes
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            provider.add(tagPrefix.getUnlocalizedName(), tagPrefix.langValue);
        }

        // All GTToolTypes
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            provider.add(toolType.getUnlocalizedName(), toEnglishName(toolType.name));
        }

        // Tag Prefix
        provider.add("tagprefix.polymer.plate", "%s Sheet");
        provider.add("tagprefix.polymer.foil", "Thin %s Sheet");
        provider.add("tagprefix.polymer.nugget", "%s Chip");
        provider.add("tagprefix.polymer.dense_plate", "Dense %s Sheet");
        provider.add("tagprefix.polymer.double_plate", "Double %s Sheet");
        provider.add("tagprefix.polymer.tiny_dust", "Tiny Pile of %s Pulp");
        provider.add("tagprefix.polymer.small_dust", "Small Pile of %s Pulp");
        provider.add("tagprefix.polymer.dust", "%s Pulp");
        provider.add("tagprefix.polymer.ingot", "%s Ingot");

        // Material Items
        provider.add("item.gtceu.tiny_gunpowder_dust", "Tiny Pile of Gunpowder");
        provider.add("item.gtceu.small_gunpowder_dust", "Small Pile of Gunpowder");

        provider.add("item.gtceu.tiny_paper_dust", "Tiny Pile of Chad");
        provider.add("item.gtceu.small_paper_dust", "Small Pile of Chad");
        provider.add("item.gtceu.paper_dust", "Chad");

        provider.add("item.gtceu.tiny_rare_earth_dust", "Tiny Pile of Rare Earth");
        provider.add("item.gtceu.small_rare_earth_dust", "Small Pile of Rare Earth");
        provider.add("item.gtceu.rare_earth_dust", "Rare Earth");

        provider.add("item.gtceu.tiny_ash_dust", "Tiny Pile of Ashes");
        provider.add("item.gtceu.small_ash_dust", "Small Pile of Ashes");
        provider.add("item.gtceu.ash_dust", "Ashes");

        provider.add("item.gtceu.tiny_bone_dust", "Tiny Pile of Bone Meal");
        provider.add("item.gtceu.small_bone_dust", "Small Pile of Bone Meal");
        provider.add("item.gtceu.bone_dust", "Bone Meal");

        provider.add("item.gtceu.refined_cassiterite_sand_ore", "Refined Cassiterite Sand");
        provider.add("item.gtceu.purified_cassiterite_sand_ore", "Purified Cassiterite Sand");
        provider.add("item.gtceu.crushed_cassiterite_sand_ore", "Ground Cassiterite Sand");
        provider.add("item.gtceu.tiny_cassiterite_sand_dust", "Tiny Pile of Cassiterite Sand");
        provider.add("item.gtceu.small_cassiterite_sand_dust", "Small Pile of Cassiterite Sand");
        provider.add("item.gtceu.impure_cassiterite_sand_dust", "Impure Pile of Cassiterite Sand");
        provider.add("item.gtceu.pure_cassiterite_sand_dust", "Purified Pile of Cassiterite Sand");
        provider.add("item.gtceu.cassiterite_sand_dust", "Cassiterite Sand");

        provider.add("item.gtceu.tiny_dark_ash_dust", "Tiny Pile of Dark Ashes");
        provider.add("item.gtceu.small_dark_ash_dust", "Small Pile of Dark Ashes");
        provider.add("item.gtceu.dark_ash_dust", "Dark Ashes");

        provider.add("item.gtceu.tiny_ice_dust", "Tiny Pile of Crushed Ice");
        provider.add("item.gtceu.small_ice_dust", "Small Pile of Crushed Ice");
        provider.add("item.gtceu.ice_dust", "Crushed Ice");

        provider.add("item.gtceu.sugar_gem", "Sugar Cube");
        provider.add("item.gtceu.chipped_sugar_gem", "Small Sugar Cubes");
        provider.add("item.gtceu.flawed_sugar_gem", "Tiny Sugar Cube");

        provider.add("item.gtceu.tiny_rock_salt_dust", "Tiny Pile of Rock Salt");
        provider.add("item.gtceu.small_rock_salt_dust", "Small Pile of Rock Salt");
        provider.add("item.gtceu.impure_rock_salt_dust", "Impure Pile of Rock Salt");
        provider.add("item.gtceu.pure_rock_salt_dust", "Purified Pile of Rock Salt");
        provider.add("item.gtceu.rock_salt_dust", "Rock Salt");

        provider.add("item.gtceu.tiny_salt_dust", "Tiny Pile of Salt");
        provider.add("item.gtceu.small_salt_dust", "Small Pile of Salt");
        provider.add("item.gtceu.impure_salt_dust", "Impure Pile of Salt");
        provider.add("item.gtceu.pure_salt_dust", "Purified Pile of Salt");
        provider.add("item.gtceu.salt_dust", "Salt");

        provider.add("item.gtceu.tiny_wood_dust", "Tiny Pile of Wood Pulp");
        provider.add("item.gtceu.small_wood_dust", "Small Pile of Wood Pulp");
        provider.add("item.gtceu.wood_dust", "Wood Pulp");
        provider.add("item.gtceu.wood_plate", "Wood Plank");
        provider.add("item.gtceu.long_wood_rod", "Long Wood Stick");
        provider.add("item.gtceu.wood_bolt", "Short Wood Stick");

        provider.add("item.gtceu.tiny_treated_wood_dust", "Tiny Pile of Treated Wood Pulp");
        provider.add("item.gtceu.small_treated_wood_dust", "Small Pile of Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_dust", "Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_plate", "Treated Wood Plank");
        provider.add("item.gtceu.treated_wood_rod", "Treated Wood Stick");
        provider.add("item.gtceu.long_treated_wood_rod", "Long Treated Wood Stick");
        provider.add("item.gtceu.treated_wood_bolt", "Short Treated Wood Stick");

        provider.add("item.gtceu.glass_gem", "Glass Crystal");
        provider.add("item.gtceu.chipped_glass_gem", "Chipped Glass Crystal");
        provider.add("item.gtceu.flawed_glass_gem", "Flawed Glass Crystal");
        provider.add("item.gtceu.flawless_glass_gem", "Flawless Glass Crystal");
        provider.add("item.gtceu.exquisite_glass_gem", "Exquisite Glass Crystal");
        provider.add("item.gtceu.glass_plate", "Glass Pane");
        provider.add("item.gtceu.glass_lens", "Glass Lens (White)");

        provider.add("item.gtceu.tiny_blaze_dust", "Tiny Pile of Blaze Powder");
        provider.add("item.gtceu.small_blaze_dust", "Small Pile of Blaze Powder");

        provider.add("item.gtceu.tiny_sugar_dust", "Tiny Pile of Sugar");
        provider.add("item.gtceu.small_sugar_dust", "Small Pile of Sugar");

        provider.add("item.gtceu.tiny_basaltic_mineral_sand_dust", "Tiny Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.small_basaltic_mineral_sand_dust", "Small Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.basaltic_mineral_sand_dust", "Basaltic Mineral Sand");

        provider.add("item.gtceu.tiny_granitic_mineral_sand_dust", "Tiny Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.small_granitic_mineral_sand_dust", "Small Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.granitic_mineral_sand_dust", "Granitic Mineral Sand");

        provider.add("item.gtceu.tiny_garnet_sand_dust", "Tiny Pile of Garnet Sand");
        provider.add("item.gtceu.small_garnet_sand_dust", "Small Pile of Garnet Sand");
        provider.add("item.gtceu.garnet_sand_dust", "Garnet Sand");

        provider.add("item.gtceu.tiny_quartz_sand_dust", "Tiny Pile of Quartz Sand");
        provider.add("item.gtceu.small_quartz_sand_dust", "Small Pile of Quartz Sand");
        provider.add("item.gtceu.quartz_sand_dust", "Quartz Sand");

        provider.add("item.gtceu.tiny_glauconite_sand_dust", "Tiny Pile of Glauconite Sand");
        provider.add("item.gtceu.small_glauconite_sand_dust", "Small Pile of Glauconite Sand");
        provider.add("item.gtceu.glauconite_sand_dust", "Glauconite Sand");

        provider.add("item.gtceu.refined_bentonite_ore", "Refined Bentonite");
        provider.add("item.gtceu.purified_bentonite_ore", "Purified Bentonite");
        provider.add("item.gtceu.crushed_bentonite_ore", "Ground Bentonite");
        provider.add("item.gtceu.tiny_bentonite_dust", "Tiny Pile of Bentonite");
        provider.add("item.gtceu.small_bentonite_dust", "Small Pile of Bentonite");
        provider.add("item.gtceu.impure_bentonite_dust", "Impure Pile of Bentonite");
        provider.add("item.gtceu.pure_bentonite_dust", "Purified Pile of Bentonite");
        provider.add("item.gtceu.bentonite_dust", "Bentonite");

        provider.add("item.gtceu.tiny_fullers_earth_dust", "Tiny Pile of Fullers Earth");
        provider.add("item.gtceu.small_fullers_earth_dust", "Small Pile of Fullers Earth");
        provider.add("item.gtceu.fullers_earth_dust", "Fullers Earth");

        provider.add("item.gtceu.refined_pitchblende_ore", "Refined Pitchblende");
        provider.add("item.gtceu.purified_pitchblende_ore", "Purified Pitchblende");
        provider.add("item.gtceu.crushed_pitchblende_ore", "Ground Pitchblende");
        provider.add("item.gtceu.tiny_pitchblende_dust", "Tiny Pile of Pitchblende");
        provider.add("item.gtceu.small_pitchblende_dust", "Small Pile of Pitchblende");
        provider.add("item.gtceu.impure_pitchblende_dust", "Impure Pile of Pitchblende");
        provider.add("item.gtceu.pure_pitchblende_dust", "Purified Pile of Pitchblende");
        provider.add("item.gtceu.pitchblende_dust", "Pitchblende");

        provider.add("item.gtceu.refined_talc_ore", "Refined Talc");
        provider.add("item.gtceu.purified_talc_ore", "Purified Talc");
        provider.add("item.gtceu.crushed_talc_ore_ore", "Ground Talc");
        provider.add("item.gtceu.tiny_talc_dust", "Tiny Pile of Talc");
        provider.add("item.gtceu.small_talc_dust", "Small Pile of Talc");
        provider.add("item.gtceu.impure_talc_dust", "Impure Pile of Talc");
        provider.add("item.gtceu.pure_talc_dust", "Purified Pile of Talc");
        provider.add("item.gtceu.talc_dust", "Talc");

        provider.add("item.gtceu.tiny_wheat_dust", "Tiny Pile of Flour");
        provider.add("item.gtceu.small_wheat_dust", "Small Pile of Flour");
        provider.add("item.gtceu.wheat_dust", "Flour");

        provider.add("item.gtceu.tiny_meat_dust", "Tiny Pile of Mince Meat");
        provider.add("item.gtceu.small_meat_dust", "Small Pile of Mince Meat");
        provider.add("item.gtceu.meat_dust", "Mince Meat");

        provider.add("item.gtceu.borosilicate_glass_ingot", "Borosilicate Glass Bar");
        provider.add("item.gtceu.fine_borosilicate_glass_wire", "Borosilicate Glass Fibers");

        provider.add("item.gtceu.tiny_platinum_group_sludge_dust", "Tiny Clump of Platinum Group Sludge");
        provider.add("item.gtceu.small_platinum_group_sludge_dust", "Small Clump of Platinum Group Sludge");
        provider.add("item.gtceu.platinum_group_sludge_dust", "Platinum Group Sludge");

        provider.add("item.gtceu.tiny_platinum_raw_dust", "Tiny Pile of Raw Platinum Powder");
        provider.add("item.gtceu.small_platinum_raw_dust", "Small Pile of Raw Platinum Powder");
        provider.add("item.gtceu.platinum_raw_dust", "Raw Platinum Powder");

        provider.add("item.gtceu.tiny_palladium_raw_dust", "Tiny Pile of Raw Palladium Powder");
        provider.add("item.gtceu.small_palladium_raw_dust", "Small Pile of Raw Palladium Powder");
        provider.add("item.gtceu.palladium_raw_dust", "Raw Palladium Powder");

        provider.add("item.gtceu.tiny_inert_metal_mixture_dust", "Tiny Pile of Inert Metal Mixture");
        provider.add("item.gtceu.small_inert_metal_mixture_dust", "Small Pile of Inert Metal Mixture");
        provider.add("item.gtceu.inert_metal_mixture_dust", "Inert Metal Mixture");

        provider.add("item.gtceu.tiny_rarest_metal_mixture_dust", "Tiny Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.small_rarest_metal_mixture_dust", "Small Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.rarest_metal_mixture_dust", "Rarest Metal Mixture");

        provider.add("item.gtceu.tiny_platinum_sludge_residue_dust", "Tiny Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.small_platinum_sludge_residue_dust", "Small Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.platinum_sludge_residue_dust", "Platinum Sludge Residue");

        provider.add("item.gtceu.tiny_iridium_metal_residue_dust", "Tiny Pile of Iridium Metal Residue");
        provider.add("item.gtceu.small_iridium_metal_residue_dust", "Small Pile of Iridium Metal Residue");
        provider.add("item.gtceu.iridium_metal_residue_dust", "Iridium Metal Residue");
    }

    private static void initItemNames(GTLangProvider provider) {
        provider.add("item.gtceu.tungsten_steel_fluid_cell", "%s Tungstensteel Cell");
    }

    private static void initItemTooltips(GTLangProvider provider) {
        // Nano Saber
        provider.add("item.gtceu.nano_saber.tooltip", "§7Ryujin no ken wo kurae!");

        // Casting Molds
        provider.add("item.gtceu.empty_mold.tooltip", "§7Raw Plate to make Molds and Extrude Shapes");
        provider.add("item.gtceu.plate_casting_mold.tooltip", "§7Mold for making Plates");
        provider.add("item.gtceu.casing_casting_mold.tooltip", "§7Mold for making Item Casings");
        provider.add("item.gtceu.gear_casting_mold.tooltip", "§7Mold for making Gears");
        provider.add("item.gtceu.bottle_casting_mold.tooltip", "§7Mold for making Bottles");
        provider.add("item.gtceu.ingot_casting_mold.tooltip", "§7Mold for making Ingots");
        provider.add("item.gtceu.ball_casting_mold.tooltip", "§7Mold for making Balls");
        provider.add("item.gtceu.block_casting_mold.tooltip", "§7Mold for making Blocks");
        provider.add("item.gtceu.nugget_casting_mold.tooltip", "§7Mold for making Nuggets");
        provider.add("item.gtceu.cylinder_casting_mold.tooltip", "§7Mold for shaping Cylinders");
        provider.add("item.gtceu.anvil_casting_mold.tooltip", "§7Mold for shaping Anvils");
        provider.add("item.gtceu.name_casting_mold.tooltip",
                "§7Mold for naming Items in the Forming Press (rename Mold with Anvil)");
        provider.add("item.gtceu.gear_casting_mold.small.tooltip", "§7Mold for making small Gears");
        provider.add("item.gtceu.rotor_casting_mold.tooltip", "§7Mold for making Rotors");

        // Extruder Shapes
        provider.add("item.gtceu.plate_extruder_mold.tooltip", "§7Extruder Shape for making Plates");
        provider.add("item.gtceu.rod_extruder_mold.tooltip", "§7Extruder Shape for making Rods");
        provider.add("item.gtceu.bolt_extruder_mold.tooltip", "§7Extruder Shape for making Bolts");
        provider.add("item.gtceu.ring_extruder_mold.tooltip", "§7Extruder Shape for making Rings");
        provider.add("item.gtceu.cell_extruder_mold.tooltip", "§7Extruder Shape for making Cells");
        provider.add("item.gtceu.ingot_extruder_mold.tooltip",
                "§7Extruder Shape for, wait, can't we just use a Furnace?");
        provider.add("item.gtceu.wire_extruder_mold.tooltip", "§7Extruder Shape for making Wires");
        provider.add("item.gtceu.casing_extruder_mold.tooltip", "§7Extruder Shape for making Item Casings");
        provider.add("item.gtceu.pipe.tiny_extruder_mold.tooltip", "§7Extruder Shape for making tiny Pipes");
        provider.add("item.gtceu.pipe.small_extruder_mold.tooltip", "§7Extruder Shape for making small Pipes");
        provider.add("item.gtceu.pipe.normal_extruder_mold.tooltip", "§7Extruder Shape for making Pipes");
        provider.add("item.gtceu.pipe.large_extruder_mold.tooltip", "§7Extruder Shape for making large Pipes");
        provider.add("item.gtceu.pipe.huge_extruder_mold.tooltip", "§7Extruder Shape for making full Block Pipes");
        provider.add("item.gtceu.block_extruder_mold.tooltip", "§7Extruder Shape for making Blocks");
        provider.add("item.gtceu.sword_extruder_mold.tooltip", "§7Extruder Shape for making Swords");
        provider.add("item.gtceu.pickaxe_extruder_mold.tooltip", "§7Extruder Shape for making Pickaxes");
        provider.add("item.gtceu.shovel_extruder_mold.tooltip", "§7Extruder Shape for making Shovels");
        provider.add("item.gtceu.axe_extruder_mold.tooltip", "§7Extruder Shape for making Axes");
        provider.add("item.gtceu.hoe_extruder_mold.tooltip", "§7Extruder Shape for making Hoes");
        provider.add("item.gtceu.hammer_extruder_mold.tooltip", "§7Extruder Shape for making Hammers");
        provider.add("item.gtceu.file_extruder_mold.tooltip", "§7Extruder Shape for making Files");
        provider.add("item.gtceu.saw_extruder_mold.tooltip", "§7Extruder Shape for making Saws");
        provider.add("item.gtceu.gear_extruder_mold.tooltip", "§7Extruder Shape for making Gears");
        provider.add("item.gtceu.bottle_extruder_mold.tooltip", "§7Extruder Shape for making Bottles");
        provider.add("item.gtceu.gear_small_extruder_mold.tooltip", "§7Extruder Shape for making Small Gears");
        provider.add("item.gtceu.foil_extruder_mold.tooltip", "§7Extruder Shape for making Foils from Non-Metals");
        provider.add("item.gtceu.rod_long_extruder_mold.tooltip", "§7Extruder Shape for making Long Rods");
        provider.add("item.gtceu.rotor_extruder_mold.tooltip", "§7Extruder Shape for making Rotors");

        provider.add("item.gtceu.empty_spray_can.tooltip", "§7Can be filled with sprays of various colors");

        provider.add("fluid_cell.empty", "Empty");

        // Lighters
        provider.add("item.gtceu.tool.matchbox.tooltip", "§7This is not a Car");
        provider.add("item.gtceu.tool.lighter.platinum.tooltip", "§7A known Prank Master is engraved on it");

        // Batteries
        provider.add("item.gtceu.lv_battery_hull.tooltip", "§7An empty LV Battery Hull");
        provider.add("item.gtceu.mv_battery_hull.tooltip", "§7An empty §bMV §7Battery Hull");
        provider.add("item.gtceu.hv_battery_hull.tooltip", "§7An empty §6HV §7Battery Hull");
        provider.add("item.gtceu.ev_battery_hull.tooltip", "§7An empty §5EV §7Battery Hull");
        provider.add("item.gtceu.iv_battery_hull.tooltip", "§7An empty §1IV §7Battery Hull");
        provider.add("item.gtceu.luv_battery_hull.tooltip", "§7An empty §dLuV §7Battery Hull");
        provider.add("item.gtceu.zpm_battery_hull.tooltip", "§7An empty §fZPM §7Battery Hull");
        provider.add("item.gtceu.uv_battery_hull.tooltip", "§7An empty §3UV §7Battery Hull");

        // Battery Behavior
        provider.add("item.gtceu.battery.charge_time", "§aHolds %s %s of Power (%s)");
        provider.add("item.gtceu.battery.charge_detailed", "%s/%s EU§7 - Tier %s §7(%s/%s remaining§7)");

        // Rechargeable Batteries
        provider.add("item.gtceu.ulv_tantalum_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.ev_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.iv_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.luv_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.zpm_naquadria_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.uv_naquadria_battery.tooltip", "§7Reusable Battery");

        provider.add("item.gtceu.energy_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotron_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotronic_energy_orb.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotronic_energy_orb_cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_module.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.max_battery.tooltip", "§7Fill this to win Minecraft");

        // Cover Tooltips
        provider.add("item.gtceu.electric.pump.tooltip", "§7Transfers §fFluids§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.fluid.regulator.tooltip", "§7Limits §fFluids§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.conveyor.module.tooltip", "§7Transfers §fItems§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.robot.arm.tooltip", "§7Limits §fItems§7 to specific quantities as §fCover§7.");

        // Data Items
        provider.add("item.gtceu.data_stick.tooltip", "§7A Low Capacity Data Storage");
        provider.add("item.gtceu.data_orb.tooltip", "§7A High Capacity Data Storage");

        // Programmed Circuit
        provider.addMultiline("item.gtceu.programmed_circuit.tooltip",
                "Use to open configuration GUI\nShift-Right-Click on a machine\nwith a circuit slot to set it to\nthis circuit's value.");
        provider.add("item.gtceu.circuit.integrated.gui", "§7Programmed Circuit Configuration");
        provider.add("item.int_circuit.configuration", "Configuration: %d");
        // provider.addMultiline("item.gtceu.circuit.integrated.jei_description", "JEI is only showing recipes for the
        // given configuration.\n\nYou can select a configuration in the Programmed Circuit configuration tab.");

        // Boules
        provider.add("item.gtceu.silicon_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.phosphorus_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.naquadah_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.neutronium_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.silicon_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.phosphorus_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.naquadah_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.neutronium_wafer.tooltip", "§7Raw Circuit");

        // Boards
        provider.add("item.gtceu.resin_circuit_board.tooltip", "§7A Coated Board");
        provider.add("item.gtceu.phenolic_circuit_board.tooltip", "§7A Good Board");
        provider.add("item.gtceu.plastic_circuit_board.tooltip", "§7A Good Board");
        provider.add("item.gtceu.epoxy_circuit_board.tooltip", "§7An Advanced Board");
        provider.add("item.gtceu.fiber_reinforced_circuit_board.tooltip", "§7An Extreme Board");
        provider.add("item.gtceu.multilayer_fiber_reinforced_circuit_board.tooltip", "§7An Elite Board");
        provider.add("item.gtceu.wetware_circuit_board.tooltip", "§7The Board that keeps life");

        provider.add("item.gtceu.resin_printed_circuit_board.tooltip", "§7A Basic Circuit Board");
        provider.add("item.gtceu.phenolic_printed_circuit_board.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.plastic_printed_circuit_board.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.epoxy_printed_circuit_board.tooltip", "§7An Advanced Circuit Board");
        provider.add("item.gtceu.fiber_reinforced_printed_circuit_board.tooltip", "§7A More Advanced Circuit Board");
        provider.add("item.gtceu.multilayer_fiber_reinforced_printed_circuit_board.tooltip",
                "§7An Elite Circuit Board");
        provider.add("item.gtceu.wetware_printed_circuit_board.tooltip", "§7The Board that keeps life");

        // Circuit Components
        provider.addMultiline("item.gtceu.vacuum_tube.tooltip", "§7Technically a Diode\n§cULV-Tier");
        provider.add("item.gtceu.diode.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.resistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.transistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.capacitor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.inductor.tooltip", "§7A Small Coil");

        provider.add("item.gtceu.smd_diode.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_capacitor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_transistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_resistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_inductor.tooltip", "§7Electronic Component");

        provider.add("item.gtceu.advanced_smd_diode.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_capacitor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_transistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_resistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_inductor.tooltip", "§7Advanced Electronic Component");

        // Wafers
        provider.add("item.gtceu.highly_advanced_soc_wafer.tooltip", "§7Raw Highly Advanced Circuit");
        provider.add("item.gtceu.advanced_soc_wafer.tooltip", "§7Raw Advanced Circuit");
        provider.add("item.gtceu.ilc_wafer.tooltip", "§7Raw Integrated Circuit");
        provider.add("item.gtceu.cpu_wafer.tooltip", "§7Raw Processing Unit");
        provider.add("item.gtceu.hpic_wafer.tooltip", "§7Raw High Power Circuit");
        provider.add("item.gtceu.uhpic_wafer.tooltip", "§7Raw Ultra High Power Circuit");
        provider.add("item.gtceu.nand_memory_wafer.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.ulpic_wafer.tooltip", "§7Raw Ultra Low Power Circuit");
        provider.add("item.gtceu.lpic_wafer.tooltip", "§7Raw Low Power Circuit");
        provider.add("item.gtceu.mpic_wafer.tooltip", "§7Raw Power Circuit");
        provider.add("item.gtceu.nano_cpu_wafer.tooltip", "§7Raw Nano Circuit");
        provider.add("item.gtceu.nor_memory_wafer.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.qbit_cpu_wafer.tooltip", "§7Raw Qubit Circuit");
        provider.add("item.gtceu.ram_wafer.tooltip", "§7Raw Memory");
        provider.add("item.gtceu.soc_wafer.tooltip", "§7Raw Basic Circuit");
        provider.add("item.gtceu.simple_soc_wafer.tooltip", "§7Raw Simple Circuit");
        provider.add("item.gtceu.engraved_crystal_chip.tooltip", "§7Needed for Circuits");
        provider.add("item.gtceu.raw_crystal_chip.tooltip", "§7Raw Crystal Processor");
        provider.add("item.gtceu.raw_crystal_chip_parts.tooltip", "§7Raw Crystal Processor Parts");
        provider.add("item.gtceu.crystal_cpu.tooltip", "§7Crystal Processing Unit");
        provider.add("item.gtceu.crystal_soc.tooltip", "§7Crystal System on Chip");
        provider.add("item.gtceu.advanced_soc.tooltip", "§7Advanced System on Chip");
        provider.add("item.gtceu.highly_advanced_soc.tooltip", "§7Highly Advanced System on Chip");

        // Chips
        provider.add("item.gtceu.ilc_chip.tooltip", "§7Integrated Logic Circuit");
        provider.add("item.gtceu.cpu_chip.tooltip", "§7Central Processing Unit");
        provider.add("item.gtceu.hpic_chip.tooltip", "§7High Power IC");
        provider.add("item.gtceu.uhpic_chip.tooltip", "§7Ultra High Power IC");
        provider.add("item.gtceu.nand_memory_chip.tooltip", "§7NAND Logic Gate");
        provider.add("item.gtceu.nano_cpu_chip.tooltip", "§7Nano Central Processing Unit");
        provider.add("item.gtceu.nor_memory_chip.tooltip", "§7NOR Logic Gate");
        provider.add("item.gtceu.ulpic_chip.tooltip", "§7Ultra Low Power IC");
        provider.add("item.gtceu.lpic_chip.tooltip", "§7Low Power IC");
        provider.add("item.gtceu.mpic_chip.tooltip", "§7Power IC");
        provider.add("item.gtceu.qbit_cpu_chip.tooltip", "§7Qubit Central Processing Unit");
        provider.add("item.gtceu.ram_chip.tooltip", "§7Random Access Memory");
        provider.add("item.gtceu.soc.tooltip", "§7System on Chip");
        provider.add("item.gtceu.simple_soc.tooltip", "§7Simple System on Chip");

        // Circuits
        provider.addMultiline("item.gtceu.basic_electronic_circuit.tooltip",
                "§7Your First Circuit\n§cLV-Tier Circuit");
        provider.addMultiline("item.gtceu.good_electronic_circuit.tooltip",
                "§7Your Second Circuit\n§cMV-Tier Circuit");

        provider.addMultiline("item.gtceu.basic_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6LV-Tier Circuit");
        provider.addMultiline("item.gtceu.good_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6MV-Tier Circuit");
        provider.addMultiline("item.gtceu.advanced_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6HV-Tier Circuit");

        provider.addMultiline("item.gtceu.nand_chip.tooltip", "§7A Superior Simple Circuit\n§6ULV-Tier Circuit");
        provider.addMultiline("item.gtceu.microchip_processor.tooltip",
                "§7A Superior Basic Circuit\n§eLV-Tier Circuit");
        provider.addMultiline("item.gtceu.micro_processor.tooltip",
                "§7Amazing Computation Speed!\n§eMV-Tier Circuit");
        provider.addMultiline("item.gtceu.micro_processor_assembly.tooltip",
                "§7Amazing Computation Speed!\n§eHV-Tier Circuit");
        provider.addMultiline("item.gtceu.micro_processor_computer.tooltip",
                "§7Amazing Computation Speed!\n§eEV-Tier Circuit");
        provider.addMultiline("item.gtceu.micro_processor_mainframe.tooltip",
                "§7Amazing Computation Speed!\n§eIV-Tier Circuit");

        provider.addMultiline("item.gtceu.nano_processor.tooltip", "§7Smaller than ever\n§bHV-Tier Circuit");
        provider.addMultiline("item.gtceu.nano_processor_assembly.tooltip", "§7Smaller than ever\n§bEV-Tier Circuit");
        provider.addMultiline("item.gtceu.nano_processor_computer.tooltip", "§7Smaller than ever\n§bIV-Tier Circuit");
        provider.addMultiline("item.gtceu.nano_processor_mainframe.tooltip",
                "§7Smaller than ever\n§bLuV-Tier Circuit");

        provider.addMultiline("item.gtceu.quantum_processor.tooltip",
                "§7Quantum Computing comes to life!\n§aEV-Tier Circuit");
        provider.addMultiline("item.gtceu.quantum_processor_assembly.tooltip",
                "§7Quantum Computing comes to life!\n§aIV-Tier Circuit");
        provider.addMultiline("item.gtceu.quantum_processor_computer.tooltip",
                "§7Quantum Computing comes to life!\n§aLuV-Tier Circuit");
        provider.addMultiline("item.gtceu.quantum_processor_mainframe.tooltip",
                "§7Quantum Computing comes to life!\n§aZPM-Tier Circuit");

        provider.addMultiline("item.gtceu.crystal_processor.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9IV-Tier Circuit");
        provider.addMultiline("item.gtceu.crystal_processor_assembly.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9LuV-Tier Circuit");
        provider.addMultiline("item.gtceu.crystal_processor_computer.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9ZPM-Tier Circuit");
        provider.addMultiline("item.gtceu.crystal_processor_mainframe.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9UV-Tier Circuit");

        provider.addMultiline("item.gtceu.wetware_processor.tooltip",
                "§7You have a feeling like it's watching you\n§4LuV-Tier Circuit");
        provider.addMultiline("item.gtceu.wetware_processor_assembly.tooltip",
                "§7Can run Minecraft\n§4ZPM-Tier Circuit");
        provider.addMultiline("item.gtceu.wetware_processor_computer.tooltip",
                "§7Ultimate fusion of Flesh and Machine\n§4UV-Tier Circuit");
        provider.addMultiline("item.gtceu.wetware_processor_mainframe.tooltip",
                "§7The best Man has ever seen\n§4UHV-Tier Circuit");

        // Stars
        provider.add("item.gtceu.quantum_eye.tooltip", "§7Improved Ender Eye");
        provider.add("item.gtceu.quantum_star.tooltip", "§7Improved Nether Star");
        provider.add("item.gtceu.gravi_star.tooltip", "§7Ultimate Nether Star");

        // Filters
        provider.addMultiline("item.gtceu.item_filter.tooltip",
                "§7Filters §fItem§7 I/O as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.addMultiline("item.gtceu.item_tag_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fItem Tags§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.addMultiline("item.gtceu.tag_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fTag§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.addMultiline("item.gtceu.fluid_filter.tooltip",
                "§7Filters §fFluid§7 I/O as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        provider.addMultiline("item.gtceu.fluid_tag_filter.tooltip",
                "§7Filters §fFluid§7 I/O with §fFluid Tags§7 as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        provider.addMultiline("item.gtceu.item_smart_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fMachine Recipes§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.add("item.gtceu.machine_controller.tooltip", "§7Turns Machines §fON/OFF§7 as §fCover§7.");
        provider.add("item.gtceu.activity_detector_cover.tooltip",
                "§7Gives out §fActivity Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_activity_detector_cover.tooltip",
                "§7Gives out §fMachine Progress§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.fluid_detector_cover.tooltip",
                "§7Gives out §fFluid Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_fluid_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fFluid Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.item_detector_cover.tooltip", "§7Gives out §fItem Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_item_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fItem Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.energy_detector_cover.tooltip",
                "§7Gives out §fEnergy Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_energy_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fEnergy Status§7 as Redstone as §fCover§7.");
        provider.addMultiline("item.gtceu.fluid_voiding_cover.tooltip",
                "§7Voids §fFluids§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.addMultiline("item.gtceu.advanced_fluid_voiding_cover.tooltip",
                "§7Voids §fFluids§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.addMultiline("item.gtceu.item_voiding_cover.tooltip",
                "§7Voids §fItems§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.addMultiline("item.gtceu.advanced_item_voiding_cover.tooltip",
                "§7Voids §fItems§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.addMultiline("item.gtceu.facade_cover.tooltip",
                "§7Decorative Outfit §fCover§7.\n§7Crafted using 3 Iron Plates and any block");
        provider.add("item.gtceu.computer_monitor_cover.tooltip", "§7Displays §fData§7 as §fCover§7.");
        provider.add("item.gtceu.shutter_module_cover.tooltip",
                "§fBlocks Transfer§7 through attached Side as §fCover§7.");
        provider.addMultiline("item.gtceu.solar_panel.tooltip",
                "§7May the Sun be with you.\nProduces §fEnergy§7 from the §eSun§7 as §fCover§7.");
        provider.add("item.gtceu.infinite_water_cover.tooltip",
                "§7Fills attached containers with §9Water§7 as §fCover§7.");
        provider.add("item.gtceu.ender_fluid_link_cover.tooltip",
                "§7Transports §fFluids§7 with a §fWireless §dEnder§f Connection§7 as §fCover§7.");

        // Misc Items
        provider.add("item.gtceu.gelled_toluene.tooltip", "§7Raw Explosive");
        provider.add("item.gtceu.bottle.purple.drink.tooltip",
                "§7How about Lemonade. Or some Ice Tea? I got Purple Drink!");
        provider.addMultiline("item.gtceu.foam_sprayer.tooltip",
                "§7Sprays Construction Foam\nUse on a frame to foam connected frames\nFoam can be colored");
        provider.add("item.gtceu.firebrick.tooltip", "§7Heat resistant");

        provider.add("item.gtceu.basic_tape.tooltip",
                "§7Not strong enough for mechanical issues\nCan be used to pick up crates without dropping their items");
        provider.add("item.gtceu.duct_tape.tooltip", "§7If you can't fix it with this, use more of it!");

        provider.add("item.gtceu.stem_cells.tooltip", "§7Raw Intelligence");
        provider.add("item.gtceu.neuro_processing_unit.tooltip", "§7Neuro CPU");
        provider.add("item.gtceu.petri_dish.tooltip", "§7For cultivating Cells");
        provider.add("item.gtceu.neutron_reflector.tooltip", "§7Indestructible");
        provider.add("item.gtceu.blacklight.tooltip", "Long-Wave §dUltraviolet§7 light source");

        provider.add("item.gtceu.nan_certificate.tooltip", "Challenge Accepted!");

        // Turbines
        provider.add("item.gtceu.turbine_rotor.tooltip", "Turbine Rotors for your power station");

        // Terminal
        provider.add("item.gtceu.terminal.tooltip",
                "Shift + R-Click on a controller to automatically build the multi-block");
        provider.add("item.terminal.tooltip", "Sharp tools make good work");
        provider.add("item.terminal.tooltip.creative", "§bCreative Mode");
        provider.add("item.terminal.tooltip.hardware", "§aHardware: %d");

        // Records
        provider.add("item.gtceu.sus_record.tooltip", "§7sussy!");
        provider.add("item.gtceu.sus_record.desc", "§7Leonz - Among Us Drip");

        // Voltage Coils
        provider.add("item.gtceu.ulv_voltage_coil.tooltip", "Primitive Coil");
        provider.add("item.gtceu.lv_voltage_coil.tooltip", "Basic Coil");
        provider.add("item.gtceu.mv_voltage_coil.tooltip", "Good Coil");
        provider.add("item.gtceu.hv_voltage_coil.tooltip", "Advanced Coil");
        provider.add("item.gtceu.ev_voltage_coil.tooltip", "Extreme Coil");
        provider.add("item.gtceu.iv_voltage_coil.tooltip", "Elite Coil");
        provider.add("item.gtceu.luv_voltage_coil.tooltip", "Master Coil");
        provider.add("item.gtceu.zpm_voltage_coil.tooltip", "Super Coil");
        provider.add("item.gtceu.uv_voltage_coil.tooltip", "Ultimate Coil");
        provider.add("item.gtceu.uhv_voltage_coil.tooltip", "Ultra Coil");
        provider.add("item.gtceu.uev_voltage_coil.tooltip", "Unreal Coil");
        provider.add("item.gtceu.uiv_voltage_coil.tooltip", "Insane Coil");
        provider.add("item.gtceu.uxv_voltage_coil.tooltip", "Epic Coil");
        provider.add("item.gtceu.opv_voltage_coil.tooltip", "Legendary Coil");
        provider.add("item.gtceu.max_voltage_coil.tooltip", "Maximum Coil");

        // Clipboard
        provider.add("item.clipboard.tooltip",
                "Can be written on (without any writing Instrument). Right-click on Wall to place, and Shift-Right-Click to remove");

        provider.add("item.generic.fluid_container.tooltip", "%d/%dL %s");

        // General Electric Tools
        provider.add("item.generic.electric_item.tooltip", "%d/%d EU - Tier %s");
        provider.add("item.generic.electric_item.stored", "%d/%d EU (%s)");
        provider.add("item.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("item.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("item.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");
    }

    public static void generateBehaviorKeys(GTLangProvider provider) {
        // Item Behaviors
        provider.add("behaviour.hoe", "Can till dirt");
        provider.add("behaviour.soft_hammer", "Activates and Deactivates Machines");
        provider.add("behaviour.soft_hammer.enabled", "Working Enabled");
        provider.add("behaviour.soft_hammer.disabled", "Working Disabled");
        provider.add("behaviour.soft_hammer.idle_after_cycle", "Pause machine after current cycle");

        provider.add("behaviour.lighter.tooltip.description", "Can light things on fire");
        provider.add("behaviour.lighter.tooltip.usage", "Shift-right click to open/close");
        provider.add("behaviour.lighter.fluid.tooltip", "Can light things on fire with Butane or Propane");
        provider.add("behaviour.lighter.uses", "Remaining uses: %d");

        provider.add("behavior.toggle_energy_consumer.tooltip", "Use to toggle mode");
        provider.add("behaviour.hammer", "Turns on and off Muffling for Machines (by hitting them)");
        provider.add("behaviour.wrench", "Rotates Blocks on Rightclick");

        // Susso
        provider.add("behaviour.boor.by", "by %s");

        // Magnet
        provider.add("behavior.item_magnet.enabled", "§aMagnetic Field Enabled");
        provider.add("behavior.item_magnet.disabled", "§cMagnetic Field Disabled");

        // Wrench Configuration
        provider.add("item.machine_configuration.mode", "§aConfiguration Mode:§r %s");
        provider.add("gtceu.mode.fluid", "§9Fluid§r");
        provider.add("gtceu.mode.item", "§6Item§r");
        provider.add("gtceu.mode.both", "§dBoth (Fluid And Item)§r");

        // Spray can
        provider.add("behaviour.paintspray.solvent.tooltip", "Can remove color from things");
        provider.add("behaviour.paintspray.white.tooltip", "Can paint things in White");
        provider.add("behaviour.paintspray.orange.tooltip", "Can paint things in Orange");
        provider.add("behaviour.paintspray.magenta.tooltip", "Can paint things in Magenta");
        provider.add("behaviour.paintspray.light_blue.tooltip", "Can paint things in Light Blue");
        provider.add("behaviour.paintspray.yellow.tooltip", "Can paint things in Yellow");
        provider.add("behaviour.paintspray.lime.tooltip", "Can paint things in Lime");
        provider.add("behaviour.paintspray.pink.tooltip", "Can paint things in Pink");
        provider.add("behaviour.paintspray.gray.tooltip", "Can paint things in Gray");
        provider.add("behaviour.paintspray.light_gray.tooltip", "Can paint things in Light Gray");
        provider.add("behaviour.paintspray.cyan.tooltip", "Can paint things in Cyan");
        provider.add("behaviour.paintspray.purple.tooltip", "Can paint things in Purple");
        provider.add("behaviour.paintspray.blue.tooltip", "Can paint things in Blue");
        provider.add("behaviour.paintspray.brown.tooltip", "Can paint things in Brown");
        provider.add("behaviour.paintspray.green.tooltip", "Can paint things in Green");
        provider.add("behaviour.paintspray.red.tooltip", "Can paint things in Red");
        provider.add("behaviour.paintspray.black.tooltip", "Can paint things in Black");
        provider.add("behaviour.paintspray.uses", "Remaining Uses: %d");

        // Copy/Paste Configuration
        provider.add("behaviour.meta.machine.config.copy.tooltip", "§7Sneak + R-Click to copy machine configuration");
        provider.add("behaviour.meta.machine.config.paste.tooltip", "§7R-Click to paste machine configuration");
        provider.add("behaviour.setting.allow.input.from.output.tooltip", "%s input from output side is %s");
        provider.add("behaviour.setting.output.direction.tooltip", "%s output direction: %s");
        provider.add("behaviour.setting.item_auto_output.tooltip", "%s auto-output is %s");
        provider.add("behaviour.setting.muffled.tooltip", "Muffling %s");
        provider.add("item.toggle.advanced.info.tooltip", "§8<Sneak to view stored configuration>");

        // Prospector
        provider.add("behaviour.prospecting", "Usable for Prospecting");
        provider.add("item.prospector.mode.ores", "§aOre Prospection Mode§r");
        provider.add("item.prospector.mode.fluid", "§bFluid Prospection Mode§r");
        provider.add("item.prospector.mode.bedrock_ore", "§bBedrock Ore Prospection Mode§r");
        provider.add("item.prospector.tooltip.radius", "Scans range in a %s Chunk Radius");
        provider.add("item.prospector.tooltip.modes", "Available Modes:");
        provider.add("behavior.prospector.not_enough_energy", "Not Enough Energy!");
        provider.add("behavior.prospector.added_waypoint", "Created waypoint named %s!");

        // Portable Scanner
        provider.add("item.tricorder_scanner.tooltip", "Tricorder");
        provider.add("item.debug_scanner.tooltip", "Tricorder");

        provider.add("behavior.portable_scanner.bedrock_fluid.amount", "Fluid In Deposit: %s %s - %s%%");
        provider.add("behavior.portable_scanner.bedrock_fluid.amount_unknown", "Fluid In Deposit: %s%%");
        provider.add("behavior.portable_scanner.bedrock_fluid.nothing", "Fluid In Deposit: §6Nothing§r");

        provider.add("behavior.portable_scanner.environmental_hazard", "Environmental Hazard In Chunk: %s§r - %s ppm");
        provider.add("behavior.portable_scanner.environmental_hazard.nothing",
                "Environmental Hazard In Chunk: §6Nothing§r");

        provider.add("behavior.portable_scanner.local_hazard", "Local Hazard In Area: %s§r - %s ppm");
        provider.add("behavior.portable_scanner.local_hazard.nothing", "Local Hazard In Area: §6Nothing§r");

        provider.add("behavior.portable_scanner.block_hardness", "Hardness: %s Blast Resistance: %s");
        provider.add("behavior.portable_scanner.block_name", "Name: %s MetaData: %s");

        provider.add("behavior.portable_scanner.debug_cpu_load",
                "Average CPU load of ~%sns over %s ticks with worst time of %sns.");
        provider.add("behavior.portable_scanner.debug_cpu_load_seconds", "This is %s seconds.");
        provider.add("behavior.portable_scanner.debug_lag_count",
                "Caused %s Lag Spike Warnings (anything taking longer than %sms) on the Server.");
        provider.add("behavior.portable_scanner.debug_machine", "Meta-ID: %s");
        provider.add("behavior.portable_scanner.debug_machine_invalid", " invalid!");
        provider.add("behavior.portable_scanner.debug_machine_invalid_null=invalid! MetaTileEntity =",
                " null!");
        provider.add("behavior.portable_scanner.debug_machine_valid", " valid");

        provider.add("behavior.portable_scanner.divider", "=========================");

        provider.add("behavior.portable_scanner.energy_container_in", "Max IN: %s (%s) EU at %s A");
        provider.add("behavior.portable_scanner.energy_container_out", "Max OUT: %s (%s) EU at %s A");
        provider.add("behavior.portable_scanner.energy_container_storage", "Energy: %s EU / %s EU");

        provider.add("behavior.portable_scanner.eu_per_sec", "Average (last second): %s EU/t");
        provider.add("behavior.portable_scanner.amp_per_sec", "Average (last second): %s A");
        provider.add("behavior.portable_scanner.machine_disabled", "Disabled.");

        provider.add("behavior.portable_scanner.machine_front_facing", "Front Facing: %s");
        provider.add("behavior.portable_scanner.machine_upwards_facing", "Upwards Facing: %s");

        provider.add("behavior.portable_scanner.machine_ownership", "§2Machine Owner Type: %s§r");
        provider.add("behavior.portable_scanner.guild_name", "§2Guild Name: %s§r");
        provider.add("behavior.portable_scanner.team_name", "§2Team Name: %s§r");
        provider.add("behavior.portable_scanner.player_name", "§2Player Name: %s§r, §7Player Online: %s§r");

        provider.add("behavior.portable_scanner.machine_power_loss", "Shut down due to power loss.");
        provider.add("behavior.portable_scanner.machine_progress", "Progress/Load: %s / %s");

        provider.add("behavior.portable_scanner.muffled", "Muffled.");

        provider.add("behavior.portable_scanner.multiblock_energy_input",
                "Max Energy Income: %s EU/t Tier: %s");
        provider.add("behavior.portable_scanner.multiblock_energy_output",
                "Max Energy Output: %s EU/t Tier: %s");
        provider.add("behavior.portable_scanner.multiblock_maintenance", "Problems: %s");
        provider.add("behavior.portable_scanner.multiblock_parallel", "Multi Processing: %s");

        provider.add("behavior.portable_scanner.position", "----- X: %s Y: %s Z: %s D: %s -----");
        provider.add("behavior.portable_scanner.state", "%s: %s");
        provider.add("behavior.portable_scanner.tank", "Tank %s: %s mB / %s mB %s");
        provider.add("behavior.portable_scanner.tanks_empty", "All Tanks Empty");

        provider.add("behavior.portable_scanner.workable_consumption", "Probably Uses: %s EU/t at %s A");
        provider.add("behavior.portable_scanner.workable_production", "Probably Produces: %s EU/t at %s A");
        provider.add("behavior.portable_scanner.workable_progress", "Progress: %s s / %s s");
        provider.add("behavior.portable_scanner.workable_stored_energy", "Stored Energy: %s EU / %s EU");

        provider.add("behavior.portable_scanner.mode.caption", "Display mode: %s");
        provider.add("behavior.portable_scanner.mode.show_all_info", "Show all info");
        provider.add("behavior.portable_scanner.mode.show_block_info", "Show block info");
        provider.add("behavior.portable_scanner.mode.show_machine_info", "Show machine info");
        provider.add("behavior.portable_scanner.mode.show_electrical_info", "Show electrical info");
        provider.add("behavior.portable_scanner.mode.show_recipe_info", "Show recipe info");
        provider.add("behavior.portable_scanner.mode.show_environmental_info", "Show environmental info");

        // Data Stick
        provider.add("behavior.data_item.assemblyline.title", "§nAssembly Line Construction Data:");
        provider.add("behavior.data_item.assemblyline.data", "- §a%s");
    }
}
