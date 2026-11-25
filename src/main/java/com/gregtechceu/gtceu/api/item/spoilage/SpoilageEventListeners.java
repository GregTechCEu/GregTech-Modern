package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class SpoilageEventListeners {

    public static void init() {
        if (SpoilUtils.spoilageEnabled()) {
            NeoForge.EVENT_BUS.register(SpoilageEventListeners.class);
        }
    }

    @SubscribeEvent
    public static void addSpoilageTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        List<Component> tooltips = event.getToolTip();
        TooltipFlag flag = event.getFlags();

        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable instanceof IAddInformation withTooltip) {
            withTooltip.appendHoverText(stack, event.getContext(), tooltips, flag);
        }
    }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockSpoilTicker.get(level).tick();

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

        BlockSpoilTicker.get(level).chunkLoaded(chunk);
    }

    @SubscribeEvent
    public static void chunkUnloaded(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (!(chunk.getLevel() instanceof ServerLevel level)) return;

        BlockSpoilTicker.get(level).chunkUnloaded(chunk);
    }
}
