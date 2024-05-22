package com.gregtechceu.gtceu.integration.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xaero.map.mods.gui.Waypoint;

public class XaerosWorldMapPlugin {
    public static boolean isActive = false;
    public static void init(){
        isActive = true;
    }
}
