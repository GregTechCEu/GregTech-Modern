package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.map.MapManager;
import dev.ftb.mods.ftbchunks.client.map.WaypointImpl;
import dev.ftb.mods.ftbchunks.client.map.WaypointType;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OreVeinIcon implements MapIcon {

    protected final GeneratedVeinMetadata veinMetadata;
    protected String name = null;

    public OreVeinIcon(GeneratedVeinMetadata veinMetadata) {
        this.veinMetadata = veinMetadata;
    }

    @Override
    public double getIconScale(MapType mapType) {
        return mapType.isLargeMap() ? (double) ConfigHolder.INSTANCE.compat.minimap.oreIconSize / 8 :
                MapIcon.super.getIconScale(mapType);
    }

    public boolean isEnabled() {
        return FTBChunksOptions.showLayer("ore_veins") &&
                !(veinMetadata.depleted() && FTBChunksOptions.hideDepleted());
    }

    public String getName() {
        return name == null ? name = OreRenderLayer.getName(veinMetadata).getString() : name;
    }

    public @NotNull Material getMaterial() {
        return OreRenderLayer.getMaterial(veinMetadata);
    }

    @Override
    public Vec3 getPos(float v) {
        return veinMetadata.center().getCenter();
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        if (!isEnabled() || !(baseScreen instanceof LargeMapScreen largeMapScreen)) {
            return false;
        }
        if (mouseButton.isLeft()) {
            toggleWaypoint(largeMapScreen);
            return true;
        } else if (mouseButton.isRight()) {
            openContextMenu(largeMapScreen);
            return true;
        }
        return false;
    }

    private void openContextMenu(LargeMapScreen screen) {
        var title = Component.literal(getName());
        if (veinMetadata.depleted()) {
            title.append(" (").append(Component.translatable("gtceu.minimap.ore_vein.depleted")).append(")");
        }
        var markDepleted = new ContextMenuItem(Component.translatable("button.gtceu.mark_as_depleted.name"),
                Icons.REMOVE,
                b -> veinMetadata.depleted(!veinMetadata.depleted()));
        var material = getMaterial();
        var color = material.isNull() ? Color4I.rgba(0xFFFFFFFF) : Color4I.rgba(material.getMaterialARGB());
        var waypointIcon = WaypointType.DEFAULT.getIcon().withColor(color);
        var toggleWaypoint = new ContextMenuItem(Component.translatable("button.gtceu.toggle_waypoint.name"),
                waypointIcon,
                b -> toggleWaypoint(screen));
        var contextMenu = List.of(
                ContextMenuItem.title(title),
                ContextMenuItem.SEPARATOR,
                markDepleted,
                toggleWaypoint);
        screen.openContextMenu(contextMenu);
    }

    public void toggleWaypoint(LargeMapScreen screen) {
        var dimension = screen.currentDimension();
        var mapManager = MapManager.getInstance().orElse(null);
        var waypointManager = FTBChunksAPI.clientApi().getWaypointManager(dimension)
                .orElse(null);
        if (mapManager == null || waypointManager == null) return;
        var waypoint = new WaypointImpl(WaypointType.DEFAULT, mapManager.getDimension(dimension),
                veinMetadata.center());
        if (waypointManager.getAllWaypoints().contains(waypoint)) {
            waypointManager.removeWaypoint(waypoint);
        } else {
            var material = getMaterial();
            var color = material.isNull() ? 0xFFFFFFFF : material.getMaterialARGB();
            waypointManager.addWaypointAt(veinMetadata.center(), getName())
                    .setColor(color)
                    .setHidden(false);
        }
        screen.refreshWidgets();
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        if (!isEnabled()) {
            return false;
        }

        if (key.is(InputConstants.KEY_DELETE)) {
            veinMetadata.depleted(!veinMetadata.depleted());
            return true;
        }
        return false;
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
        var material = getMaterial();
        var color = material.isNull() ? 0xFFFFFFFF : material.getMaterialARGB();
        var colors = DrawUtil.floats(color);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        var iconSet = material.isNull() ? MaterialIconSet.METALLIC : material.getMaterialIconSet();
        var oreTexture = MaterialIconType.rawOre.getItemTexturePath(iconSet,
                true);
        if (oreTexture != null) {
            var oreSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(oreTexture);
            graphics.blit(x, y, 0, w, h, oreSprite, colors[0], colors[1], colors[2], 1);
        }
        oreTexture = MaterialIconType.rawOre.getItemTexturePath(iconSet, "secondary", true);
        if (oreTexture != null) {
            var materialSecondaryARGB = material.isNull() ? 0xFFFFFFFF : material.getMaterialSecondaryARGB();
            colors = DrawUtil.floats(materialSecondaryARGB);
            var oreSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(oreTexture);
            graphics.blit(x, y, 0, w, h, oreSprite, colors[0], colors[1], colors[2], 1);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        var borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(color | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            var thickness = iconSize / 16;
            graphics.fill(x, y, x + w, y + h + thickness, borderColor);
            graphics.fill(x, y - thickness, x + w, y + h + thickness, borderColor);
            graphics.fill(x, y, x + w + thickness, y + h, borderColor);
            graphics.fill(x - thickness, y, x + w + thickness, y + h, borderColor);
        }
    }
}
