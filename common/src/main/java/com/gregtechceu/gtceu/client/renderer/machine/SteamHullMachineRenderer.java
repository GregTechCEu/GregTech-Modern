package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.mixins.BlockModelAccessor;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamHullMachineRenderer
 */
public class SteamHullMachineRenderer extends MachineRenderer {
    protected final ResourceLocation top, bottom, side;

    public SteamHullMachineRenderer(boolean isHighTier, ResourceLocation modelLocation) {
        super(modelLocation);
        var name = isHighTier ? "bricked_steel" : "bricked_bronze";
        this.top = GTCEu.id("block/casings/steam/%s/top".formatted(name));
        this.side = GTCEu.id("block/casings/steam/%s/side".formatted(name));
        this.bottom = GTCEu.id("block/casings/steam/%s/bottom".formatted(name));
    }

    public SteamHullMachineRenderer(String name, ResourceLocation modelLocation) {
        super(modelLocation);
        this.top = GTCEu.id("block/casings/steam/%s/top".formatted(name));
        this.side = GTCEu.id("block/casings/steam/%s/side".formatted(name));
        this.bottom = GTCEu.id("block/casings/steam/%s/bottom".formatted(name));
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected UnbakedModel getModel() {
        var model = super.getModel();
        if (model instanceof BlockModelAccessor blockModelAccessor) {
            blockModelAccessor.getTextureMap().put("bottom", ModelFactory.parseBlockTextureLocationOrReference(bottom.toString()));
            blockModelAccessor.getTextureMap().put("top", ModelFactory.parseBlockTextureLocationOrReference(top.toString()));
            blockModelAccessor.getTextureMap().put("side", ModelFactory.parseBlockTextureLocationOrReference(side.toString()));
        }
        return model;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(top);
            register.accept(side);
            register.accept(bottom);
        }
    }

    @NotNull
    @Override
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(side);
    }
}
