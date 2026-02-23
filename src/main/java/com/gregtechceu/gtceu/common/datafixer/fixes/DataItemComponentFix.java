package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.ItemStackComponentRemainderFix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.Nullable;

public class DataItemComponentFix extends ItemStackComponentRemainderFix {

    public DataItemComponentFix(Schema outputSchema) {
        super(outputSchema, "GTDataItemComponentFix", "gtceu:data_item");
    }

    @SuppressWarnings("NullableProblems") // this method is passed to Optional#map, where null is a valid return value.
    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> tag) {
        Dynamic<T> oldValue = tag;

        tag = tag.emptyMap();
        if (oldValue.asBoolean().isSuccess()) {
            tag = tag.set("requires_data_bank", oldValue);
        }
        // assign a default capacity of 8 because it has to be set to something
        tag = tag.set("capacity", tag.createInt(8));

        return tag;
    }
}
