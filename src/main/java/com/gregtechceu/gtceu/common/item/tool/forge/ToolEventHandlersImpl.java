package com.gregtechceu.gtceu.common.item.tool.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID)
public class ToolEventHandlersImpl {
    public static int fireItemPickupEvent(ItemEntity drop, Player player) {
        return ForgeEventFactory.onItemPickup(drop, player);
    }

    @SubscribeEvent
    public static void onPlayerDestroyItem(@NotNull PlayerDestroyItemEvent event) {
        ToolEventHandlers.onPlayerDestroyItem(event.getOriginal(), event.getHand(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(@NotNull PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ToolEventHandlers.onPlayerEntityInteract(event.getEntity(), event.getHand(), event.getTarget());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        if (!ToolEventHandlers.onAnvilUpdateEvent(event.getLeft(), event.getRight())) {
            event.setCanceled(true);
        }
    }
}
