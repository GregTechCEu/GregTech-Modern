package com.gregtechceu.gtceu.api.cosmetics.event;

import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * RegisterGTCapesEvent is fired when the server is first loaded.<br>
 * It can be used to make additional capes available to the player in
 * {@link CapeRegistry#registerCape(ResourceLocation, ResourceLocation)}
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class RegisterGTCapesEvent extends Event {

    @ApiStatus.Internal
    public RegisterGTCapesEvent() {}

    /**
     * Registers a cape to the cape registry.
     *
     * @param id      An identifier for the cape
     * @param texture The full path to the cape's texture in a resource pack
     */
    public void registerCape(ResourceLocation id, ResourceLocation texture) {
        CapeRegistry.registerCape(id, texture);
    }

    /**
     * Registers a cape that will always be unlocked for all players.
     *
     * @param id      An identifier for the cape
     * @param texture The full path to the cape's texture in a resource pack
     */
    public void registerFreeCape(ResourceLocation id, ResourceLocation texture) {
        CapeRegistry.registerFreeCape(id, texture);
    }

    /**
     * Automatically makes a cape available to a player.
     *
     * @param owner The UUID of the player to give the cape to
     * @param cape  The cape to give
     */
    public void unlockCapeFor(UUID owner, ResourceLocation cape) {
        CapeRegistry.unlockCape(owner, cape);
    }
}
