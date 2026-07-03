package com.gregtechceu.gtceu.integration.xei.widgets;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

public class VoltageBorderWidget extends Widget {

    private final int color;

    public VoltageBorderWidget(int x, int y, int width, int height, int color) {
        super(x, y, width, height);
        this.color = color;
        this.setClientSideWidget();
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();

        int outerThickness = 1;
        int innerThickness = 1;
        int glowThickness = 1;
        int margin = 4;

        int x = pos.x - margin;
        int y = pos.y - margin;
        int width = size.width + 2 * margin;
        int height = size.height + 2 * margin;

        // 颜色
        int voltageColor = this.color;
        int outerColor = darken(voltageColor, 0.6f);
        int innerColor = withAlpha(voltageColor, 0xE0);   // 更亮一点
        int glowColor = withAlpha(voltageColor, 0x40);    // 发光更柔和

        // === 外层边框（暗色，JEI风格） ===
        graphics.fill(x, y, x + width, y + outerThickness, outerColor); // top
        graphics.fill(x, y + height - outerThickness, x + width, y + height, outerColor); // bottom
        graphics.fill(x, y, x + outerThickness, y + height, outerColor); // left
        graphics.fill(x + width - outerThickness, y, x + width, y + height, outerColor); // right

        // === 内层边框（亮色，靠近内容） ===
        graphics.fill(x + 1, y + 1, x + width - 1, y + 1 + innerThickness, innerColor); // top
        graphics.fill(x + 1, y + height - 1 - innerThickness, x + width - 1, y + height - 1, innerColor); // bottom
        graphics.fill(x + 1, y + 1 + innerThickness, x + 1 + innerThickness, y + height - 1 - innerThickness,
                innerColor); // left
        graphics.fill(x + width - 1 - innerThickness, y + 1 + innerThickness, x + width - 1,
                y + height - 1 - innerThickness, innerColor); // right

        // === Glow 层（只上下）===
        graphics.fill(x + 4, y + 3, x + width - 4, y + 3 + glowThickness, glowColor); // top glow
        graphics.fill(x + 4, y + height - 3 - glowThickness, x + width - 4, y + height - 3, glowColor); // bottom glow
    }

    private int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private int darken(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // 永远不拦截点击事件
    }
}
