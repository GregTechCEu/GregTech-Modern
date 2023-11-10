package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote TieredHullMachineRenderer
 */
public class TieredHullMachineRenderer extends MachineRenderer {

    public TieredHullMachineRenderer(int tier, ResourceLocation modelLocation) {
        super(modelLocation);
        setTextureOverride(Map.of(
                "bottom", GTCEu.id("block/casings/voltage/%s/bottom".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT))),
                "top", GTCEu.id("block/casings/voltage/%s/top".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT))),
                "side", GTCEu.id("block/casings/voltage/%s/side".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)))
        ));
    }

    @NotNull
    @Override
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(override.get("side"));
    }

}
