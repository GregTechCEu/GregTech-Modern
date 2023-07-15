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
                "bottom", GTCEu.id("block/casings/voltage/%s_bottom".formatted(GTValues.VN[tier].toLowerCase())),
                "top", GTCEu.id("block/casings/voltage/%s_top".formatted(GTValues.VN[tier].toLowerCase())),
                "side", GTCEu.id("block/casings/voltage/%s_side".formatted(GTValues.VN[tier].toLowerCase()))
        ));
    }

    @NotNull
    @Override
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(override.get("side"));
    }

}
