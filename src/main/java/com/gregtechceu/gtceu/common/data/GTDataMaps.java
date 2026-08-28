package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.spoilage.ItemSpoilageData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class GTDataMaps {

    public static final DataMapType<Item, ItemSpoilageData> SPOILABLE_DATA = DataMapType.builder(
            GTCEu.id("item_spoil_data"),
            Registries.ITEM,
            ItemSpoilageData.CODEC
    ).build();

}
