package com.gregtechceu.gtceu.integration.forestry.client;

import com.gregtechceu.gtceu.integration.forestry.items.GTApicultureItems;
import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.api.client.IForestryClientApi;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.core.models.ClientManager;
import forestry.modules.ModuleUtil;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForestryClientHandler implements IClientModuleHandler {

    @Override
    public void registerEvents(IEventBus modBus) {

        modBus.addListener(ForestryClientHandler::registerItemColors);


        ModuleUtil.getModBus(ForestryConstants.MOD_ID).addListener(EventPriority.HIGHEST, ((ForestryClientApiImpl) IForestryClientApi.INSTANCE)::initializeTextureManager);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {

        event.register(ClientManager.FORESTRY_ITEM_COLOR,
                ApicultureItems.BEE_QUEEN.item(),
                ApicultureItems.BEE_DRONE.item(),
                ApicultureItems.BEE_PRINCESS.item(),
                ApicultureItems.BEE_LARVAE.item()
        );

        event.register(ClientManager.FORESTRY_ITEM_COLOR, GTApicultureItems.BEE_COMBS.itemArray());
    }
}
