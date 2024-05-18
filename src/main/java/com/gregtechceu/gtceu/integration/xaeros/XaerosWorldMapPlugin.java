package com.gregtechceu.gtceu.integration.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xaero.map.mods.gui.Waypoint;

public class XaerosWorldMapPlugin {
    public static boolean isActive;
    public static void init(){
        isActive = true;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        BlockPos pos = event.getPos();
        GTCEu.LOGGER.info("Right clicked pressed at ${}", pos.getX());
//        Waypoint waypoint = new Waypoint(pos.getX(), pos.getY() + 2, pos.getZ(), "test", "XD", 2, 0, false);
    }
}
