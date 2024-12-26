package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.ore;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.WaypointImpl;
import dev.ftb.mods.ftbchunks.client.map.WaypointType;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;

import java.util.function.Function;

public class OreVeinIcon implements MapIcon {

    protected final GeneratedVeinMetadata veinMetadata;
    protected String name = null;

    public OreVeinIcon(GeneratedVeinMetadata veinMetadata) {
        this.veinMetadata = veinMetadata;
    }

    public boolean isEnabled() {
        return FTBChunksPlugin.getInstance().getOptions().showLayer("ore_veins") &&
                !(veinMetadata.depleted() && FTBChunksPlugin.getInstance().getOptions().hideDepleted());
    }

    public String getName() {
        return name == null ? name = OreRenderLayer.getName(veinMetadata).getString() : name;
    }

    public Material getMaterial() {
        Material firstMaterial = null;
        if (!veinMetadata.definition().indicatorGenerators().isEmpty()) {
            var blockOrMaterial = veinMetadata.definition().indicatorGenerators().get(0).block();
            firstMaterial = blockOrMaterial == null ? null : blockOrMaterial.map(
                    state -> {
                        var matStack = ChemicalHelper.getMaterial(state.getBlock());
                        return matStack == null ? null : matStack.material();
                    },
                    Function.identity());
        }
        if (firstMaterial == null) {
            firstMaterial = veinMetadata.definition().veinGenerator().getAllMaterials().get(0);
        }
        return firstMaterial;
    }

    @Override
    public Vec3 getPos(float v) {
        return veinMetadata.center().getCenter();
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        if (!isEnabled()) {
            return false;
        }
        MapDimension.getCurrent()
                .ifPresent(mapDimension -> FTBChunksPlugin.getInstance().getClientAPI()
                        .getWaypointManager(mapDimension.dimension)
                        .ifPresent(waypointManager -> {
                            var waypoint = new WaypointImpl(WaypointType.DEFAULT, mapDimension, veinMetadata.center());
                            if (!waypointManager.getAllWaypoints().contains(waypoint)) {
                                waypointManager.addWaypointAt(veinMetadata.center(), getName())
                                        .setColor(getMaterial().getMaterialARGB())
                                        .setHidden(false);
                                baseScreen.openGui();
                            }
                        }));
        return true;
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        if (!isEnabled()) {
            return false;
        }

        if (key.is(InputConstants.KEY_DELETE)) {
            veinMetadata.depleted(!veinMetadata.depleted());
        }
        return true;
    }

    @Override
    public void addTooltip(TooltipList list) {
        if (!isEnabled()) {
            return;
        }

        OreRenderLayer.getTooltip(getName(), veinMetadata).forEach(list::add);
    }

    @Override
    public void draw(MapType mapType, GuiGraphics graphics, int x, int y, int w, int h, boolean outsideVisibleArea,
                     int iconAlpha) {
        if (outsideVisibleArea || !isEnabled()) {
            return;
        }

        var iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;
        var firstMaterial = getMaterial();
        var colors = DrawUtil.floats(firstMaterial.getMaterialARGB());
        RenderSystem.setShaderColor(1, 1, 1, 1);

        var oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(),
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
        var borderColor = ConfigHolder.INSTANCE.compat.minimap
                .getBorderColor(firstMaterial.getMaterialARGB() | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            var thickness = iconSize / 16;
            graphics.fill(x, y, x + w, y + h + thickness, borderColor);
            graphics.fill(x, y - thickness, x + w, y + h + thickness, borderColor);
            graphics.fill(x, y, x + w + thickness, y + h, borderColor);
            graphics.fill(x - thickness, y, x + w + thickness, y + h, borderColor);
        }
    }
}
