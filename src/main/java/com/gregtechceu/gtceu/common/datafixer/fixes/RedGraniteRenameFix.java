package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class RedGraniteRenameFix {

    public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder()
            .put("gtceu:granite_red_dust", "gtceu:red_granite_dust")
            .put("gtceu:small_granite_red_dust", "gtceu:small_red_granite_dust")
            .put("gtceu:tiny_granite_red_dust", "gtceu:tiny_red_granite_dust")
            .build();
}
