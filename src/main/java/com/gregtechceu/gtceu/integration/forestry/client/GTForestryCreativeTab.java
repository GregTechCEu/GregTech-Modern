package com.gregtechceu.gtceu.integration.forestry.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.forestry.items.GTApicultureItems;
import com.gregtechceu.gtceu.integration.forestry.items.GTCombItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GTForestryCreativeTab {

    @SubscribeEvent
    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {

        if (GTCEu.Mods.isForestryLoaded()) {
            if (event.getTabKey().location().toString().equals("gtceu:material_item")) {
                addGtCombsItems(event.getParameters(), event);
            }
        }
    }

    private static void addGtCombsItems(CreativeModeTab.ItemDisplayParameters params, BuildCreativeModeTabContentsEvent items) {
        for (GTCombItem comb : GTApicultureItems.BEE_COMBS.getItems()) {
            items.accept(new ItemStack(comb));
        }
    }

}