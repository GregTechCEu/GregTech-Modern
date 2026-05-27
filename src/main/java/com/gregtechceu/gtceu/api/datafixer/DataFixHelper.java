package com.gregtechceu.gtceu.api.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.datafixer.GTDataFixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.*;

@ApiStatus.Internal
public abstract class DataFixHelper {

    public static final String GT_DATA_VERSION_TAG = "GTCEu_DataVersion";
    public static final Logger LOGGER = LogManager.getLogger("GTCEu/DFU");

    // region updating

    public static <T> Dynamic<T> updateToCurrentVersion(DSL.TypeReference type, Dynamic<T> dynamic) {
        return update(type, dynamic, getGTDataVersion(dynamic), GTCEu.GT_DATA_VERSION);
    }

    public static <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> dynamic, int version, int newVersion) {
        if (GTDataFixers.getDataFixer() == null) {
            return dynamic;
        }
        return GTDataFixers.getDataFixer().update(type, dynamic, version, newVersion);
    }

    public static CompoundTag updateToCurrentVersion(DSL.TypeReference dataFixTypes, CompoundTag tag) {
        return update(dataFixTypes, tag, getGTDataVersion(tag), GTCEu.GT_DATA_VERSION);
    }

    public static CompoundTag update(DSL.TypeReference dataFixTypes, CompoundTag tag, int version, int newVersion) {
        return (CompoundTag) update(dataFixTypes, new Dynamic<>(NbtOps.INSTANCE, tag), version, newVersion).getValue();
    }

    // endregion

    // region data version handling

    public static CompoundTag addGTDataVersion(CompoundTag tag) {
        if (GTDataFixers.getDataFixer() != null) {
            tag.putInt(GT_DATA_VERSION_TAG, GTCEu.GT_DATA_VERSION);
        }

        return tag;
    }

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int getGTDataVersion(Dynamic<?> dynamic) {
        return getGTDataVersion(dynamic, -1);
    }

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int getGTDataVersion(Dynamic<?> dynamic, int defaultValue) {
        return dynamic.get(GT_DATA_VERSION_TAG).asInt(defaultValue);
    }

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int getGTDataVersion(CompoundTag tag) {
        return getGTDataVersion(tag, -1);
    }

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int getGTDataVersion(CompoundTag tag, int defaultValue) {
        return tag.contains(GT_DATA_VERSION_TAG, CompoundTag.TAG_ANY_NUMERIC) ? tag.getInt(GT_DATA_VERSION_TAG) :
                defaultValue;
    }

    // endregion

    public static Schema getLatestVanillaSchema() {
        int vanillaDataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        return DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(vanillaDataVersion));
    }
}
