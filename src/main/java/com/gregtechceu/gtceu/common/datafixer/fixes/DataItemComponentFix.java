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
        // remove the old tag entirely to be replaced with the new one
        return null;
    }
}
