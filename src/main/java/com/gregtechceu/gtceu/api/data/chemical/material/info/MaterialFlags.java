package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialFlags {

    private final Set<MaterialFlag> flags = new HashSet<>();

    public MaterialFlags addFlags(MaterialFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public void verify(Material material) {
        flags.addAll(flags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        flags.forEach(f -> sb.append(f.toString()).append("\n"));
        return sb.toString();
    }

    /////////////////
    // GENERIC //
    /////////////////

    /**
     * Add to material to disable it's unification fully
     */
    @Deprecated
    public static final MaterialFlag NO_UNIFICATION = new MaterialFlag.Builder("no_unification").build();

    public static final MaterialFlag DISABLE_MATERIAL_RECIPES = new MaterialFlag.Builder("disable_material_recipes")
            .build();
    /**
     * Enables electrolyzer decomposition recipe generation
     */
    public static final MaterialFlag DECOMPOSITION_BY_ELECTROLYZING = new MaterialFlag.Builder(
            "decomposition_by_electrolyzing").build();

    /**
     * Enables centrifuge decomposition recipe generation
     */
    public static final MaterialFlag DECOMPOSITION_BY_CENTRIFUGING = new MaterialFlag.Builder(
            "decomposition_by_centrifuging").build();

    /**
     * Disables decomposition recipe generation for this material
     */
    public static final MaterialFlag DISABLE_DECOMPOSITION = new MaterialFlag.Builder("disable_decomposition").build();

    /**
     * Add to material if it is some kind of explosive
     */
    public static final MaterialFlag EXPLOSIVE = new MaterialFlag.Builder("explosive").build();

    /**
     * Add to material if it is some kind of flammable
     */
    public static final MaterialFlag FLAMMABLE = new MaterialFlag.Builder("flammable").build();

    /**
     * Add to material if it is some kind of sticky
     */
    public static final MaterialFlag STICKY = new MaterialFlag.Builder("sticky").build();

    /**
     * Add to material if it is some kind of phosphorescent
     */
    public static final MaterialFlag PHOSPHORESCENT = new MaterialFlag.Builder("phosphorescent").build();

    //////////////////
    // DUST //
    //////////////////

    /**
     * Generate a plate for this material
     * If it's dust material, dust compressor recipe into plate will be generated
     * If it's metal material, bending machine recipes will be generated
     * If block is found, cutting machine recipe will be also generated
     */
    public static final MaterialFlag GENERATE_PLATE = new MaterialFlag.Builder("generate_plate")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_DENSE = new MaterialFlag.Builder("generate_dense")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_ROD = new MaterialFlag.Builder("generate_rod")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_BOLT_SCREW = new MaterialFlag.Builder("generate_bolt_screw")
            .requireFlags(GENERATE_ROD)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_FRAME = new MaterialFlag.Builder("generate_frame")
            .requireFlags(GENERATE_ROD)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_GEAR = new MaterialFlag.Builder("generate_gear")
            .requireFlags(GENERATE_PLATE, GENERATE_ROD)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_LONG_ROD = new MaterialFlag.Builder("generate_long_rod")
            .requireFlags(GENERATE_ROD)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag FORCE_GENERATE_BLOCK = new MaterialFlag.Builder("force_generate_block")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     * Also preventing extruding and alloy smelting recipes via SHAPE_EXTRUDING/MOLD_BLOCK
     */
    public static final MaterialFlag EXCLUDE_BLOCK_CRAFTING_RECIPES = new MaterialFlag.Builder(
            "exclude_block_crafting_recipes")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag EXCLUDE_PLATE_COMPRESSOR_RECIPE = new MaterialFlag.Builder(
            "exclude_plate_compressor_recipe")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     */
    public static final MaterialFlag EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES = new MaterialFlag.Builder(
            "exclude_block_crafting_by_hand_recipes")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag MORTAR_GRINDABLE = new MaterialFlag.Builder("mortar_grindable")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * Add to material if it cannot be worked by any other means, than smashing or smelting. This is used for coated
     * Materials.
     */
    public static final MaterialFlag NO_WORKING = new MaterialFlag.Builder("no_working")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * Add to material if it cannot be used for regular Metal working techniques since it is not possible to bend it.
     */
    public static final MaterialFlag NO_SMASHING = new MaterialFlag.Builder("no_smashing")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * Add to material if it's impossible to smelt it
     */
    public static final MaterialFlag NO_SMELTING = new MaterialFlag.Builder("no_smelting")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * Add to material if it's impossible to smelt it from an ore.
     */
    public static final MaterialFlag NO_ORE_SMELTING = new MaterialFlag.Builder("no_ore_smelting")
            .requireProps(PropertyKey.DUST)
            .build();

    /**
     * Add to a material to disable creating an ore processing tab.
     */
    public static final MaterialFlag NO_ORE_PROCESSING_TAB = new MaterialFlag.Builder("no_ore_processing_tab")
            .requireProps(PropertyKey.ORE)
            .build();

    /**
     * Add this to your Material if you want to have its Ore Calcite heated in a Blast Furnace for more output. Already
     * listed are:
     * Iron, Pyrite, PigIron, WroughtIron.
     */
    public static final MaterialFlag BLAST_FURNACE_CALCITE_DOUBLE = new MaterialFlag.Builder(
            "blast_furnace_calcite_double")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag BLAST_FURNACE_CALCITE_TRIPLE = new MaterialFlag.Builder(
            "blast_furnace_calcite_triple")
            .requireProps(PropertyKey.DUST)
            .build();

    // GCYM
    /**
     * Use to disable alloy blast recipes from generating
     */
    public static final MaterialFlag DISABLE_ALLOY_BLAST = new MaterialFlag.Builder("disable_alloy_blast")
            .requireProps(PropertyKey.BLAST, PropertyKey.FLUID)
            .build();

    /**
     * Use to disable everything related to alloy blasting
     */
    public static final MaterialFlag DISABLE_ALLOY_PROPERTY = new MaterialFlag.Builder("disable_alloy_property")
            .requireProps(PropertyKey.BLAST, PropertyKey.FLUID)
            .requireFlags(DISABLE_ALLOY_BLAST)
            .build();

    /////////////////
    // FLUID //
    /////////////////

    public static final MaterialFlag SOLDER_MATERIAL = new MaterialFlag.Builder("solder_material")
            .requireProps(PropertyKey.FLUID)
            .build();

    public static final MaterialFlag SOLDER_MATERIAL_BAD = new MaterialFlag.Builder("solder_material_bad")
            .requireProps(PropertyKey.FLUID)
            .build();

    public static final MaterialFlag SOLDER_MATERIAL_GOOD = new MaterialFlag.Builder("solder_material_good")
            .requireProps(PropertyKey.FLUID)
            .build();

    /////////////////
    // INGOT //
    /////////////////

    public static final MaterialFlag GENERATE_FOIL = new MaterialFlag.Builder("generate_foil")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_RING = new MaterialFlag.Builder("generate_ring")
            .requireFlags(GENERATE_ROD)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_SPRING = new MaterialFlag.Builder("generate_spring")
            .requireFlags(GENERATE_LONG_ROD)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_SPRING_SMALL = new MaterialFlag.Builder("generate_spring_small")
            .requireFlags(GENERATE_ROD)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_SMALL_GEAR = new MaterialFlag.Builder("generate_small_gear")
            .requireFlags(GENERATE_PLATE, GENERATE_ROD)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_FINE_WIRE = new MaterialFlag.Builder("generate_fine_wire")
            .requireFlags(GENERATE_FOIL)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_ROTOR = new MaterialFlag.Builder("generate_rotor")
            .requireFlags(GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_PLATE)
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_ROUND = new MaterialFlag.Builder("generate_round")
            .requireProps(PropertyKey.INGOT)
            .build();

    /**
     * Add this to your Material if it is a magnetized form of another Material.
     */
    public static final MaterialFlag IS_MAGNETIC = new MaterialFlag.Builder("is_magnetic")
            .requireProps(PropertyKey.INGOT)
            .build();

    /////////////////
    // GEM //
    /////////////////

    /**
     * If this material can be crystallized.
     */
    public static final MaterialFlag CRYSTALLIZABLE = new MaterialFlag.Builder("crystallizable")
            .requireProps(PropertyKey.GEM)
            .build();

    public static final MaterialFlag GENERATE_LENS = new MaterialFlag.Builder("generate_lens")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.GEM)
            .build();

    /////////////////
    // ORE //
    /////////////////

    public static final MaterialFlag HIGH_SIFTER_OUTPUT = new MaterialFlag.Builder("high_sifter_output")
            .requireProps(PropertyKey.GEM, PropertyKey.ORE)
            .build();
}
