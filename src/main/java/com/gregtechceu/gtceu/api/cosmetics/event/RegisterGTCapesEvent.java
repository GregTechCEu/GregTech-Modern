package com.gregtechceu.gtceu.api.cosmetics.event;

import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * RegisterGTCapesEvent is fired when the server is first loaded.<br>
 * It can be used to make additional capes available to the player in
 * {@link CapeRegistry#registerCape(Identifier, Identifier)}
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
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
    public void registerCape(Identifier id, Identifier texture) {
        CapeRegistry.registerCape(id, texture);
    }

    /**
     * Registers a cape that will always be unlocked for all players.
     *
     * @param id      An identifier for the cape
     * @param texture The full path to the cape's texture in a resource pack
     */
    public void registerFreeCape(Identifier id, Identifier texture) {
        CapeRegistry.registerFreeCape(id, texture);
    }

    /**
     * Automatically makes a cape available to a player.
     *
     * @param owner The UUID of the player to give the cape to
     * @param cape  The cape to give
     */
    public void unlockCapeFor(UUID owner, Identifier cape) {
        CapeRegistry.unlockCape(owner, cape);
    }
}
