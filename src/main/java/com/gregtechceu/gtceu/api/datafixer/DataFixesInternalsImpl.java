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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.gregtechceu.gtceu.common.datafixer.schemas.V0;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ApiStatus.Internal
public final class DataFixesInternalsImpl extends DataFixesInternals {

    private final Schema latestVanillaSchema;

    private @Nullable DataFixerEntry dataFixer;

    public DataFixesInternalsImpl(Schema latestVanillaSchema) {
        this.latestVanillaSchema = latestVanillaSchema;

        this.dataFixer = null;
    }

    @Override
    public void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion, DataFixer dataFixer) {
        if (this.dataFixer != null) {
            throw new IllegalArgumentException("GTCEu already has a registered data fixer");
        }

        this.dataFixer = new DataFixerEntry(dataFixer, currentVersion);
    }

    @Override
    public @Nullable DataFixerEntry getFixerEntry() {
        return dataFixer;
    }

    @Override
    public Schema createBaseSchema() {
        return new V0(0, this.latestVanillaSchema);
    }

    @Override
    public <T> Dynamic<T> updateWithAllFixers(DSL.TypeReference type, Dynamic<T> dynamic) {
        if (dataFixer == null) {
            return dynamic;
        }
        int modDataVersion = DataFixesInternals.getGTDataVersion(dynamic);
        return dataFixer.dataFixer().update(type, dynamic, modDataVersion, dataFixer.currentVersion());
    }

    @Override
    public CompoundTag addGTDataVersion(CompoundTag compound) {
        if (dataFixer != null)
            compound.putInt(GT_DATA_VERSION_TAG, dataFixer.currentVersion());

        return compound;
    }
}
