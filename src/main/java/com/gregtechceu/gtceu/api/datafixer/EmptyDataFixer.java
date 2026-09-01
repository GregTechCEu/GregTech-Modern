package com.gregtechceu.gtceu.api.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.function.Supplier;

class EmptyDataFixer implements DataFixer {

    static final EmptyDataFixer INSTANCE = new EmptyDataFixer();

    private final Schema schema = new EmptySchema(0);

    private EmptyDataFixer() {}

    @Override
    public <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> input, int version, int newVersion) {
        return input;
    }

    @Override
    public Schema getSchema(int key) {
        return this.schema;
    }

    /*
     * Copyright 2022 QuiltMC
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
    /**
     * Represents an empty {@link Schema}, having no parent and containing no type definitions.
     */
    static final class EmptySchema extends Schema {

        /**
         * Constructs an empty schema.
         *
         * @param versionKey the data version key
         */
        EmptySchema(@Range(from = 0, to = Integer.MAX_VALUE) int versionKey) {
            super(versionKey, null);
        }

        // all of these methods refer to this.parent without checking if its null
        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                                  Map<String, Supplier<TypeTemplate>> blockEntityTypes) {}

        @Override
        public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
            return Map.of();
        }

        @Override
        public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
            return Map.of();
        }

        // Ensure the schema stays empty.
        @Override
        public void registerType(boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> template) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Map<String, Type<?>> buildTypes() {
            return Object2ObjectMaps.emptyMap();
        }
    }
}
