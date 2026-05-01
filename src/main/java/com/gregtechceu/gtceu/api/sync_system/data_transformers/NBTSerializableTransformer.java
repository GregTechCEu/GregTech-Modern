package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerializableTransformer implements ValueTransformer<INBTSerializable<Tag>> {

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value,
                            ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        return value.serializeNBT(context.lookup());
    }

    @Override
    public @Nullable INBTSerializable<Tag> deserializeNBT(Tag tag,
                                                          ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from saved data.");
            return null;
        }
        currentVal.deserializeNBT(context.lookup(), TagCompatibilityFixer.stripLDLibPayloadWrapper(tag));
        return currentVal;
    }
}
