package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class ListTransformer<T> extends ValueTransformer<List<T>> {

    private final ValueTransformer<T> elementTransformer;

    public ListTransformer(ValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    @Override
    public Tag serializeNBT(List<T> value, ISyncManaged holder) {
        ListTag list = new ListTag();
        for (var obj : value) {
            list.add(elementTransformer.serializeNBT(obj, null));
        }
        return list;
    }

    @Override
    public List<T> deserializeNBT(Tag tag, ISyncManaged holder, List<T> current) {
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return current;
        }
        if (current != null) current.clear();
        else current = new ArrayList<>();
        List<T> finalCurrent = current;
        listTag.forEach(t -> finalCurrent
                .add(elementTransformer.deserializeNBT(ValueTransformer.stripLdlibWrapper(t), null, null)));

        return current;
    }
}
