package com.gregtechceu.gtceu.data.forge;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author KilaBash
 * @date 2023/7/20
 * @implNote GTRegistriesDatapackGenerator
 */
public class GTRegistriesDatapackGenerator extends DatapackBuiltinEntriesProvider {

    private final String name;

    public GTRegistriesDatapackGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, RegistrySetBuilder datapackEntriesBuilder, Set<String> modIds, String name) {
        super(output, registries, datapackEntriesBuilder, modIds);
        this.name = name;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }
}
