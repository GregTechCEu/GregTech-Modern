package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;

import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SteamHullMachineModel extends MachineModel {

    public SteamHullMachineModel(boolean isHighTier, ResourceLocation modelLocation) {
        this(isHighTier ? "bricked_steel" : "bricked_bronze", modelLocation);
    }

    public SteamHullMachineModel(String name, ResourceLocation modelLocation) {
        super(modelLocation);
        setTextureOverride(Map.of(
                "bottom", GTCEu.id("block/casings/steam/%s/bottom".formatted(name)),
                "top", GTCEu.id("block/casings/steam/%s/top".formatted(name)),
                "side", GTCEu.id("block/casings/steam/%s/side".formatted(name))));
    }

    @NotNull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureOverride.get("side"));
    }
}
