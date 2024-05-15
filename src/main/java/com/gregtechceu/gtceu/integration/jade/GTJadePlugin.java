package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.integration.jade.provider.ControllableBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.ElectricContainerBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;
import com.gregtechceu.gtceu.integration.jade.provider.WorkableBlockProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ElectricContainerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new WorkableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ControllableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeLogicProvider(), BlockEntity.class);

        registration.registerFluidStorage(FluidPipeStorageProvider.INSTANCE, FluidPipeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ElectricContainerBlockProvider(), Block.class);
        registration.registerBlockComponent(new WorkableBlockProvider(), Block.class);
        registration.registerBlockComponent(new ControllableBlockProvider(), Block.class);
        registration.registerBlockComponent(new RecipeLogicProvider(), Block.class);

        registration.registerFluidStorageClient(FluidPipeStorageProvider.INSTANCE);
    }

    /*
     * TODO fix once Jade 1.20.5 is out
     * static {
     * GTItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
     * if (type.harvestTags.isEmpty() || type.harvestTags.get(0).location().getNamespace().equals("minecraft")) return;
     * HarvestToolProvider.registerHandler(new SimpleToolHandler(GTCEu.id(type.name), true, map
     * .values()
     * .stream()
     * .filter(Objects::nonNull)
     * .filter(ItemProviderEntry::isBound)
     * .map(ItemProviderEntry::asItem)
     * .map(Item::getDefaultInstance)
     * .toList()
     * ));
     * });
     * }
     */
}
