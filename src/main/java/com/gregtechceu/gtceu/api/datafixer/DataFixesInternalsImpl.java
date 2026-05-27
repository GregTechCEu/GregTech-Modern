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
import com.gregtechceu.gtceu.common.datafixer.schemas.V0;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class DataFixesInternalsImpl extends DataFixesInternals {

    private final Supplier<Schema> latestVanillaSchema;

    private @Nullable DataFixer dataFixer;

    public DataFixesInternalsImpl(Supplier<Schema> latestVanillaSchema) {
        this.latestVanillaSchema = latestVanillaSchema;

        this.dataFixer = null;
    }

    @Override
    public void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion, DataFixer dataFixer) {
        if (this.dataFixer != null) {
            throw new IllegalArgumentException("GTCEu already has a registered data fixer");
        }

        this.dataFixer = dataFixer;
    }

    @Override
    public Schema createBaseSchema() {
        return new V0(0, this.latestVanillaSchema.get());
    }

    @Override
    public <T> Dynamic<T> updateWithAllFixers(DSL.TypeReference type, Dynamic<T> dynamic) {
        if (this.dataFixer == null) {
            return dynamic;
        }
        int modDataVersion = DataFixesInternals.getGTDataVersion(dynamic);
        return this.dataFixer.update(type, dynamic, modDataVersion, GTCEu.GT_DATA_VERSION);
    }

    @Override
    public CompoundTag addGTDataVersion(CompoundTag compound) {
        if (this.dataFixer != null) {
            compound.putInt(GT_DATA_VERSION_TAG, GTCEu.GT_DATA_VERSION);
        }

        return compound;
    }
}
