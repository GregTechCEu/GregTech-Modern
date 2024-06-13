package com.gregtechceu.gtceu.data.datamap;

import com.gregtechceu.gtceu.data.recipe.misc.ComposterRecipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

public class GTDataMaps extends DataMapProvider {

    public GTDataMaps(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterRecipes.addComposterRecipes((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(),
                new Compostable(chance), false));
    }
}
