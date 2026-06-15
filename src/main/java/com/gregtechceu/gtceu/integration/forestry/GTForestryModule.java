package com.gregtechceu.gtceu.integration.forestry;

import com.gregtechceu.gtceu.integration.forestry.client.ForestryClientHandler;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.IForestryModule;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@ForestryModule
public class GTForestryModule implements IForestryModule {
    @Override
    public ResourceLocation getId() {
        return GTForestryPlugin.ID;
    }

    @Override
    public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
        registrar.accept(new ForestryClientHandler());
    }
}
