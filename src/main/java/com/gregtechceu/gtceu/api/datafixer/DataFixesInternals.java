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

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.*;

import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;

@ApiStatus.Internal
public abstract class DataFixesInternals {

    public static final BiFunction<Integer, Schema, Schema> BASE_SCHEMA = (version, parent) -> {
        checkArgument(version == 0, "version must be 0");
        checkArgument(parent == null, "parent must be null");
        return get().createBaseSchema();
    };

    public record DataFixerEntry(DataFixer dataFixer, int currentVersion) {}

    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int getModDataVersion(@NotNull Dynamic<?> compound) {
        return compound.get("GTCEu_DataVersion").asInt(0);
    }

    private static DataFixesInternals instance;

    public static @NotNull DataFixesInternals get() {
        if (instance == null) {
            // Init config in case it's not loaded yet
            ConfigHolder.init();
            if (!ConfigHolder.INSTANCE.compat.doDatafixers) {
                instance = new NoOpDataFixesInternals();
                return instance;
            }

            Schema latestVanillaSchema;
            try {
                latestVanillaSchema = DataFixers.getDataFixer()
                        .getSchema(DataFixUtils
                                .makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion()));
            } catch (Exception e) {
                latestVanillaSchema = null;
            }

            if (latestVanillaSchema == null) {
                GTCEu.LOGGER.warn("[GTCEuM DFU] Failed to initialize! Either someone stopped DFU from initializing,");
                GTCEu.LOGGER.warn("[GTCEuM DFU] or this Minecraft build is hosed.");
                GTCEu.LOGGER.warn("[GTCEuM DFU] Using no-op implementation.");
                instance = new NoOpDataFixesInternals();
            } else {
                instance = new DataFixesInternalsImpl(latestVanillaSchema);
            }
        }

        return instance;
    }

    public abstract void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion,
                                       @NotNull DataFixer dataFixer);

    public abstract @Nullable DataFixerEntry getFixerEntry();

    @Contract(value = "-> new", pure = true)
    public abstract @NotNull Schema createBaseSchema();

    public abstract @NotNull Dynamic<?> updateWithAllFixers(DSL.TypeReference dataFixTypes,
                                                            @NotNull Dynamic<?> dynamic);

    public abstract @NotNull CompoundTag addModDataVersions(@NotNull CompoundTag compound);
}
