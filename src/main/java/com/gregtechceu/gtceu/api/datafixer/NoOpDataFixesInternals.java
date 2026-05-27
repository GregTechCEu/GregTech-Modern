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

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Range;

public class NoOpDataFixesInternals extends DataFixesInternals {

    private final Schema schema;

    public NoOpDataFixesInternals() {
        schema = new EmptySchema(0);
    }

    @Override
    public void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion, DataFixer dataFixer) {}

    @Override
    public Schema createBaseSchema() {
        return schema;
    }

    @Override
    public <T> Dynamic<T> updateWithAllFixers(DSL.TypeReference dataFixTypes, Dynamic<T> dynamic) {
        return dynamic;
    }

    @Override
    public CompoundTag addGTDataVersion(CompoundTag compound) {
        return compound;
    }
}
