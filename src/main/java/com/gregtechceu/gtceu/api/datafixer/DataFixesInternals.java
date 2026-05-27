/*
 * Copyright 2022 QuiltMC
 * Modified by the Steam 'n' Rails (Railways) team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gregtechceu.gtceu.api.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.*;

import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;

@ApiStatus.Internal
public abstract class DataFixesInternals {

    public static final String GT_DATA_VERSION_TAG = "GTCEu_DataVersion";
    private static final Logger LOGGER = LogManager.getLogger("GTCEu/DFU");

    public static final BiFunction<Integer, @Nullable Schema, Schema> BASE_SCHEMA = (version, parent) -> {
        checkArgument(version == 0, "version must be 0");
        checkArgument(parent == null, "parent must be null");
        return get().createBaseSchema();
    };

    private static @Nullable DataFixesInternals instance;

    public static DataFixesInternals get() {
        if (instance == null) {
            // Init config in case it's not loaded yet
            if (!ConfigHolder.getInstance().compat.doDataFixers) {
                instance = new NoOpDataFixesInternals();
                return instance;
            }

            try {

                instance = new DataFixesInternalsImpl(() -> {
                    int vanillaDataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
                    return DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(vanillaDataVersion));
                });
            } catch (Exception ex) {
                LOGGER.warn("Failed to initialize! Either someone stopped DFU from initializing, or this Minecraft build is hosed.");
                LOGGER.warn("Using no-op implementation.");
                LOGGER.warn("Full error: ", ex);

                instance = new NoOpDataFixesInternals();
            }
        }

        return instance;
    }

    public abstract void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion,
                                       DataFixer dataFixer);

    @Contract(value = "-> new", pure = true)
    public abstract Schema createBaseSchema();

    public abstract <T> Dynamic<T> updateWithAllFixers(DSL.TypeReference dataFixTypes, Dynamic<T> dynamic);

    public abstract CompoundTag addGTDataVersion(CompoundTag tag);

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
}
