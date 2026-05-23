package com.gregtechceu.gtceu.api.datafixer;

import com.gregtechceu.gtceu.GTCEu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.embeddedt.modernfix.dfu.DFUBlaster;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyDataFixer implements DataFixer {
    private @Nullable DataFixer backingDataFixer;
    private final Supplier<DataFixer> dfuSupplier;

    public LazyDataFixer(Supplier<DataFixer> dfuSupplier) {
        this.backingDataFixer = null;
        this.dfuSupplier = dfuSupplier;
    }

    private DataFixer getDataFixer() {
        synchronized(this) {
            if (this.backingDataFixer == null) {
                GTCEu.LOGGER.info("Instantiating GTCEu Data Fixers");
                DFUBlaster.blastMaps();
                this.backingDataFixer = this.dfuSupplier.get();
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
