package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.WaypointImpl;
import dev.ftb.mods.ftbchunks.client.map.WaypointType;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import lombok.Getter;

public class FluidChunkWidget extends Widget {

    @Getter
    private final ChunkPos chunkPos;
    private final ProspectorMode.FluidInfo fluidInfo;

    private String name;

    public FluidChunkWidget(RegionMapPanel panel, ChunkPos chunkPos,
                            ProspectorMode.FluidInfo fluidInfo) {
        super(panel);
        setSize(FTBChunks.TILE_SIZE, FTBChunks.TILE_SIZE);
        this.chunkPos = chunkPos;
        this.fluidInfo = fluidInfo;
    }

    public Vec3 getPos() {
        return getChunkPos().getMiddleBlockPosition(70).getCenter();
    }

    public String getName() {
        return name == null ? name = FluidRenderLayer.getName(fluidInfo).getString() : name;
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
    public boolean mouseDoubleClicked(MouseButton button) {
        if (!shouldDraw()) {
            return false;
        }

        MapDimension.getCurrent()
                .ifPresent(mapDimension -> FTBChunksPlugin.getInstance().getClientAPI()
                        .getWaypointManager(mapDimension.dimension)
                        .ifPresent(waypointManager -> {
                            var pos = BlockPos.containing(getPos());
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
    public boolean isEnabled() {
        return FTBChunksPlugin.getInstance().getOptions().showLayer("bedrock_fluids");
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!shouldDraw()) {
            return;
        }

        FluidRenderLayer.getTooltip(fluidInfo).forEach(list::add);
    }

    @Override
    public boolean shouldDraw() {
        return fluidInfo.left() > 0 || !FTBChunksPlugin.getInstance().getOptions().hideDepleted();
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (!shouldDraw()) {
            return;
        }

        var fluidIcon = Icon.getIcon(IClientFluidTypeExtensions.of(fluidInfo.fluid()).getStillTexture())
                .withColor(Color4I.rgba(getColor()).withAlphaf(isMouseOver() ? 0.9f : 0.8f));
        if (isMouseOver()) {
            fluidIcon = fluidIcon.withBorder(Color4I.rgba(getColor()), false);
        }
        fluidIcon.draw(graphics, x, y, w, h);
    }
}
