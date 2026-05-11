package com.gregtechceu.gtceu.common.mui.widgets.prospector;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectingUpdatePacket;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.common.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IKey;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import com.google.common.base.Strings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProspectorMapHandler<T> extends Widget<ProspectorMapHandler<T>> implements Interactable {

    private static final BorderDrawable WHITE_BORDER = new BorderDrawable(0xFFFFFFFF, -1);

    @Getter
    private final ProspectorMode<T> mode;
    @Getter
    private final int chunkRadius;
    private final int scanInterval;

    @Getter
    private final Player player;
    @Getter
    private final ChunkPos playerChunkPos;

    private final StringValue searchValue;
    private final DynamicSyncHandler syncHandler;
    @Getter
    private final ProspectorMapTexture<T> texture;

    // runtime
    @Getter
    private @Nullable String selected = null;
    private final Set<T> items = new HashSet<>();
    private int chunkIndex = 0;

    public ProspectorMapHandler(ProspectorMode<T> mode, int chunkRadius, int scanInterval,
                                StringValue searchValue, DynamicSyncedWidget<?> searchListWidget,
                                PanelSyncManager panelSyncManager, Player player) {
        super();
        this.mode = mode;
        this.chunkRadius = chunkRadius;
        this.scanInterval = scanInterval;

        this.searchValue = searchValue;
        this.syncHandler = createListSyncHandler();
        searchListWidget.syncHandler(this.syncHandler);

        this.player = player;
        this.playerChunkPos = player.chunkPosition();

        this.texture = new ProspectorMapTexture<>(this);
        background(this.texture);
        size(this.texture.getImageWidth(), this.texture.getImageHeight());

        panelSyncManager.onServerTick(this::scanOres);
    }

    private DynamicSyncHandler createListSyncHandler() {
        return new DynamicSyncHandler()
                .widgetProvider((syncManager, buf) -> {
                    ProspectingUpdatePacket<T> packet = ProspectingUpdatePacket.read(this.mode, buf);
                    if (syncManager.isClient()) {
                        this.texture.updateTexture(packet);
                    }
                    this.addOresToList(packet.data);

                    return new ListWidget<>()
                            .collapseDisabledChildren()
                            .expanded()
                            .sizeRel(1f)
                            .children(this.items, item -> {
                                String uniqueId = mode.getUniqueId(item);
                                Component description = mode.getDescription(item);

                                BoolValue.Dynamic selected = new BoolValue.Dynamic(
                                        () -> Objects.equals(this.getSelected(), uniqueId),
                                        v -> this.setSelected(v ? uniqueId : null, syncManager.isClient()));

                                return new ToggleButton().widgetTheme(IThemeApi.TOGGLE_BUTTON)
                                        .value(selected)
                                        .widthRel(1f).height(18)
                                        .background(GuiTextures.BUTTON_CLEAN)
                                        .selectedBackground(WHITE_BORDER)
                                        .setEnabledIf(w -> {
                                            String searched = searchValue.getStringValue();
                                            if (Strings.isNullOrEmpty(searched)) {
                                                return true;
                                            } else {
                                                return description.getString().toLowerCase().contains(searched);
                                            }
                                        })
                                        .child(Flow.row()
                                                .sizeRel(1f)
                                                .padding(4, 0)
                                                // .marginBottom(-1)
                                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                .child(mode.getItemIcon(item).asWidget()
                                                        .verticalCenter().leftRel(0f)
                                                        .size(12))
                                                .child(new ScrollingTextWidget(IKey.lang(description))
                                                        .textAlign(Alignment.CenterLeft)
                                                        .verticalCenter()
                                                        // .expanded()
                                                        .margin(1)
                                                        .left(20).right(2)
                                                        .invisible()));
                            });
                });
    }

    private void scanOres() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        Level level = this.player.level();
        int chunkDiameter = this.chunkRadius * 2 - 1;

        if (this.player.tickCount % this.scanInterval == 0 && this.chunkIndex < chunkDiameter * chunkDiameter) {
            int row = this.chunkIndex / chunkDiameter;
            int column = this.chunkIndex % chunkDiameter;

            int ox = column - this.chunkRadius + 1;
            int oz = row - this.chunkRadius + 1;

            LevelChunk chunk = level.getChunk(this.playerChunkPos.x + ox, this.playerChunkPos.z + oz);
            if (mode == ProspectorMode.ORE) {
                ServerCache.instance.prospectAllInChunk(level.dimension(), chunk.getPos(), serverPlayer);
            }

            ProspectingUpdatePacket<T> packet = new ProspectingUpdatePacket<>(this.playerChunkPos.x + ox,
                    this.playerChunkPos.z + oz, mode);
            mode.scan(packet.data, chunk);
            this.syncHandler.notifyUpdate(packet::writePacketData);

            this.chunkIndex++;
        }
    }

    private void addOresToList(T[][][] data) {
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                Collections.addAll(this.items, data[x][z]);
            }
        }
    }

    public void setSelected(@Nullable String uniqueID, boolean isClient) {
        if (!Objects.equals(this.selected, uniqueID)) {
            this.selected = uniqueID;

            if (isClient) {
                this.texture.loadToImage();
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        if (!WaypointManager.isActive()) return Result.IGNORE;

        WaypointItem clickedItem = getClickedVein(mouseX, mouseY);
        if (clickedItem == null) return Result.ACCEPT;

        WaypointManager.setWaypoint(clickedItem.uniqueId,
                clickedItem.name.getString(), clickedItem.color,
                player.level().dimension(), clickedItem.position);
        player.displayClientMessage(
                Component.translatable("behavior.prospector.added_waypoint",
                        clickedItem.name.copy().withStyle(style -> style.withColor(clickedItem.color))),
                false);

        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    private @Nullable WaypointItem getClickedVein(double mouseX, double mouseY) {
        int chunkX = (int) (mouseX - getArea().x()) / 16;
        int chunkZ = (int) (mouseY - getArea().y()) / 16;
        int offsetX = (int) (mouseX - getArea().x()) % 16;
        int offsetZ = (int) (mouseY - getArea().y()) % 16;
        int xDiff = chunkX - (this.chunkRadius - 1);
        int zDiff = chunkZ - (this.chunkRadius - 1);

        int x = SectionPos.sectionToBlockCoord(player.chunkPosition().x + xDiff) + offsetX;
        int z = SectionPos.sectionToBlockCoord(player.chunkPosition().z + zDiff) + offsetZ;
        int y = player.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        if (chunkX < 0 || chunkZ < 0 || chunkX >= this.chunkRadius * 2 - 1 || chunkZ >= this.chunkRadius * 2 - 1) {
            return null;
        }

        BlockPos pos = new BlockPos(x, y, z);
        // If the ores are filtered use its name
        if (this.getSelected() != null) {
            for (T item : this.items) {
                String uniqueId = mode.getUniqueId(item);
                if (!this.getSelected().equals(uniqueId)) continue;

                Component name = mode.getDescription(item);
                int color = mode.getItemColor(item);
                return new WaypointItem(pos, uniqueId, name, color);
            }
        }

        // If the cursor is over an ore use its name
        T[] hoveredItem = this.texture.data[chunkX * mode.cellSize + (offsetX * mode.cellSize / 16)][chunkZ *
                mode.cellSize + (offsetZ * mode.cellSize / 16)];
        if (hoveredItem != null && hoveredItem.length != 0) {
            String uniqueId = mode.getUniqueId(hoveredItem[0]);
            Component name = mode.getDescription(hoveredItem[0]);
            int color = mode.getItemColor(hoveredItem[0]);
            return new WaypointItem(pos, uniqueId, name, color);
        }

        // If all else fails see if there's a nearby vein and use the vein's name
        if (mode == ProspectorMode.ORE) {
            var veins = GTClientCache.instance.getNearbyVeins(player.level().dimension(), pos, 32);
            if (veins.isEmpty()) {
                return new WaypointItem(pos, null, Component.translatable("gtceu.minimap.ore_vein.depleted"),
                        0xFF990000);
            }
            veins.sort((o1, o2) -> {
                int o1Dist = (int) o1.center().distToCenterSqr(x, o1.center().getY(), z);
                int o2Dist = (int) o2.center().distToCenterSqr(x, o2.center().getY(), z);
                return o1Dist - o2Dist;
            });
            String uniqueId = OreRenderLayer.getId(veins.get(0));
            Component name = OreRenderLayer.getName(veins.get(0));
            List<Material> materials = veins.get(0).definition().veinGenerator().getAllMaterials();
            Material mostCommonItem = materials.get(materials.size() - 1);
            int color = mostCommonItem.getMaterialARGB();
            return new WaypointItem(pos, uniqueId, name, color);
        }

        return new WaypointItem(pos, null, Component.translatable("gtceu.minimap.ore_vein.depleted"), 0xFF990000);
    }

    private record WaypointItem(BlockPos position, @Nullable String uniqueId, Component name, int color) {}
}
