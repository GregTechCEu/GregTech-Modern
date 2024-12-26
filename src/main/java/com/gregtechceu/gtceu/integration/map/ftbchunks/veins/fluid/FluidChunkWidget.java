package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;

import java.util.List;

public class FluidChunkWidget extends Widget {

    private final XZ chunkPos;
    private final ProspectorMode.FluidInfo fluidInfo;

    private List<Component> tooltip;
    private String name;

    public FluidChunkWidget(RegionMapPanel panel, XZ chunkPos,
                            ProspectorMode.FluidInfo fluidInfo) {
        super(panel);
        setSize(FTBChunks.TILE_SIZE, FTBChunks.TILE_SIZE);
        this.chunkPos = chunkPos;
        this.fluidInfo = fluidInfo;
    }

    public List<Component> getTooltip() {
        return tooltip == null ? tooltip = FluidRenderLayer.getTooltip(fluidInfo) : tooltip;
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos(chunkPos.x(), chunkPos.z());
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
        getTooltip().forEach(list::add);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (!shouldDraw()) {
            return;
        }

        Icon.getIcon(IClientFluidTypeExtensions.of(fluidInfo.fluid()).getStillTexture())
                .withColor(Color4I.rgba(getColor()))
                .draw(graphics, x, y, FTBChunks.TILE_SIZE, FTBChunks.TILE_SIZE);
    }
}
