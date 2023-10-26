package com.gregtechceu.gtceu.integration.ae2.util;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * @Author GlodBlock
 * @Description A serializable {@link GenericStackInv} from AE2
 * @Date 2023/4/18-23:52
 */
public class SerializableGenericStackInv extends GenericStackInv implements ITagSerializable<ListTag> {

    public SerializableGenericStackInv(@Nullable Runnable listener, int size) {
        super(listener, size);
    }

    @Override
    public ListTag serializeNBT() {
        return super.writeToTag();
    }

    @Override
    public void deserializeNBT(ListTag tags) {
        super.readFromTag(tags);
    }

}