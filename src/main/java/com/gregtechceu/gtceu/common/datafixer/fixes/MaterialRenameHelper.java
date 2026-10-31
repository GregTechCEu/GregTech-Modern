package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.UnaryOperator;

public class MaterialRenameHelper {

    /**
     * This class creates a renamer that handles all of a material's (default) forms
     * 
     * @param oldName
     * @param newName
     * @return
     */
    public static UnaryOperator<String> createRenamer(String oldName, String newName) {
        // variable names are shoter than usual to make the rename map be as small as possible
        // old namespace (including colon)
        final String oNs = oldName.substring(0, oldName.indexOf(':') + 1);
        // old path
        final String oP = oldName.substring(oldName.indexOf(':') + 1);

        // new namespace (including colon)
        final String nNs = newName.substring(0, newName.indexOf(':') + 1);
        // new path
        final String nP = newName.substring(newName.indexOf(':') + 1);

        // if anyone has a better suggestion on how to implement this, feel free to tell.
        final Map<String, String> renameMap = ImmutableMap.<String, String>builder() // spotless:off
                .put(oldName,                                              newName)
                .put(oNs + "flowing_" + oP,                                nNs + "flowing_" + nP)
                .put(oNs + "liquid_" + oP,                                 nNs + "liquid_" + nP)
                .put(oNs + "flowing_" + "liquid_" + oP,                    nNs + "flowing_" + "liquid_" + nP)
                .put(oldName + "_gas",                                     newName + "_gas")
                .put(oNs + "flowing_" + oP + "_gas",                       nNs + "flowing_" + nP + "_gas")
                .put(oldName + "_plasma",                                  newName + "_plasma")
                .put(oNs + "flowing_" + oP + "_plasma",                    nNs + "flowing_" + nP + "_plasma")
                .put(oNs + "molten_" + oP,                                 nNs + "molten_" + nP)
                .put(oNs + "flowing_" + "molten_" + oP,                    nNs + "flowing_" + "molten_" + nP)

                .put(oldName + "_bucket",                                  newName + "_bucket")
                .put(oNs + "liquid_" + oP + "_bucket",                     nNs + "liquid_" + nP + "_bucket")
                .put(oldName + "_gas" + "_bucket",                         newName + "_gas" + "_bucket")
                .put(oldName + "_plasma" + "_bucket",                      newName + "_plasma" + "_bucket")
                .put(oNs + "molten_" + oP + "_bucket",                     nNs + "molten_" + nP + "_bucket")

                .put(oldName + "_ore",                                     newName + "_ore")
                .put(oNs + "granite_" + oP + "_ore",                       nNs + "granite_" + nP + "_ore")
                .put(oNs + "diorite_" + oP + "_ore",                       nNs + "diorite_" + nP + "_ore")
                .put(oNs + "andesite_" + oP + "_ore",                      nNs + "andesite_" + nP + "_ore")
                .put(oNs + "red_granite_" + oP + "_ore",                   nNs + "red_granite_" + nP + "_ore")
                .put(oNs + "marble_" + oP + "_ore",                        nNs + "marble_" + nP + "_ore")
                .put(oNs + "deepslate_" + oP + "_ore",                     nNs + "deepslate_" + nP + "_ore")
                .put(oNs + "tuff_" + oP + "_ore",                          nNs + "tuff_" + nP + "_ore")
                .put(oNs + "sand_" + oP + "_ore",                          nNs + "sand_" + nP + "_ore")
                .put(oNs + "red_sand_" + oP + "_ore",                      nNs + "red_sand_" + nP + "_ore")
                .put(oNs + "gravel_" + oP + "_ore",                        nNs + "gravel_" + nP + "_ore")
                .put(oNs + "basalt_" + oP + "_ore",                        nNs + "basalt_" + nP + "_ore")
                .put(oNs + "netherrack_" + oP + "_ore",                    nNs + "netherrack_" + nP + "_ore")
                .put(oNs + "blackstone_" + oP + "_ore",                    nNs + "blackstone_" + nP + "_ore")
                .put(oNs + "endstone_" + oP + "_ore",                      nNs + "endstone_" + nP + "_ore")
                .put(oldName + "_indicator",                               newName + "_indicator")

                .put(oNs + "raw_" + oP,                                    nNs + "raw_" + nP)
                .put(oNs + "raw_" + oP + "_block",                         nNs + "raw_" + nP + "_block")
                .put(oNs + "crushed_" + oP + "_ore",                       nNs + "crushed_" + nP + "_ore")
                .put(oNs + "refined_" + oP + "_ore",                       nNs + "refined_" + nP + "_ore")
                .put(oNs + "purified_" + oP + "_ore",                      nNs + "purified_" + nP + "_ore")

                .put(oldName + "_dust",                                    newName + "_dust")
                .put(oNs + "small_" + oP + "_dust",                        nNs + "small_" + nP + "_dust")
                .put(oNs + "tiny_" + oP + "_dust",                         nNs + "tiny_" + nP + "_dust")
                .put(oNs + "impure_" + oP + "_dust",                       nNs + "impure_" + nP + "_dust")
                .put(oNs + "purified_" + oP + "_dust",                     nNs + "purified_" + nP + "_dust")

                .put(oldName + "_ingot",                                   newName + "_ingot")
                .put(oNs + "hot_" + oP + "_ingot",                         nNs + "hot_" + nP + "_ingot")
                .put(oldName + "_nugget",                                  newName + "_nugget")

                .put(oldName + "_gem",                                     newName + "_gem")
                .put(oNs + "chipped_" + oP + "_gem",                       nNs + "chipped_" + nP + "_gem")
                .put(oNs + "flawed_" + oP + "_gem",                        nNs + "flawed_" + nP + "_gem")
                .put(oNs + "flawless_" + oP + "_gem",                      nNs + "flawless_" + nP + "_gem")
                .put(oNs + "exquisite_" + oP + "_gem",                     nNs + "exquisite_" + nP + "_gem")

                .put(oldName + "_plate",                                   newName + "_plate")
                .put(oNs + "dense_" + oP + "_plate",                       nNs + "dense_" + nP + "_plate")
                .put(oNs + "double_" + oP + "_plate",                      nNs + "double_" + nP + "_plate")

                .put(oldName + "_round",                                   newName + "_round")
                .put(oldName + "_foil",                                    newName + "_foil")
                .put(oldName + "_rod",                                     newName + "_rod")
                .put(oNs + "long_" + oP + "_rod",                          nNs + "long_" + nP + "_rod")
                .put(oldName + "_bolt",                                    newName + "_bolt")
                .put(oldName + "_screw",                                   newName + "_screw")
                .put(oldName + "_ring",                                    newName + "_ring")
                .put(oldName + "_spring",                                  newName + "_spring")
                .put(oNs + "small_" + oP + "_spring",                      nNs + "small_" + nP + "_spring")
                .put(oldName + "_rotor",                                   newName + "_rotor")
                .put(oldName + "_gear",                                    newName + "_gear")
                .put(oNs + "small_" + oP + "_gear",                        nNs + "small_" + nP + "_gear")
                .put(oldName + "_lens",                                    newName + "_lens")

                .put(oldName + "_buzz_saw_blade",                          newName + "_buzz_saw_blade")
                .put(oldName + "_screwdriver_tip",                         newName + "_screwdriver_tip")
                .put(oldName + "_drill_head",                              newName + "_drill_head")
                .put(oldName + "_chainsaw_head",                           newName + "_chainsaw_head")
                .put(oldName + "_wrench_tip",                              newName + "_wrench_tip")
                .put(oldName + "_wire_cutter_head",                        newName + "_wire_cutter_head")
                .put(oldName + "_turbine_blade",                           newName + "_turbine_blade")
                .put(oldName + "_turbine_rotor",                           newName + "_turbine_rotor")

                .put(oldName + "_block",                                   newName + "_block")
                .put(oldName + "_frame",                                   newName + "_frame")

                .put(oNs + "fine_" + oP + "_wire",                         nNs + "fine_" + nP + "_wire")
                .put(oldName + "_single_wire",                             newName + "_single_wire")
                .put(oldName + "_double_wire",                             newName + "_double_wire")
                .put(oldName + "_quadruple_wire",                          newName + "_quadruple_wire")
                .put(oldName + "_octal_wire",                              newName + "_octal_wire")
                .put(oldName + "_hex_wire",                                newName + "_hex_wire")
                .put(oldName + "_single_cable",                            newName + "_single_cable")
                .put(oldName + "_double_cable",                            newName + "_double_cable")
                .put(oldName + "_quadruple_cable",                         newName + "_quadruple_cable")
                .put(oldName + "_octal_cable",                             newName + "_octal_cable")
                .put(oldName + "_hex_cable",                               newName + "_hex_cable")

                .put(oNs + "tiny_" + oP + "_fluid_pipe",                   nNs + "tiny_" + nP + "_fluid_pipe")
                .put(oNs + "small_" + oP + "_fluid_pipe",                  nNs + "small_" + nP + "_fluid_pipe")
                .put(oNs + "normal_" + oP + "_fluid_pipe",                 nNs + "normal_" + nP + "_fluid_pipe")
                .put(oNs + "large_" + oP + "_fluid_pipe",                  nNs + "large_" + nP + "_fluid_pipe")
                .put(oNs + "huge_" + oP + "_fluid_pipe",                   nNs + "huge_" + nP + "_fluid_pipe")
                .put(oNs + "quadruple_" + oP + "_fluid_pipe",              nNs + "quadruple_" + nP + "_fluid_pipe")
                .put(oNs + "nonuple_" + oP + "_fluid_pipe",                nNs + "nonuple_" + nP + "_fluid_pipe")

                .put(oNs + "small_" + oP + "_item_pipe",                   nNs + "small_" + nP + "_item_pipe")
                .put(oNs + "normal_" + oP + "_item_pipe",                  nNs + "normal_" + nP + "_item_pipe")
                .put(oNs + "large_" + oP + "_item_pipe",                   nNs + "large_" + nP + "_item_pipe")
                .put(oNs + "huge_" + oP + "_item_pipe",                    nNs + "huge_" + nP + "_item_pipe")
                .put(oNs + "small_" + "restrictive_" + oP + "_item_pipe",  nNs + "small_" + "restrictive_" + nP + "_item_pipe")
                .put(oNs + "normal_" + "restrictive_" + oP + "_item_pipe", nNs + "normal_" + "restrictive_" + nP + "_item_pipe")
                .put(oNs + "large_" + "restrictive_" + oP + "_item_pipe",  nNs + "large_" + "restrictive_" + nP + "_item_pipe")
                .put(oNs + "huge_" + "restrictive_" + oP + "_item_pipe",   nNs + "huge_" + "restrictive_" + nP + "_item_pipe")
                .build(); // spotless:on

        // noinspection DataFlowIssue
        return id -> renameMap.getOrDefault(NamespacedSchema.ensureNamespaced(id), id);
    }
}
