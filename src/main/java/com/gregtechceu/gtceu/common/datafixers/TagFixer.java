package com.gregtechceu.gtceu.common.datafixers;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Locale;

public class TagFixer {

    public static final String[] FLUID_TAGS = {
            "currentMilliBucketsPerTick",
            "globalTransferSizeMillibuckets",
            "minValue",
            "maxValue",
    };

    // This is necessary for updating from old versions due to FluidStack long -> int changes
    // Any fluid-related long tags need to be turned into int tags
    public static void fixFluidTags(CompoundTag tag) {
        if (tag.contains("cover", Tag.TAG_COMPOUND)) {
            CompoundTag t = tag.getCompound("cover");
            for (String key : t.getAllKeys()) {
                var cover = t.getCompound(key);
                var id = cover.getCompound("uid").getString("id");
                if ((id.toLowerCase(Locale.ROOT).contains("fluid") || id.toLowerCase(Locale.ROOT).contains("pump"))) {
                    var data = cover.getCompound("payload").getCompound("d");
                    for (String fix_key : FLUID_TAGS) {
                        if (data.contains(fix_key, Tag.TAG_LONG)) {
                            var l = data.getLong(fix_key);
                            data.putInt(fix_key, GTMath.saturatedCast(l));
                        }
                    }
                }
            }
        }
    }
}
