package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.recipe.ConsumedInputsData;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class ConsumedInputsDataTransformer implements ValueTransformer<ConsumedInputsData> {

    @Override
    public Tag serializeNBT(ConsumedInputsData value, TransformerContext<ConsumedInputsData> context) {
        return ConsumedInputsData.CODEC.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    @Override
    public @Nullable ConsumedInputsData deserializeNBT(Tag tag, TransformerContext<ConsumedInputsData> context) {
        return ConsumedInputsData.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }
}
