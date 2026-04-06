package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.common.item.behavior.ProspectorScannerBehavior;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ProspectingMapWidget<T> extends WidgetGroup implements SearchComponentWidget.IWidgetSearch<T> {

    private final int chunkRadius;
    private final ProspectorMode<T> mode;
    private final int scanTick;
    @Getter
    private boolean darkMode = false;
    private final DraggableScrollableWidgetGroup itemList;

    @OnlyIn(Dist.CLIENT)
    private ProspectingTexture<T> texture;

    private int playerChunkX;
    private int playerChunkZ;
    // runtime
    private int chunkIndex = 0;

    private final Queue<PacketProspecting<T>> packetQueue = new LinkedBlockingQueue<>();
    private final Set<T> items = new CopyOnWriteArraySet<>();
    private final Map<String, SelectableWidgetGroup> selectedMap = new ConcurrentHashMap<>();

    public ProspectingMapWidget(int xPosition, int yPosition, int width, int height, int chunkRadius,
                                @NotNull ProspectorMode<T> mode, int scanTick) {
        super(xPosition, yPosition, width, height);
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;

        int imageWidth = (chunkRadius * 2 - 1) * 16;
        int imageHeight = (chunkRadius * 2 - 1) * 16;
        addWidget(new ImageWidget(0, (height - imageHeight) / 2 - 4, imageWidth + 8, imageHeight + 8,
                GuiTextures.BACKGROUND_INVERSE));

        WidgetGroup group = (WidgetGroup) new WidgetGroup(imageWidth + 10, 0, width - (imageWidth + 10), height)
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(this.itemList = new DraggableScrollableWidgetGroup(4, 28,
                group.getSize().width - 8, group.getSize().height - 32)
                .setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1)));
        group.addWidget(new SearchComponentWidget<>(6, 6, group.getSize().width - 12, 18, this));
        addWidget(group);
        addNewItem("[all]", "all resources", IGuiTexture.EMPTY, -1);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeVarInt(this.playerChunkX = gui.entityPlayer.chunkPosition().x);
        buffer.writeVarInt(this.playerChunkZ = gui.entityPlayer.chunkPosition().z);
        buffer.writeVarInt(gui.entityPlayer.getBlockX());
        buffer.writeVarInt(gui.entityPlayer.getBlockZ());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        this.texture = new ProspectingTexture<>(
                buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(),
                gui.entityPlayer.getVisualRotationYInDegrees(),
                mode, chunkRadius, darkMode);
    }

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode == darkMode) {
            return;
        }
        this.darkMode = darkMode;
        if (isRemote()) {
            this.texture.setDarkMode(this.darkMode);
        }
    }

    private void addOresToList(T[][][] data) {
        HashSet<T> newItems = new HashSet<>();
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                for (T item : data[x][z]) {
                    newItems.add(item);
                    addNewItem(mode.getUniqueId(item), mode.getDescription(item), mode.getItemIcon(item));
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
        Player player = gui.entityPlayer;
        Level level = player.level();

        int chunkDiameter = this.chunkRadius * 2 - 1;

        if (gui.getTickCount() % this.scanTick == 0 && this.chunkIndex < chunkDiameter * chunkDiameter) {
            int row = this.chunkIndex / chunkDiameter;
            int column = this.chunkIndex % chunkDiameter;

            int ox = column - this.chunkRadius + 1;
            int oz = row - this.chunkRadius + 1;

            LevelChunk chunk = level.getChunk(this.playerChunkX + ox, this.playerChunkZ + oz);
            if (mode == ProspectorMode.ORE) {
                ServerCache.instance.prospectAllInChunk(level.dimension(), chunk.getPos(), (ServerPlayer) player);
            }
            PacketProspecting<T> packet = new PacketProspecting<>(this.playerChunkX + ox, this.playerChunkZ + oz, mode);
            mode.scan(packet.data, chunk);
            writeUpdateInfo(-1, packet::writePacketData);

            this.chunkIndex++;
        }

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(held.getItem() instanceof IComponentItem componentItem)) {
            return;
        }
        for (var component : componentItem.getComponents()) {
            if (component instanceof ProspectorScannerBehavior prospector) {
                if (!player.isCreative() && !prospector.drainEnergy(held, false)) {
                    player.closeContainer();
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

        int maxToProcess = 10;
        while (maxToProcess-- > 0 && !packetQueue.isEmpty()) {
            PacketProspecting<T> packet = packetQueue.poll();
            texture.updateTexture(packet);
            addOresToList(packet.data);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addPacketToQueue(PacketProspecting<T> packet) {
        packetQueue.add(packet);
        if (mode == ProspectorMode.FLUID && packet.data[0][0].length > 0) {
            GTClientCache.instance.addFluid(
                    gui.entityPlayer.level().dimension(),
                    packet.chunkX, packet.chunkZ,
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
        int x = position.x + 3;
        int y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;
        this.texture.draw(graphics, x, y);

        int chunkDiameter = this.chunkRadius * 2 - 1;
        int cX = (mouseX - x) / 16;
        int cZ = (mouseY - y) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkDiameter && cZ < chunkDiameter) {
            // draw hover layer
            DrawerHelper.drawSolidRect(graphics, cX * 16 + x, cZ * 16 + y, 16, 16, 0x4B6C6C6C);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        var x = position.x + 3;
        var y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;

        int chunkDiameter = this.chunkRadius * 2 - 1;
        int chunkX = (mouseX - x) / 16;
        int chunkZ = (mouseY - y) / 16;
        if (chunkX < 0 || chunkZ < 0 || chunkX >= chunkDiameter || chunkZ >= chunkDiameter) {
            return;
        }

        List<Component> tooltips = new ArrayList<>();
        tooltips.add(Component.translatable(mode.unlocalizedName));
        List<T[]> items = new ArrayList<>();

        for (int i = 0; i < mode.cellSize; i++) {
            for (int j = 0; j < mode.cellSize; j++) {
                if (this.texture.data[chunkX * mode.cellSize + i][chunkZ * mode.cellSize + j] != null) {
                    items.add(this.texture.data[chunkX * mode.cellSize + i][chunkZ * mode.cellSize + j]);
                }
            }
        }

        // draw tooltips
        mode.appendTooltips(items, tooltips, this.texture.getSelected());
        gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        WaypointItem clickedItem = getClickedVein(mouseX, mouseY);
        if (clickedItem == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!WaypointManager.isActive()) return true;

        WaypointManager.setWaypoint(clickedItem.uniqueId,
                clickedItem.name.getString(), clickedItem.color,
                gui.entityPlayer.level().dimension(), clickedItem.position);
        gui.entityPlayer.displayClientMessage(
                Component.translatable("behavior.prospector.added_waypoint", veinName), false);
        playButtonClickSound();
        return true;
    }

    private WaypointItem getClickedVein(double mouseX, double mouseY) {
        var position = getPosition();
        var size = getSize();
        int x = position.x + 3;
        int y = position.y + (size.getHeight() - this.texture.getImageHeight()) / 2 - 1;

        int cX = (int) (mouseX - x) / 16;
        int cZ = (int) (mouseY - y) / 16;
        int offsetX = Math.abs((int) (mouseX - x) % 16);
        int offsetZ = Math.abs((int) (mouseY - y) % 16);
        int xDiff = cX - (this.chunkRadius - 1);
        int zDiff = cZ - (this.chunkRadius - 1);

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
        T[] hoveredItem = this.texture.data
                [cX * mode.cellSize + (offsetX * mode.cellSize / 16)]
                [cZ * mode.cellSize + (offsetZ * mode.cellSize / 16)];
        if (hoveredItem != null && hoveredItem.length != 0) {
            var name = Component.translatable(mode.getDescriptionId(hoveredItem[0])).getString();
            var color = mode.getItemColor(hoveredItem[0]);
            return new WaypointItem(blockPos, name, color);
        }

        // If all else fails see if there's a nearby vein and use the vein's name
        if (mode == ProspectorMode.ORE) {
            var veins = GTClientCache.instance.getNearbyVeins(gui.entityPlayer.level().dimension(), pos, 32);
            if (!veins.isEmpty()) {
                veins.sort((o1, o2) -> {
                    int o1Dist = (int) o1.center().distToCenterSqr(xPos, o1.center().getY(), zPos);
                    int o2Dist = (int) o2.center().distToCenterSqr(xPos, o2.center().getY(), zPos);
                    return o1Dist - o2Dist;
                });
                String uniqueId = OreRenderLayer.getId(veins.get(0));
                Component name = OreRenderLayer.getName(veins.get(0));
                List<Material> materials = veins.get(0).definition().veinGenerator().getAllMaterials();
                Material mostCommonItem = materials.get(materials.size() - 1);
                int color = mostCommonItem.getMaterialARGB();
                return new WaypointItem(pos, uniqueId, name, color);
            }
        }

        return new WaypointItem(blockPos, "Depleted Vein", 0x990000);
    }

    @Override
    public String resultDisplay(Object value) {
        return mode.getDescriptionId(value);
    }

    @Override
    public void selectResult(T item) {
        if (isRemote()) {
            String uniqueId = mode.getUniqueId(item);
            this.texture.setSelected(uniqueId);
            var selected = this.selectedMap.get(uniqueId);
            if (selected != null) {
                this.itemList.setSelected(selected);
            }
        }
    }

    @Override
    public void search(String searched, Consumer<T> consumer) {
        HashSet<String> added = new HashSet<>();
        for (T item : this.items) {
            if (Thread.currentThread().isInterrupted()) return;
            String id = mode.getUniqueId(item);
            if (!added.contains(id)) {
                added.add(id);
                String localized = resultDisplay(item);
                if (item.toString().toLowerCase(Locale.ROOT).contains(searched.toLowerCase(Locale.ROOT)) ||
                        localized.toLowerCase(Locale.ROOT).contains(searched.toLowerCase(Locale.ROOT))) {
                    consumer.accept(item);
                }
            }
        }
    }

    private record WaypointItem(BlockPos position, String name, int color) {}
}
