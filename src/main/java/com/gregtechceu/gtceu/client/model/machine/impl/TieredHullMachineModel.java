package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class TieredHullMachineModel extends MachineModel {

    public TieredHullMachineModel(int tier, ResourceLocation modelLocation) {
        super(modelLocation);
        setTextureOverride(Map.of(
                "bottom",
                GTCEu.id("block/casings/voltage/%s/bottom".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT))),
                "top", GTCEu.id("block/casings/voltage/%s/top".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT))),
                "side",
                GTCEu.id("block/casings/voltage/%s/side".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)))));
    }

    @NotNull
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureOverride.get("side"));
    }
}
