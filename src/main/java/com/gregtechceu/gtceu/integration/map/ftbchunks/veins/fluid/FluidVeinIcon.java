package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftbchunks.client.gui.MapIconWidget;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.WaypointImpl;
import dev.ftb.mods.ftbchunks.client.map.WaypointType;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import lombok.Getter;
import lombok.Setter;

public class FluidVeinIcon implements MapIcon {

    @Getter
    private final ChunkPos chunkPos;
    @Getter
    private final ProspectorMode.FluidInfo fluidInfo;
    @Setter
    private int size;
    @Setter
    private MapIconWidget widget;

    public FluidVeinIcon(ChunkPos chunkPos, ProspectorMode.FluidInfo fluidInfo) {
        this.chunkPos = chunkPos;
        this.fluidInfo = fluidInfo;
        this.size = 1;
        this.widget = null;
    }

    public boolean isEnabled() {
        return FTBChunksOptions.showLayer("bedrock_fluids") &&
                (fluidInfo.left() > 0 || !FTBChunksOptions.hideDepleted());
    }

    public String getName() {
        return FluidRenderLayer.getName(fluidInfo).getString();
    }

    public int getColor() {
        var color = IClientFluidTypeExtensions.of(fluidInfo.fluid()).getTintColor();
        var material = ChemicalHelper.getMaterial(fluidInfo.fluid());
        if (material != null) {
            color = material.getMaterialARGB();
        }
        return color;
    }

    @Override
    public double getIconScale(MapType mapType) {
        return mapType.isMinimap() ? 1 : size;
    }

    @Override
    public boolean isZoomDependant(MapType mapType) {
        return false;
    }

    @Override
    public Vec3 getPos(float v) {
        return getMiddleBlock().getCenter();
    }

    public BlockPos getMiddleBlock() {
        return chunkPos.getMiddleBlockPosition(70);
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        if (!isEnabled()) {
            return false;
        }

        MapDimension.getCurrent()
                .ifPresent(mapDimension -> FTBChunksAPI.clientApi()
                        .getWaypointManager(mapDimension.dimension)
                        .ifPresent(waypointManager -> {
                            var pos = getMiddleBlock();
                            var waypoint = new WaypointImpl(WaypointType.DEFAULT, mapDimension, pos);
                            if (!waypointManager.getAllWaypoints().contains(waypoint)) {
                                waypointManager.addWaypointAt(pos, getName())
                                        .setColor(getColor())
                                        .setHidden(false);
                                baseScreen.refreshWidgets();
                            }
                        }));
        return true;
    }

    @Override
    public void addTooltip(TooltipList list) {
        FluidRenderLayer.getTooltip(fluidInfo).forEach(list::add);
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        return false;
    }

    @Override
    public void draw(MapType mapType, GuiGraphics graphics, int x, int y, int w, int h, boolean outsideVisibleArea,
                     int iconAlpha) {
        if (outsideVisibleArea || !isEnabled()) {
            return;
        }

        var mouseOver = false;
        if (widget != null) {
            mouseOver = widget.isMouseOver();
        }
        var fluidIcon = Icon.getIcon(IClientFluidTypeExtensions.of(fluidInfo.fluid()).getStillTexture())
                .withColor(Color4I.rgba(getColor()).withAlphaf(mouseOver ? 0.9f : 0.8f));
        if (mouseOver) {
            fluidIcon = fluidIcon.withBorder(Color4I.rgba(getColor()), false);
        }
        fluidIcon.draw(graphics, x, y, w, h);
    }
}
