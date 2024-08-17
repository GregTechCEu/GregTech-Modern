package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.integration.jade.provider.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.List;
import java.util.Objects;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ElectricContainerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new WorkableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ControllableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeLogicProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ParallelProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeOutputProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MultiblockStructureProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MaintenanceBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ExhaustVentBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new AutoOutputBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new CableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MachineModeProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new StainedColorProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new HazardCleanerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new TransformerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new PrimitivePumpBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MEPatternBufferProxyProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MEPatternBufferProvider(), BlockEntity.class);

        registration.registerFluidStorage(FluidPipeStorageProvider.INSTANCE, FluidPipeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ElectricContainerBlockProvider(), Block.class);
        registration.registerBlockComponent(new WorkableBlockProvider(), Block.class);
        registration.registerBlockComponent(new ControllableBlockProvider(), Block.class);
        registration.registerBlockComponent(new RecipeLogicProvider(), Block.class);
        registration.registerBlockComponent(new ParallelProvider(), Block.class);
        registration.registerBlockComponent(new RecipeOutputProvider(), Block.class);
        registration.registerBlockComponent(new MultiblockStructureProvider(), Block.class);
        registration.registerBlockComponent(new MaintenanceBlockProvider(), Block.class);
        registration.registerBlockComponent(new ExhaustVentBlockProvider(), Block.class);
        registration.registerBlockComponent(new AutoOutputBlockProvider(), Block.class);
        registration.registerBlockComponent(new CableBlockProvider(), Block.class);
        registration.registerBlockComponent(new MachineModeProvider(), Block.class);
        registration.registerBlockComponent(new StainedColorProvider(), Block.class);
        registration.registerBlockComponent(new HazardCleanerBlockProvider(), Block.class);
        registration.registerBlockComponent(new TransformerBlockProvider(), Block.class);
        registration.registerBlockComponent(new PrimitivePumpBlockProvider(), Block.class);
        registration.registerBlockComponent(new MEPatternBufferProxyProvider(), Block.class);
        registration.registerBlockComponent(new MEPatternBufferProvider(), Block.class);

        registration.registerFluidStorageClient(FluidPipeStorageProvider.INSTANCE);
    }

    static {
        GTItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
            if (type.toolDefinition.getTool().rules().isEmpty() || map.isEmpty()) return;

            List<Item> tools = map
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(ItemProviderEntry::isBound)
                    .map(ItemProviderEntry::asItem)
                    .toList();
            if (tools.isEmpty()) return;
            HarvestToolProvider.registerHandler(SimpleToolHandler.create(GTCEu.id(type.name), true, tools));
        });
    }
}
