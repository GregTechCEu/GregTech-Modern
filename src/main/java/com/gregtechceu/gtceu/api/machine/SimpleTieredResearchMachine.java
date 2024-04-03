package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;

public class SimpleTieredResearchMachine extends SimpleTieredMachine {
    @Persisted
    public final NotifiableComputationContainer importComputation;
    @Persisted
    public final NotifiableComputationContainer exportComputation;

    public SimpleTieredResearchMachine(IMachineBlockEntity holder, int tier, Int2LongFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
        this.importComputation = createImportComputationContainer(args);
        this.exportComputation = createExportComputationContainer(args);
    }

    protected NotifiableComputationContainer createImportComputationContainer(Object... args) {
        boolean transmitter = true;
        if (args.length > 0 && args[args.length - 1] instanceof Boolean isTransmitter) {
            transmitter = isTransmitter;
        }
        return new NotifiableComputationContainer(this, IO.IN, transmitter);
    }

    protected NotifiableComputationContainer createExportComputationContainer(Object... args) {
        return new NotifiableComputationContainer(this, IO.OUT, false);
    }
}
