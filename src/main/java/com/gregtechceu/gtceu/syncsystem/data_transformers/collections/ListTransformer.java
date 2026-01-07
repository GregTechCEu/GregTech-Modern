package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class ListTransformer<T> implements IValueTransformer<List<T>> {

    private final IValueTransformer<T> elementTransformer;

    public ListTransformer(IValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    @Override
    public Tag serializeNBT(List<T> value, ISyncManaged holder) {
        ListTag list = new ListTag();
        if (elementTransformer == null) return list;
        for (var obj : value) {
            list.add(elementTransformer.serializeNBT(obj, null));
        }
        return list;
    }

    @Override
    public List<T> deserializeNBT(Tag tag, ISyncManaged holder, List<T> current) {
        if (!(tag instanceof ListTag listTag) || elementTransformer == null) return List.of();

        try {
            if (current != null) current.clear();
            else current = new ArrayList<>();
            List<T> finalCurrent = current;
            listTag.forEach(t -> finalCurrent
                    .add(elementTransformer.deserializeNBT(IValueTransformer.stripLdlibWrapper(t), null, null)));
        } catch (UnsupportedOperationException e) {
            GTCEu.LOGGER.error("Sync: Cannot sync an immutable list: {} {}", holder, e);
        }

        return current;
    }
}
