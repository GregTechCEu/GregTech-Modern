package com.gregtechceu.gtceu.integration.ae2.util;

import com.gregtechceu.gtceu.core.mixins.ae2.GenericStackInvAccessor;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.helpers.externalstorage.GenericStackInv;
import org.jetbrains.annotations.Nullable;

/**
 * @Author GlodBlock
 * @Description A serializable {@link GenericStackInv} from AE2
 * @Date 2023/4/18-23:52
 */
public class SerializableGenericStackInv extends GenericStackInv
                                         implements INBTSerializable<ListTag>, IContentChangeAware {

    public SerializableGenericStackInv(@Nullable Runnable listener, int size) {
        super(listener, size);
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        return super.writeToTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag tags) {
        super.readFromTag(tags);
    }

    @Override
    public void setOnContentsChanged(Runnable runnable) {
        ((GenericStackInvAccessor) this).setListener(runnable);
    }

    @Override
    public Runnable getOnContentsChanged() {
        return ((GenericStackInvAccessor) this).getListener();
    }
}
