package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilableData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class GTDataMaps {

    public static final DataMapType<Item, SpoilableData> SPOILABLE_DATA = DataMapType.builder(
            GTCEu.id("spoilable_data"),
            Registries.ITEM,
            SpoilableData.CODEC
    ).build();

}
