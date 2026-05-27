/*
 * Copyright (C) 2023 embeddedt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gregtechceu.gtceu.api.datafixer;

import com.gregtechceu.gtceu.GTCEu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Original from
 * <a href=
 * "https://github.com/embeddedt/ModernFix/blob/c561d818f398f9b9f6b826db34b705a97c187f74/src/main/java/org/embeddedt/modernfix/dfu/LazyDataFixer.java">ModernFix</a>
 */
public class LazyDataFixer implements DataFixer {
    private @Nullable DataFixer backingDataFixer;
    private final Supplier<@Nullable DataFixer> dfuSupplier;

    public LazyDataFixer(Supplier<@Nullable DataFixer> dfuSupplier) {
        this.backingDataFixer = null;
        this.dfuSupplier = dfuSupplier;
    }

    private DataFixer getDataFixer() {
        synchronized(this) {
            if (this.backingDataFixer == null) {
                GTCEu.LOGGER.info("Instantiating GTCEu Data Fixers");
                this.backingDataFixer = this.dfuSupplier.get();
            }
            if (this.backingDataFixer == null) {
                this.backingDataFixer = EmptyDataFixer.INSTANCE;
            }
        }

        return this.backingDataFixer;
    }

    public <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> input, int version, int newVersion) {
        return version >= newVersion ? input : this.getDataFixer().update(type, input, version, newVersion);
    }

    public Schema getSchema(int key) {
        return this.getDataFixer().getSchema(key);
    }
}
