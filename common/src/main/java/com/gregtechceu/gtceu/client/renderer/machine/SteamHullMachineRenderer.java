package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamHullMachineRenderer
 */
public class SteamHullMachineRenderer extends MachineRenderer {

    public SteamHullMachineRenderer(boolean isHighTier, ResourceLocation modelLocation) {
        this(isHighTier ? "bricked_steel" : "bricked_bronze", modelLocation);
    }

    public SteamHullMachineRenderer(String name, ResourceLocation modelLocation) {
        super(modelLocation);
        setTextureOverride(Map.of(
                "bottom", GTCEu.id("block/casings/steam/%s/bottom".formatted(name)),
                "top", GTCEu.id("block/casings/steam/%s/top".formatted(name)),
                "side", GTCEu.id("block/casings/steam/%s/side".formatted(name))
        ));
    }

    @NotNull
    @Override
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(override.get("side"));
    }
}
