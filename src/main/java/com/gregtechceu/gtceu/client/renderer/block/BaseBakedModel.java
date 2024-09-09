package com.gregtechceu.gtceu.client.renderer.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.IDynamicBakedModel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote BaseBakedModel
 */
public abstract class BaseBakedModel implements IDynamicBakedModel {

    public static final Set<BaseBakedModel> LISTENERS = new HashSet<>();

    public void onAdditionalModel(Consumer<ResourceLocation> consumer) {}

    public void registerEvent() {
        LISTENERS.add(this);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(MissingTextureAtlasSprite.getLocation());
    }
}
