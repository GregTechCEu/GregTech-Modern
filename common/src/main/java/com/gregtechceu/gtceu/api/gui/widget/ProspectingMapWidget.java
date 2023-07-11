package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.gui.texture.ProspectingTexture;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.ProspectorScannerBehavior;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ProspectingMapWidget extends WidgetGroup implements SearchComponentWidget.IWidgetSearch<String> {
    private final int chunkRadius;
    private final ProspectorMode mode;
    private final int scanTick;
    @Getter
    private boolean darkMode = false;
    private final DraggableScrollableWidgetGroup itemList;
    @Environment(EnvType.CLIENT)
    private ProspectingTexture texture;

    //runtime
    private int chunkIndex = 0;
    private final Queue<PacketProspecting> packetQueue = new LinkedBlockingQueue<>();
    private final Set<String> items = new CopyOnWriteArraySet<>();
    private final Map<String, SelectableWidgetGroup> selectedMap = new HashMap<>();

    public ProspectingMapWidget(int xPosition, int yPosition, int width, int height, int chunkRadius, @Nonnull ProspectorMode mode, int scanTick) {
        super(xPosition, yPosition, width, height);
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;
        int imageWidth = (chunkRadius * 2 - 1) * 16;
        int imageHeight = (chunkRadius * 2 - 1) * 16;
        addWidget(new ImageWidget(0, (height - imageHeight) / 2 - 4, imageWidth + 8, imageHeight + 8, GuiTextures.BACKGROUND_INVERSE));
        var group = (WidgetGroup) new WidgetGroup(imageWidth + 10, 0, width - (imageWidth + 10), height).setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(itemList = new DraggableScrollableWidgetGroup(4, 28, group.getSize().width - 8, group.getSize().height - 32)
                .setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1)));
        group.addWidget(new SearchComponentWidget<>(6, 6, group.getSize().width - 12, 18, this));
        addWidget(group);
        if (isRemote()) {
            texture = new ProspectingTexture(mode, chunkRadius, darkMode);
            addNewItem("[all]", "all resources", IGuiTexture.EMPTY);
        }
    }

    public void setDarkMode(boolean mode) {
        if (darkMode != mode) {
            darkMode = mode;
            if (isRemote()) {
                texture.setDarkMode(darkMode);
            }
        }
    }

    private void addOresToList(String[][][] data) {
        var newNames = new HashSet<String>();
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                for (var name : data[x][z]) {
                    if (!items.contains(name) && !newNames.contains(name)) {
                        newNames.add(name);
                        addNewItem(name, mode.getDescriptionId(name), mode.getItemIcon(name));
                    }
                }
            }
        }
        items.addAll(newNames);
    }

    private void addNewItem(String name, String renderingName, IGuiTexture icon) {
        var index = itemList.widgets.size();
        var selectableWidgetGroup = new SelectableWidgetGroup(0, index * 15, itemList.getSize().width - 4, 15);
        var size = selectableWidgetGroup.getSize();
        selectableWidgetGroup.addWidget(new ImageWidget(0, 0, 15, 15, icon));
        selectableWidgetGroup.addWidget(new ImageWidget(15, 0, size.width - 15, 15, new TextTexture(renderingName).setWidth(size.width - 15).setType(TextTexture.TextType.LEFT_HIDE)));
        selectableWidgetGroup.setOnSelected(s -> {
            if (isRemote()) {
                texture.setSelected(name);
            }
        });
        selectableWidgetGroup.setSelectedTexture(ColorPattern.WHITE.borderTexture(-1));
        itemList.addWidget(selectableWidgetGroup);
        selectedMap.put(name, selectableWidgetGroup);
    }

    @Override
    public void detectAndSendChanges() {
        var player = gui.entityPlayer;
        var world = player.level;
        if (gui.getTickCount() % scanTick == 0 && chunkIndex < (chunkRadius * 2 - 1) * (chunkRadius * 2 - 1)) {

            int playerChunkX = player.chunkPosition().x;
            int playerChunkZ = player.chunkPosition().z;

            int row = chunkIndex / (chunkRadius * 2 - 1);
            int column = chunkIndex % (chunkRadius * 2 - 1);

            int ox = column - chunkRadius + 1;
            int oz = row - chunkRadius + 1;

            var chunk = world.getChunk(playerChunkX + ox, playerChunkZ + oz);
            PacketProspecting packet = new PacketProspecting(playerChunkX + ox, playerChunkZ + oz, this.mode);
            mode.scan(packet.data, chunk);
            writeUpdateInfo(-1, packet::writePacketData);
            chunkIndex++;
        }
        var held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() instanceof ComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof ProspectorScannerBehavior prospector) {
                    if (!player.isCreative() && !prospector.drainEnergy(held, false)) {
                        player.closeContainer();
                    }
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == -1) {
            addPacketToQueue(PacketProspecting.readPacketData(mode, buffer));
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (packetQueue != null) {
            int max = 10;
            while (max-- > 0 && !packetQueue.isEmpty()) {
                var packet = packetQueue.poll();
                texture.updateTexture(
                        gui.entityPlayer.chunkPosition().x,
                        gui.entityPlayer.chunkPosition().z,
                        gui.entityPlayer.getBlockX(),
                        gui.entityPlayer.getBlockZ(), packet);
                addOresToList(packet.data);
            }
        }
    }


    @Environment(EnvType.CLIENT)
    private void addPacketToQueue(PacketProspecting packet) {
        packetQueue.add(packet);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        //draw background
        var x = position.x + 3;
        var y = position.y + (size.getHeight() - texture.getImageHeight()) / 2 - 1;
        texture.draw(poseStack, x, y);
        int cX = (mouseX - x) / 16;
        int cZ = (mouseY - y) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkRadius * 2 - 1 && cZ < chunkRadius * 2 - 1) {
            // draw hover layer
            DrawerHelper.drawSolidRect(poseStack, cX * 16 + x, cZ * 16 + y, 16, 16,0x4B6C6C6C);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void drawInForeground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(poseStack, mouseX, mouseY, partialTicks);
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
            final var itemHover = collectItemInfo(cX, cZ);
            itemHover.forEach((name, count) -> {
                tooltips.add(Component.translatable(mode.getDescriptionId(name)).append(" --- " + count));
            });
            gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
        }
    }

    @NotNull
    private HashMap<String, Integer> collectItemInfo(int cX, int cZ) {
        HashMap<String, Integer> itemHover = new HashMap<>();
        for (int i = 0; i < mode.cellSize; i++) {
            for (int j = 0; j < mode.cellSize; j++) {
                assert texture != null;
                if (texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j] != null) {
                    var items = texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j];
                    for (String item : items) {
                        if (ProspectingTexture.SELECTED_ALL.equals(texture.getSelected()) || texture.getSelected().equals(item)) {
                            itemHover.put(item, itemHover.getOrDefault(item, 0) + 1);
                        }
                    }
                }
            }
        }
        return itemHover;
    }

    @Override
    public String resultDisplay(String value) {
        return mode.getDescriptionId(value);
    }

    @Override
    public void selectResult(String value) {
        if (isRemote()) {
            texture.setSelected(value);
            var selected = selectedMap.get(value);
            if (selected != null) {
                itemList.setSelected(selected);
            }
        }
    }

    @Override
    public void search(String s, Consumer<String> consumer) {
        for (String item : this.items) {
            var localized = LocalizationUtils.format(resultDisplay(item));
            if (item.toLowerCase().contains(s.toLowerCase()) || localized.contains(s)) {
                consumer.accept(item);
            }
        }
    }
}
