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
@SuppressWarnings("deprecation") // why does JavaDoc not have an "internal" tag?
public class RegisterGTCapesEvent extends Event {

    @ApiStatus.Internal
    public RegisterGTCapesEvent() {}

    /**
     * Makes a cape available to the {@code /gtceu cape} command, allowing it to be used in advancements etc.
     *
     * @param id      An identifier for giving the cape with commands etc.
     * @param texture The ResourceLocation that points to the texture of the cape accessible via {@code id}
     */
    public void registerCape(ResourceLocation id, ResourceLocation texture) {
        CapeRegistry.registerCape(id, texture);
    }

    /**
     * Adds a cape that will always be unlocked for all players.
     *
     * @param id      An identifier for giving the cape with commands etc.
     * @param texture A ResourceLocation pointing to the cape texture.
     */
    public void registerFreeCape(ResourceLocation id, ResourceLocation texture) {
        CapeRegistry.registerFreeCape(id, texture);
    }

    public void unlockCapeFor(UUID owner, ResourceLocation capeId) {
        CapeRegistry.unlockCape(owner, capeId);
    }
}
