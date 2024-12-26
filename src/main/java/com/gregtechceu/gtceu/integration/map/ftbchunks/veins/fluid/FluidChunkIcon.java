package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
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

import java.util.List;

@Getter
public class FluidChunkIcon implements MapIcon {

    protected ChunkPos chunkPos;
    protected ProspectorMode.FluidInfo fluidVein;

    protected String name = null;
    protected List<Component> tooltip = null;

    public FluidChunkIcon(ProspectorMode.FluidInfo fluidVein, ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
        this.fluidVein = fluidVein;
    }

    public String getName() {
        return name == null ? name = FluidRenderLayer.getName(fluidVein).getString() : name;
    }

    public int getColor() {
        var color = IClientFluidTypeExtensions.of(fluidVein.fluid()).getTintColor();
        var material = ChemicalHelper.getMaterial(fluidVein.fluid());
        if (material != null) {
            color = material.getMaterialARGB();
        }
        return (color & 0xFF) << 24 | (color >> 8 & 0xFF) << 16 | (color >> 16 & 0xFF) << 8;
    }

    public List<Component> getTooltip() {
        return tooltip == null ? tooltip = FluidRenderLayer.getTooltip(fluidVein) : tooltip;
    }

    @Override
    public void addTooltip(TooltipList list) {
        getTooltip().forEach(list::add);
    }

    @Override
    public boolean isVisible(MapType mapType, double distanceToPlayer, boolean outsideVisibleArea) {
        return GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids") &&
                MapIcon.super.isVisible(mapType, distanceToPlayer, outsideVisibleArea);
    }

    @Override
    public Vec3 getPos(float v) {
        return chunkPos.getWorldPosition().getCenter();
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        MapDimension.getCurrent()
                .ifPresent(mapDimension -> FTBChunksPlugin.getInstance().getClientAPI()
                        .getWaypointManager(mapDimension.dimension)
                        .ifPresent(waypointManager -> {
                            var pos = BlockPos.containing(getPos(0));
                            var waypoint = new WaypointImpl(WaypointType.DEFAULT, mapDimension, pos);
                            if (!waypointManager.getAllWaypoints().contains(waypoint)) {
                                waypointManager.addWaypointAt(pos, getName())
                                        .setColor(getColor())
                                        .setHidden(false);
                            }
                        }));
        return true;
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        return false;
    }

    @Override
    public void draw(MapType mapType, GuiGraphics graphics, int x, int y, int w, int h, boolean outsideVisibleArea,
                     int iconAlpha) {
        if (!FTBChunksPlugin.getInstance().getOptions().showLayer("bedrock_fluids") ||
                MapDimension.getCurrent().isEmpty()) {
            return;
        }

        var color = getColor();

        var fillOpacity = 25;
        var centerColor = color | 255 * fillOpacity / 100;
        Icon.getIcon(IClientFluidTypeExtensions.of(fluidVein.fluid()).getStillTexture())
                .withColor(Color4I.rgba(centerColor))
                .draw(graphics, x, y, FTBChunks.TILE_SIZE, FTBChunks.TILE_SIZE);
    }
}
