package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.jade.provider.ControllableBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.ElectricContainerBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;
import com.gregtechceu.gtceu.integration.jade.provider.WorkableBlockProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.Objects;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ElectricContainerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new WorkableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ControllableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeLogicProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ElectricContainerBlockProvider(), Block.class);
        registration.registerBlockComponent(new WorkableBlockProvider(), Block.class);
        registration.registerBlockComponent(new ControllableBlockProvider(), Block.class);
        registration.registerBlockComponent(new RecipeLogicProvider(), Block.class);
    }

    static {
        GTItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
            if (type.harvestTag.location().getNamespace().equals("minecraft")) return;
            HarvestToolProvider.registerHandler(new SimpleToolHandler(type.name, type.harvestTag, map.values().stream().filter(Objects::nonNull).filter(ItemEntry::isPresent).map(ItemEntry::get).toArray(GTToolItem[]::new)));
        });
    }
}
