package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;

import java.util.UUID;

@Info("""
        Invoked when the server is first loaded.
        """)
public class RegisterCapesEventJS extends EventJS {

    private final RegisterGTCapesEvent event;

    public RegisterCapesEventJS(RegisterGTCapesEvent event) {
        this.event = event;
    }

    @Info(value = """
            Registers a cape.
            """,
          params = {
                  @Param(name = "id", value = "An identifier for the cape"),
                  @Param(name = "texture", value = "The full path to the cape's texture in a resource pack")
          })
    public void registerCape(ResourceLocation id, ResourceLocation texture) {
        event.registerCape(id, texture);
    }

    @Info(value = """
            Registers a cape that will always be unlocked for all players.
            """,
          params = {
                  @Param(name = "id", value = "An identifier for the cape"),
                  @Param(name = "texture", value = "The full path to the cape's texture in a resource pack")
          })
    public void registerFreeCape(ResourceLocation id, ResourceLocation texture) {
        event.registerFreeCape(id, texture);
    }

    @Info(value = """
            Automatically makes a cape available to a player.
            """,
          params = {
                  @Param(name = "owner", value = "The UUID of the player to give the cape to."),
                  @Param(name = "capeId", value = "The cape to give")
          })
    public void unlockCapeFor(UUID owner, ResourceLocation capeId) {
        event.unlockCapeFor(owner, capeId);
    }
}
