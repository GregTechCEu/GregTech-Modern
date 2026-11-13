package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.ItemRenameFix;

import com.mojang.datafixers.schemas.Schema;

import java.util.Set;

public class ExtruderCastingMoldRemapFix extends ItemRenameFix {

    private static Set<String> REMOVED_MOLDS = Set.of(
            "gtceu:rod_extruder_mold",
            "gtceu:bolt_extruder_mold",
            "gtceu:gear_extruder_mold",
            "gtceu:small_gear_extruder_mold",
            "gtceu:rotor_extruder_mold"
    // "gtceu:rotor_casting_mold" TODO uncomment when this is removed
    );
    private static final String REPLACEMENT_ITEM = "gtceu:empty_mold";

    public ExtruderCastingMoldRemapFix(Schema outputSchema) {
        super(outputSchema, "Extruder/Casting Mold Remap Fix");
    }

    @Override
    protected String fixItem(String item) {
        return REMOVED_MOLDS.contains(item) ? REPLACEMENT_ITEM : item;
    }
}
