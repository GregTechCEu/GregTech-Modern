package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.UnaryOperator;

public class MaterialRenameHelper {

    public static UnaryOperator<String> createRenamer(String oldName, String newName) {
        // variable names are shoter than usual to make the rename map be as small as possible
        // old namespace
        final String oNs = oldName.substring(0, oldName.indexOf(':') + 1);
        // old path
        final String oP = oldName.substring(oldName.indexOf(':') + 1);

        // new namespace
        final String nNs = newName.substring(0, newName.indexOf(':') + 1);
        // new path
        final String nP = newName.substring(newName.indexOf(':') + 1);

        // if anyone has a better suggestion on how to implement this, feel free to tell.
        final Map<String, String> renameMap = ImmutableMap.<String, String>builder() // spotless:off
                .put(oNs + oP,                                             nNs + nP)
                .put(oNs + "flowing_" + oP,                                nNs + "flowing_" + nP)

                .put(oNs + oP + "_ore",                                    nNs + nP + "_ore")
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
                .put(oNs + oP + "_indicator",                              nNs + nP + "_indicator")

                .put(oNs + "raw_" + oP,                                    nNs + "raw_" + nP)
                .put(oNs + "raw_" + oP + "_block",                         nNs + "raw_" + nP + "_block")
                .put(oNs + "crushed_" + oP + "_ore",                       nNs + "crushed_" + nP + "_ore")
                .put(oNs + "refined_" + oP + "_ore",                       nNs + "refined_" + nP + "_ore")
                .put(oNs + "purified_" + oP + "_ore",                      nNs + "purified_" + nP + "_ore")

                .put(oNs + oP + "_dust",                                   nNs + nP + "_dust")
                .put(oNs + "small_" + oP + "_dust",                        nNs + "small_" + nP + "_dust")
                .put(oNs + "tiny_" + oP + "_dust",                         nNs + "tiny_" + nP + "_dust")
                .put(oNs + "impure_" + oP + "_dust",                       nNs + "impure_" + nP + "_dust")
                .put(oNs + "purified_" + oP + "_dust",                     nNs + "purified_" + nP + "_dust")

                .put(oNs + oP + "_ingot",                                  nNs + nP + "_ingot")
                .put(oNs + "hot_" + oP + "_ingot",                         nNs + "hot_" + nP + "_ingot")
                .put(oNs + oP + "_nugget",                                 nNs + nP + "_nugget")

                .put(oNs + oP + "_gem",                                    nNs + nP + "_gem")
                .put(oNs + "chipped_" + oP + "_gem",                       nNs + "chipped_" + nP + "_gem")
                .put(oNs + "flawed_" + oP + "_gem",                        nNs + "flawed_" + nP + "_gem")
                .put(oNs + "flawless_" + oP + "_gem",                      nNs + "flawless_" + nP + "_gem")
                .put(oNs + "exquisite_" + oP + "_gem",                     nNs + "exquisite_" + nP + "_gem")

                .put(oNs + oP + "_plate",                                  nNs + nP + "_plate")
                .put(oNs + "dense_" + oP + "_plate",                       nNs + "dense_" + nP + "_plate")
                .put(oNs + "double_" + oP + "_plate",                      nNs + "double_" + nP + "_plate")

                .put(oNs + oP + "_round",                                  nNs + nP + "_round")
                .put(oNs + oP + "_foil",                                   nNs + nP + "_foil")
                .put(oNs + oP + "_foil",                                   nNs + nP + "_foil")
                .put(oNs + oP + "_rod",                                    nNs + nP + "_rod")
                .put(oNs + "long_" + oP + "_rod",                          nNs + "long_" + nP + "_rod")
                .put(oNs + oP + "_bolt",                                   nNs + nP + "_bolt")
                .put(oNs + oP + "_screw",                                  nNs + nP + "_screw")
                .put(oNs + oP + "_ring",                                   nNs + nP + "_ring")
                .put(oNs + oP + "_spring",                                 nNs + nP + "_spring")
                .put(oNs + "small_" + oP + "_spring",                      nNs + "small_" + nP + "_spring")
                .put(oNs + oP + "_rotor",                                  nNs + nP + "_rotor")
                .put(oNs + oP + "_gear",                                   nNs + nP + "_gear")
                .put(oNs + "small_" + oP + "_gear",                        nNs + "small_" + nP + "_gear")
                .put(oNs + oP + "_lens",                                   nNs + nP + "_lens")

                .put(oNs + oP + "_buzz_saw_blade",                         nNs + nP + "_buzz_saw_blade")
                .put(oNs + oP + "_screwdriver_tip",                        nNs + nP + "_screwdriver_tip")
                .put(oNs + oP + "_drill_head",                             nNs + nP + "_drill_head")
                .put(oNs + oP + "_chainsaw_head",                          nNs + nP + "_chainsaw_head")
                .put(oNs + oP + "_wrench_tip",                             nNs + nP + "_wrench_tip")
                .put(oNs + oP + "_wire_cutter_head",                       nNs + nP + "_wire_cutter_head")
                .put(oNs + oP + "_turbine_blade",                          nNs + nP + "_turbine_blade")
                .put(oNs + oP + "_turbine_rotor",                          nNs + nP + "_turbine_rotor")

                .put(oNs + oP + "_block",                                  nNs + nP + "_block")
                .put(oNs + oP + "_frame",                                  nNs + nP + "_frame")

                .put(oNs + "fine_" + oP + "_wire",                         nNs + "fine_" + nP + "_wire")
                .put(oNs + oP + "_single_wire",                            nNs + nP + "_single_wire")
                .put(oNs + oP + "_double_wire",                            nNs + nP + "_double_wire")
                .put(oNs + oP + "_quadruple_wire",                         nNs + nP + "_quadruple_wire")
                .put(oNs + oP + "_octal_wire",                             nNs + nP + "_octal_wire")
                .put(oNs + oP + "_hex_wire",                               nNs + nP + "_hex_wire")
                .put(oNs + oP + "_single_cable",                           nNs + nP + "_single_cable")
                .put(oNs + oP + "_double_cable",                           nNs + nP + "_double_cable")
                .put(oNs + oP + "_quadruple_cable",                        nNs + nP + "_quadruple_cable")
                .put(oNs + oP + "_octal_cable",                            nNs + nP + "_octal_cable")
                .put(oNs + oP + "_hex_cable",                              nNs + nP + "_hex_cable")

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
