package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;

import java.util.List;

public class OreVeinIcon implements MapIcon {

    protected final GeneratedVeinMetadata veinMetadata;
    protected String name = null;
    protected List<Component> tooltip = null;

    public OreVeinIcon(GeneratedVeinMetadata veinMetadata) {
        this.veinMetadata = veinMetadata;
    }

    @Override
    public Vec3 getPos(float v) {
        return veinMetadata.center().getCenter();
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        return false;
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        if (key.is(InputConstants.KEY_DELETE)) {
            veinMetadata.depleted(!veinMetadata.depleted());
        }
        return false;
    }

    @Override
    public void addTooltip(TooltipList list) {
        if (name == null) name = OreRenderLayer.getName(veinMetadata).getString();
        OreRenderLayer.getTooltip(name, veinMetadata).forEach(list::add);
    }

    @Override
    public void draw(MapType mapType, GuiGraphics graphics, int x, int y, int w, int h, boolean outsideVisibleArea,
                     int iconAlpha) {
        var iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        var firstMaterial = veinMetadata.definition().veinGenerator().getAllMaterials().get(0);
        int materialARGB = firstMaterial.getMaterialARGB();
        var colors = DrawUtil.floats(materialARGB);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        ResourceLocation oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(),
                true);
        if (oreTexture != null) {
            var oreSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(oreTexture);
            graphics.blit(x, y, 0, w, h, oreSprite, colors[0], colors[1], colors[2], 1);
        }
        oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(), "secondary", true);
        if (oreTexture != null) {
            var materialSecondaryARGB = firstMaterial.getMaterialSecondaryARGB();
            colors = DrawUtil.floats(materialSecondaryARGB);
            var oreSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(oreTexture);
            graphics.blit(x, y, 0, w, h, oreSprite, colors[0], colors[1], colors[2], 1);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        var borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(materialARGB | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            var thickness = iconSize / 16;
            graphics.fill(x, y, x + w, y + h + thickness, borderColor);
            graphics.fill(x, y - (h / 16), x + w, y + h + thickness, borderColor);
            graphics.fill(x, y, x + w + (w / 16), y + h, borderColor);
            graphics.fill(x - thickness, y, x + w + thickness, y + h, borderColor);
        }
    }
}
