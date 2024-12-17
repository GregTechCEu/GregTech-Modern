package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class KJSWrappingMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition> {

    @HideFromJS
    @Getter(onMethod_ = @HideFromJS)
    private final KJSTieredMultiblockBuilder tieredBuilder;

    public KJSWrappingMultiblockBuilder(ResourceLocation id, KJSTieredMultiblockBuilder tieredBuilder) {
        super(id);
        this.tieredBuilder = tieredBuilder;
    }

    public KJSWrappingMultiblockBuilder tiers(int... tiers) {
        tieredBuilder.tiers(tiers);
        return this;
    }

    public KJSWrappingMultiblockBuilder machine(KJSTieredMultiblockBuilder.TieredCreationFunction machine) {
        tieredBuilder.machine(machine);
        return this;
    }

    public KJSWrappingMultiblockBuilder definition(KJSTieredMultiblockBuilder.DefinitionFunction definition) {
        tieredBuilder.definition(definition);
        return this;
    }

    @Override
    public MultiblockMachineDefinition register() {
        tieredBuilder.register();
        for (var def : tieredBuilder.get()) {
            if (def != null) {
                return value = def;
            }
        }
        // should never happen.
        throw new IllegalStateException("Empty tiered multiblock builder " + Arrays.toString(tieredBuilder.get()) +
                " With id " + tieredBuilder.id);
    }
}
