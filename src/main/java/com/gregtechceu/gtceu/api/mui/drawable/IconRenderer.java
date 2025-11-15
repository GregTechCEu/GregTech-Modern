package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.StyledText;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextIcon;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class IconRenderer {

    public static final IconRenderer SHARED = new IconRenderer();

    protected float maxWidth = -1, maxHeight = -1;
    protected int x = 0, y = 0;
    protected Alignment alignment = Alignment.TopLeft;
    @Setter
    protected float scale = 1f;
    @Setter
    protected boolean shadow = false;
    @Setter
    protected int color = 0;
    @Setter
    protected int linePadding = 1;
    @Setter
    protected boolean simulate;
    @Getter
    protected float lastWidth = 0, lastHeight = 0;
    @Setter
    protected boolean useWholeWidth = false;

    public void setAlignment(Alignment alignment, float maxWidth) {
        setAlignment(alignment, maxWidth, -1);
    }

    public void setAlignment(Alignment alignment, float maxWidth, float maxHeight) {
        this.alignment = alignment;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(GuiContext context, IDrawable text) {
        draw(context, Collections.singletonList(text));
    }

    public void draw(GuiContext context, List<IDrawable> lines) {
        drawMeasuredLines(context, measureLines(lines));
    }

    public void drawMeasuredLines(GuiContext context, List<IIcon> lines) {
        TextRenderer.SHARED.setColor(this.color);
        TextRenderer.SHARED.setShadow(this.shadow);
        TextRenderer.SHARED.setScale(this.scale);
        TextRenderer.SHARED.setAlignment(this.alignment, this.maxWidth);
        int totalHeight = -1, maxWidth = 0;
        if (this.useWholeWidth) {
            maxWidth = (int) this.maxWidth;
        }
        for (IIcon icon : lines) {
            totalHeight += icon.getHeight() + this.linePadding;
            if (!this.useWholeWidth && icon.getWidth() > 0) {
                maxWidth = Math.max(maxWidth, icon.getWidth());
            }
        }
        if (!lines.isEmpty()) {
            // don't add padding to last line
            totalHeight -= this.linePadding;
        }
        int y = getStartY(totalHeight);
        for (IIcon icon : lines) {
            int x = icon.getWidth() > 0 ? getStartX(icon.getWidth()) : this.x;
            if (!this.simulate) {
                icon.draw(context, x, y, maxWidth, icon.getHeight(), WidgetTheme.getDefault().getTheme());
            }
            y += (int) ((icon.getHeight() + this.linePadding) * this.scale);
        }
        this.lastWidth = this.maxWidth > 0 ? Math.min(this.maxWidth, maxWidth) : maxWidth;
        this.lastHeight = totalHeight * this.scale;
    }

    public List<IIcon> measureLines(List<IDrawable> lines) {
        List<IIcon> icons = new ArrayList<>();
        for (IDrawable element : lines) {
            if (element instanceof IIcon icon) {
                icons.add(icon);
            } else if (element instanceof IKey key) {
                float scale = this.scale;
                Alignment alignment1 = this.alignment;
                if (element instanceof StyledText styledText) {
                    scale = styledText.scale();
                    alignment1 = styledText.alignment();
                }
                Component text = key.get();
                int width = (int) (getFont().width(text) * scale);
                icons.add(new TextIcon(text, width, (int) (getFont().lineHeight * scale), scale, alignment1));
            } else {
                icons.add(element.asIcon().height(getFont().lineHeight));
            }
        }
        return icons;
    }

    public List<FormattedCharSequence> wrapLine(Component line, float scale) {
        return this.maxWidth > 0 ? getFont().split(line, (int) (this.maxWidth / scale)) :
                Collections.singletonList(line.getVisualOrderText());
    }

    protected int getStartY(int totalHeight) {
        if (this.alignment.y > 0 && this.maxHeight > 0) {
            float height = totalHeight * this.scale;
            return (int) (this.y + (this.maxHeight * this.alignment.y) - height * this.alignment.y);
        }
        return this.y;
    }

    protected int getStartX(float lineWidth) {
        if (this.alignment.x > 0 && this.maxWidth > 0) {
            return (int) (this.x + (this.maxWidth * this.alignment.x) - lineWidth * this.alignment.x);
        }
        return this.x;
    }

    public float getFontHeight() {
        return getFont().lineHeight * this.scale;
    }

    @OnlyIn(Dist.CLIENT)
    public static Font getFont() {
        return Minecraft.getInstance().font;
    }
}
