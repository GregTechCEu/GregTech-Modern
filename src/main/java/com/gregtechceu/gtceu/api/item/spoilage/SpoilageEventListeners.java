package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@ApiStatus.Internal
public class SpoilageEventListeners {

    @SubscribeEvent
    public static void addSpoilageTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        List<Component> tooltips = event.getToolTip();
        TooltipFlag flag = event.getFlags();
        if (!SpoilUtils.spoilageEnabled()) return;

        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable instanceof IAddInformation withTooltip) {
            withTooltip.appendHoverText(stack, player == null ? null : player.level(), tooltips, flag);
        }
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (!SpoilUtils.spoilageEnabled()) return;

        BlockEntitySpoilTicker.get(level).tick();

        for (Entity entity : level.getAllEntities()) {
            // process spoilage updates for all loaded entities
            SpoilUtils.updateEntity(entity);
        }
    }

    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            // the event is only given level chunks by forge, so something's gone funny
            return;
        }
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;
        if (!SpoilUtils.spoilageEnabled()) return;

        BlockEntitySpoilTicker.get(level).chunkLoaded(chunk);
    }

    @SubscribeEvent
    public static void chunkUnloaded(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;
        if (!SpoilUtils.spoilageEnabled()) return;

        BlockEntitySpoilTicker.get(level).chunkUnloaded(chunk);
    }
}
