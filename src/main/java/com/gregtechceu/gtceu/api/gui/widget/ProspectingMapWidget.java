package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.common.item.ProspectorScannerBehavior;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ProspectingMapWidget extends WidgetGroup implements SearchComponentWidget.IWidgetSearch<Object> {

    private final int chunkRadius;
    private final ProspectorMode mode;
    private final int scanTick;
    @Getter
    private boolean darkMode = false;
    private final DraggableScrollableWidgetGroup itemList;
    @OnlyIn(Dist.CLIENT)
    private ProspectingTexture texture;
    private int playerChunkX;
    private int playerChunkZ;
    // runtime
    private int chunkIndex = 0;
    private final Queue<PacketProspecting> packetQueue = new LinkedBlockingQueue<>();
    private final Set<Object> items = new CopyOnWriteArraySet<>();
    private final Map<String, SelectableWidgetGroup> selectedMap = new ConcurrentHashMap<>();

    public ProspectingMapWidget(int xPosition, int yPosition, int width, int height, int chunkRadius,
                                @NotNull ProspectorMode mode, int scanTick) {
        super(xPosition, yPosition, width, height);
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;
        int imageWidth = (chunkRadius * 2 - 1) * 16;
        int imageHeight = (chunkRadius * 2 - 1) * 16;
        addWidget(new ImageWidget(0, (height - imageHeight) / 2 - 4, imageWidth + 8, imageHeight + 8,
                GuiTextures.BACKGROUND_INVERSE));
        var group = (WidgetGroup) new WidgetGroup(imageWidth + 10, 0, width - (imageWidth + 10), height)
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(itemList = new DraggableScrollableWidgetGroup(4, 28, group.getSize().width - 8,
                group.getSize().height - 32)
                .setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1)));
        group.addWidget(new SearchComponentWidget<>(6, 6, group.getSize().width - 12, 18, this));
        addWidget(group);
        addNewItem("[all]", "all resources", IGuiTexture.EMPTY, -1);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeVarInt(playerChunkX = gui.entityPlayer.chunkPosition().x);
        buffer.writeVarInt(playerChunkZ = gui.entityPlayer.chunkPosition().z);
        buffer.writeVarInt(gui.entityPlayer.getBlockX());
        buffer.writeVarInt(gui.entityPlayer.getBlockZ());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        texture = new ProspectingTexture(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                gui.entityPlayer.getVisualRotationYInDegrees(), mode, chunkRadius, darkMode);
    }

    public void setDarkMode(boolean mode) {
        if (darkMode != mode) {
            darkMode = mode;
            if (isRemote()) {
                texture.setDarkMode(darkMode);
            }
        }
    }

    private void addOresToList(Object[][][] data) {
        var newItems = new HashSet<>();
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                for (var item : data[x][z]) {
                    newItems.add(item);
                    addNewItem(mode.getUniqueID(item), mode.getDescriptionId(item), mode.getItemIcon(item),
                            mode.getItemColor(item));
                }
            }
        }
        items.addAll(newItems);
    }

    private void addNewItem(String uniqueID, String renderingName, IGuiTexture icon, int color) {
        if (!selectedMap.containsKey(uniqueID)) {
            var index = itemList.widgets.size();
            var selectableWidgetGroup = new SelectableWidgetGroup(0, index * 15, itemList.getSize().width - 4, 15);
            var size = selectableWidgetGroup.getSize();
            selectableWidgetGroup.addWidget(new ImageWidget(0, 0, 15, 15, icon));
            selectableWidgetGroup.addWidget(new ImageWidget(15, 0, size.width - 15, 15,
                    new TextTexture(renderingName).setWidth(size.width - 15).setType(TextTexture.TextType.LEFT_HIDE)));
            selectableWidgetGroup.setOnSelected(s -> {
                if (isRemote()) {
                    texture.setSelected(uniqueID);
                }
            });
            selectableWidgetGroup.setSelectedTexture(ColorPattern.WHITE.borderTexture(-1));
            itemList.addWidget(selectableWidgetGroup);
            selectedMap.put(uniqueID, selectableWidgetGroup);
        }
    }

    @Override
    public void detectAndSendChanges() {
        var player = gui.entityPlayer;
        var world = player.level();
        if (gui.getTickCount() % scanTick == 0 && chunkIndex < (chunkRadius * 2 - 1) * (chunkRadius * 2 - 1)) {

            int row = chunkIndex / (chunkRadius * 2 - 1);
            int column = chunkIndex % (chunkRadius * 2 - 1);

            int ox = column - chunkRadius + 1;
            int oz = row - chunkRadius + 1;

            var chunk = world.getChunk(playerChunkX + ox, playerChunkZ + oz);
            if (mode == ProspectorMode.ORE) {
                ServerCache.instance.prospectAllInChunk(world.dimension(), chunk.getPos(), (ServerPlayer) player);
            }
            PacketProspecting packet = new PacketProspecting(playerChunkX + ox, playerChunkZ + oz, this.mode);
            mode.scan(packet.data, chunk);
            writeUpdateInfo(-1, packet::writePacketData);
            chunkIndex++;
        }
        var held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() instanceof IComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof ProspectorScannerBehavior prospector) {
                    if (!player.isCreative() && !prospector.drainEnergy(held, false)) {
                        player.closeContainer();
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == -1) {
            addPacketToQueue(PacketProspecting.readPacketData(mode, buffer));
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (packetQueue != null) {
            int max = 10;
            while (max-- > 0 && !packetQueue.isEmpty()) {
                var packet = packetQueue.poll();
                texture.updateTexture(packet);
                addOresToList(packet.data);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addPacketToQueue(PacketProspecting packet) {
        packetQueue.add(packet);
        if (mode == ProspectorMode.FLUID && packet.data[0][0].length > 0) {
            GTClientCache.instance.addFluid(gui.entityPlayer.level().dimension(), packet.chunkX, packet.chunkZ,
                    (ProspectorMode.FluidInfo) packet.data[0][0][0]);

        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        // draw background
        var x = position.x + 3;
        var y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;
        texture.draw(graphics, x, y);
        int cX = (mouseX - x) / 16;
        int cZ = (mouseY - y) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkRadius * 2 - 1 && cZ < chunkRadius * 2 - 1) {
            // draw hover layer
            DrawerHelper.drawSolidRect(graphics, cX * 16 + x, cZ * 16 + y, 16, 16, 0x4B6C6C6C);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        // draw tooltips
        var position = getPosition();
        var size = getSize();
        var x = position.x + 3;
        var y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;
        int cX = (mouseX - x) / 16;
        int cZ = (mouseY - y) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkRadius * 2 - 1 && cZ < chunkRadius * 2 - 1) {
            // draw hover layer
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable(mode.unlocalizedName));
            List<Object[]> items = new ArrayList<>();
            for (int i = 0; i < mode.cellSize; i++) {
                for (int j = 0; j < mode.cellSize; j++) {
                    assert texture != null;
                    if (texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j] != null) {
                        items.add(texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j]);
                    }
                }
            }
            mode.appendTooltips(items, tooltips, texture.getSelected());
            gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var clickedItem = getClickedVein(mouseX, mouseY);
        if (clickedItem == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!WaypointManager.isActive()) return true;
        MutableComponent veinName = Component.literal(clickedItem.name());
        veinName.setStyle(veinName.getStyle().withColor(clickedItem.color));
        WaypointManager.setWaypoint(new ChunkPos(clickedItem.position).toString(),
                clickedItem.name,
                clickedItem.color,
                gui.entityPlayer.level().dimension(),
                clickedItem.position.getX(), clickedItem.position.getY(), clickedItem.position.getZ());
        gui.entityPlayer.displayClientMessage(
                Component.translatable("behavior.prospector.added_waypoint", veinName), false);
        playButtonClickSound();
        return true;
    }

    private WaypointItem getClickedVein(double mouseX, double mouseY) {
        var position = getPosition();
        var size = getSize();
        var x = position.x + 3;
        var y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;

        int cX = (int) (mouseX - x) / 16;
        int cZ = (int) (mouseY - y) / 16;
        int offsetX = Math.abs((int) (mouseX - x) % 16);
        int offsetZ = Math.abs((int) (mouseY - y) % 16);
        int xDiff = cX - (chunkRadius - 1);
        int zDiff = cZ - (chunkRadius - 1);

        int xPos = ((gui.entityPlayer.chunkPosition().x + xDiff) << 4) + offsetX;
        int zPos = ((gui.entityPlayer.chunkPosition().z + zDiff) << 4) + offsetZ;

        var blockPos = new BlockPos(xPos, gui.entityPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, xPos, zPos),
                zPos);
        if (cX < 0 || cZ < 0 || cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1) {
            return null;
        }

        // If the ores are filtered use its name
        if (!texture.getSelected().equals(ProspectingTexture.SELECTED_ALL)) {
            for (var item : items) {
                if (!texture.getSelected().equals(mode.getUniqueID(item))) continue;
                var name = Component.translatable(mode.getDescriptionId(item)).getString();
                var color = mode.getItemColor(item);
                return new WaypointItem(blockPos, name, color);
            }
        }

        // If the cursor is over an ore use its name
        var hoveredItem = texture.data[cX * mode.cellSize + (offsetX * mode.cellSize / 16)][cZ * mode.cellSize +
                (offsetZ * mode.cellSize / 16)];
        if (hoveredItem != null && hoveredItem.length != 0) {
            var name = Component.translatable(mode.getDescriptionId(hoveredItem[0])).getString();
            var color = mode.getItemColor(hoveredItem[0]);
            return new WaypointItem(blockPos, name, color);
        }

        // If all else fails see if there's a nearby vein and use the vein's name
        var vein = GTClientCache.instance.getNearbyVeins(gui.entityPlayer.level().dimension(), blockPos, 32);
        if (!vein.isEmpty()) {
            vein.sort((o1, o2) -> (int) (o1.center().distToCenterSqr(xPos, o1.center().getY(), zPos) -
                    o2.center().distToCenterSqr(xPos, o2.center().getY(), zPos)));
            var name = OreRenderLayer.getName(vein.get(0)).getString();
            var materials = vein.get(0).definition().veinGenerator().getAllMaterials();
            var mostCommonItem = materials.get(materials.size() - 1);
            var color = mostCommonItem.getMaterialRGB();
            return new WaypointItem(blockPos, name, color);
        }

        return new WaypointItem(blockPos, "Depleted Vein", 0x990000);
    }

    @Override
    public String resultDisplay(Object value) {
        return mode.getDescriptionId(value);
    }

    @Override
    public void selectResult(Object item) {
        if (isRemote()) {
            var uid = mode.getUniqueID(item);
            texture.setSelected(uid);
            var selected = selectedMap.get(uid);
            if (selected != null) {
                itemList.setSelected(selected);
            }
        }
    }

    @Override
    public void search(String s, Consumer<Object> consumer) {
        var added = new HashSet<String>();
        for (var item : this.items) {
            if (Thread.currentThread().isInterrupted()) return;
            var id = mode.getUniqueID(item);
            if (!added.contains(id)) {
                added.add(id);
                var localized = LocalizationUtils.format(resultDisplay(item));
                if (item.toString().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)) ||
                        localized.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
                    consumer.accept(item);
                }
            }
        }
    }

    private record WaypointItem(BlockPos position, String name, int color) {}
}
