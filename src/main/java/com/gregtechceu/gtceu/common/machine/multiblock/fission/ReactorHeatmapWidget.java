package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.value.sync.ByteArraySyncValue;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ReactorHeatmapWidget extends Widget<ReactorHeatmapWidget> {

    private final ByteArraySyncValue dataSync;
    private List<HeatmapCell> cells = new ArrayList<>();
    private int gridMinX, gridMaxX, gridMinZ, gridMaxZ;
    private int gridWidth, gridHeight;

    private HeatmapCell hoveredCell;

    record HeatmapCell(int relX, int relZ, ReactorComponentType type,
                       float heatPct, boolean active, boolean depleted) {}

    public ReactorHeatmapWidget(ByteArraySyncValue dataSync) {
        this.dataSync = dataSync;
    }

    private void decodeData() {
        byte[] raw = dataSync.getValue();
        if (raw == null || raw.length < 4) {
            cells = new ArrayList<>();
            gridWidth = 1;
            gridHeight = 1;
            return;
        }

        int count = raw.length / 4;
        cells = new ArrayList<>(count);
        gridMinX = Integer.MAX_VALUE;
        gridMaxX = Integer.MIN_VALUE;
        gridMinZ = Integer.MAX_VALUE;
        gridMaxZ = Integer.MIN_VALUE;

        for (int i = 0; i < count; i++) {
            int offset = i * 4;
            int packed = ((raw[offset] & 0xFF) << 24) |
                    ((raw[offset + 1] & 0xFF) << 16) |
                    ((raw[offset + 2] & 0xFF) << 8) |
                    (raw[offset + 3] & 0xFF);

            int relX = ((packed >> 24) & 0xFF) - 128;
            int relZ = ((packed >> 16) & 0xFF) - 128;
            int typeOrd = (packed >> 12) & 0xF;
            float heatPct = ((packed >> 4) & 0xFF) / 255.0f;
            boolean active = (packed & 1) != 0;
            boolean depleted = (packed & 2) != 0;

            ReactorComponentType type = typeOrd < ReactorComponentType.values().length ?
                    ReactorComponentType.values()[typeOrd] : ReactorComponentType.FUEL_ROD;

            cells.add(new HeatmapCell(relX, relZ, type, heatPct, active, depleted));
            gridMinX = Math.min(gridMinX, relX);
            gridMaxX = Math.max(gridMaxX, relX);
            gridMinZ = Math.min(gridMinZ, relZ);
            gridMaxZ = Math.max(gridMaxZ, relZ);
        }

        gridWidth = cells.isEmpty() ? 1 : (gridMaxX - gridMinX + 1);
        gridHeight = cells.isEmpty() ? 1 : (gridMaxZ - gridMinZ + 1);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        decodeData();
        if (cells.isEmpty()) return;

        GuiGraphics graphics = context.getGraphics();
        int w = getArea().width;
        int h = getArea().height;

        int cellSize = Math.max(4, Math.min(w / gridWidth, h / gridHeight));
        int totalW = cellSize * gridWidth;
        int totalH = cellSize * gridHeight;
        int offsetX = (w - totalW) / 2;
        int offsetZ = (h - totalH) / 2;

        GuiDraw.drawRect(graphics, 0, 0, w, h, 0xFF101018);

        hoveredCell = null;
        float mouseX = context.getMouseX();
        float mouseY = context.getMouseY();

        for (HeatmapCell cell : cells) {
            int px = offsetX + (cell.relX - gridMinX) * cellSize;
            int pz = offsetZ + (cell.relZ - gridMinZ) * cellSize;
            int drawSize = cellSize - 1;

            int color = cellColor(cell);
            GuiDraw.drawRect(graphics, px, pz, drawSize, drawSize, color);

            if (cell.type == ReactorComponentType.CONTROLLER) {
                GuiDraw.drawRect(graphics, px, pz, drawSize, 1, 0xFFFFFFFF);
                GuiDraw.drawRect(graphics, px, pz + drawSize - 1, drawSize, 1, 0xFFFFFFFF);
                GuiDraw.drawRect(graphics, px, pz, 1, drawSize, 0xFFFFFFFF);
                GuiDraw.drawRect(graphics, px + drawSize - 1, pz, 1, drawSize, 0xFFFFFFFF);
            }

            if (mouseX >= px && mouseX < px + drawSize && mouseY >= pz && mouseY < pz + drawSize) {
                hoveredCell = cell;
            }
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (hoveredCell != null && isHovering()) {
            int absX = (int) context.getAbsMouseX();
            int absY = (int) context.getAbsMouseY();
            String typeName = hoveredCell.type.name().replace('_', ' ');

            List<Component> tooltip = new ArrayList<>();
            if (hoveredCell.type == ReactorComponentType.CASING ||
                    hoveredCell.type == ReactorComponentType.CONTROLLER ||
                    hoveredCell.type == ReactorComponentType.VESSEL) {
                tooltip.add(Component.literal(typeName));
            } else {
                String status = hoveredCell.depleted ? " (Depleted)" :
                        (!hoveredCell.active ? " (Inactive)" : "");
                tooltip.add(Component.literal(typeName + status));
                int heatVal = (int) (hoveredCell.heatPct * 100);
                tooltip.add(Component.literal("Heat: " + heatVal + "%")
                        .withStyle(heatVal > 75 ? net.minecraft.ChatFormatting.RED :
                                heatVal > 50 ? net.minecraft.ChatFormatting.YELLOW :
                                        net.minecraft.ChatFormatting.GREEN));
            }

            var font = Minecraft.getInstance().font;
            context.getGraphics().renderComponentTooltip(font, tooltip, absX, absY);
        }
    }

    private static int cellColor(HeatmapCell cell) {
        return switch (cell.type) {
            case CASING -> 0xFF3A3A5E;
            case VESSEL -> 0xFF505068;
            case CONTROLLER -> 0xFF4080DD;
            case CONTROL_ROD -> cell.active ? 0xFF8B1A1A : 0xFF555555;
            default -> {
                if (cell.depleted) yield 0xFF333333;
                int base = baseColor(cell.type);
                yield lerpToRed(base, cell.heatPct);
            }
        };
    }

    private static int baseColor(ReactorComponentType type) {
        return switch (type) {
            case FUEL_ROD -> 0xFFD4A017;
            case MODERATOR -> 0xFF33CC33;
            case NEUTRON_REFLECTOR -> 0xFFDDDDDD;
            case COOLANT_CHANNEL -> 0xFF00CCCC;
            case HEAT_EXCHANGER -> 0xFF55AAFF;
            default -> 0xFF5555FF;
        };
    }

    private static int lerpToRed(int base, float pct) {
        int bR = (base >> 16) & 0xFF, bG = (base >> 8) & 0xFF, bB = base & 0xFF;
        float t = Math.min(1.0f, pct);
        int r = (int) (bR + (0xCC - bR) * t);
        int g = (int) (bG * (1.0f - t));
        int b = (int) (bB * (1.0f - t));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
