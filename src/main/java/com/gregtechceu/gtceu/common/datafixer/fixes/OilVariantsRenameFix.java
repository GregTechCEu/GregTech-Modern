package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class OilVariantsRenameFix {

    public static final Map<String, String> RENAMED_ITEM_IDS = ImmutableMap.<String, String>builder()
            .put("gtceu:oil_heavy_bucket", "gtceu:heavy_oil_bucket")
            .put("gtceu:oil_light_bucket", "gtceu:light_oil_bucket")
            .put("gtceu:oil_medium_bucket", "gtceu:raw_oil_bucket")
            .build();

    public static final Map<String, String> RENAMED_BLOCK_IDS = ImmutableMap.<String, String>builder()
            .put("gtceu:oil_heavy", "gtceu:heavy_oil")
            .put("gtceu:oil_light", "gtceu:light_oil")
            .put("gtceu:oil_medium", "gtceu:raw_oil")
            .build();
}
