package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author KilaBash
 * @date 2022/9/3
 * @implNote TextBoxComponent
 */
@NoArgsConstructor
public class TextBoxComponent extends AbstractComponent {
    protected List<MutableComponent> components;
    protected int space = 2;
    protected boolean isCenter;

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        components = XmlUtils.getComponents(element, Style.EMPTY);
        space = XmlUtils.getAsInt(element, "space", space);
        if (element.hasAttribute("isCenter")) {
            isCenter = XmlUtils.getAsBoolean(element, "isCenter", true);
        }
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        var panel = new ComponentPanelWidget(currentPage.getPageWidth());
        return currentPage.addStreamWidget(panel);
    }

    private class ComponentPanelWidget extends Widget {
        protected final int maxWidthLimit;
        @Setter
        protected BiConsumer<String, ClickData> clickHandler;
        private List<FormattedCharSequence> cacheLines = Collections.emptyList();

        public ComponentPanelWidget(int maxWidthLimit) {
            super(0, 0, 0, 0);
            this.maxWidthLimit = maxWidthLimit;
            this.formatDisplayText();
            this.updateComponentTextSize();
        }

        public static Component withButton(Component textComponent, String componentData) {
            var style = textComponent.getStyle();
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
            style = style.withColor(ChatFormatting.YELLOW);
            return textComponent.copy().withStyle(style);
        }

        public static Component withButton(Component textComponent, String componentData, int color) {
            var style = textComponent.getStyle();
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
            style = style.withColor(color);
            return textComponent.copy().withStyle(style);
        }

        public static Component withHoverTextTranslate(Component textComponent, Component hover) {
            Style style = textComponent.getStyle();
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            return textComponent.copy().withStyle(style);
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == 1) {
                ClickData clickData = ClickData.readFromBuf(buffer);
                String componentData = buffer.readUtf();
                if (clickHandler != null) {
                    clickHandler.accept(componentData, clickData);
                }
            } else {
                super.handleClientAction(id, buffer);
            }
        }

        @Environment(EnvType.CLIENT)
        private void updateComponentTextSize() {
            var fontRenderer = Minecraft.getInstance().font;
            int totalHeight = cacheLines.size() * (fontRenderer.lineHeight + space);
            if (totalHeight > 0) {
                totalHeight -= space;
                totalHeight += 2;
            }
            setSize(new Size(maxWidthLimit, totalHeight));
        }

        @Environment(EnvType.CLIENT)
        private void formatDisplayText() {
            var fontRenderer = Minecraft.getInstance().font;
            int maxTextWidthResult = maxWidthLimit == 0 ? Integer.MAX_VALUE : maxWidthLimit;
            this.cacheLines = components.stream().flatMap(component ->
                            ComponentRenderUtils.wrapComponents(component, maxTextWidthResult, fontRenderer).stream())
                    .toList();
        }

        @Environment(EnvType.CLIENT)
        @Nullable
        protected Style getStyleUnderMouse(double mouseX, double mouseY) {
            var fontRenderer = Minecraft.getInstance().font;
            var position = getPosition();
            var size = getSize();
            var selectedLine = (mouseY - position.y) / (fontRenderer.lineHeight + space);
            if (selectedLine >= 0 && selectedLine < cacheLines.size()) {
                var cacheLine = cacheLines.get((int) selectedLine);
                var lineWidth = fontRenderer.width(cacheLine);
                var offsetX = position.x + (size.width - lineWidth) / 2f;
                if (mouseX >= offsetX) {
                    var mouseOffset = (int)(mouseX - position.x);
                    return fontRenderer.getSplitter().componentStyleAtWidth(cacheLine, mouseOffset);
                }
            }
            return null;
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            var style = getStyleUnderMouse(mouseX, mouseY);
            if (style != null) {
                if (style.getClickEvent() != null) {
                    ClickEvent clickEvent = style.getClickEvent();
                    String componentText = clickEvent.getValue();
                    if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL && componentText.startsWith("@!")) {
                        String rawText = componentText.substring(2);
                        ClickData clickData = new ClickData();
                        if (clickHandler != null) {
                            clickHandler.accept(rawText, clickData);
                        }
                        writeClientAction(1, buf -> {
                            clickData.writeToBuf(buf);
                            buf.writeUtf(rawText);
                        });
                        playButtonClickSound();
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void drawInForeground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            var style = getStyleUnderMouse(mouseX, mouseY);
            if (style != null) {
                if (style.getHoverEvent() != null) {
                    var hoverEvent = style.getHoverEvent();
                    var hoverTips = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (hoverTips != null) {
                        gui.getModularUIGui().setHoverTooltip(List.of(hoverTips), ItemStack.EMPTY, null, null);
                        return;
                    }
                }
            }
            super.drawInForeground(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void drawInBackground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(poseStack, mouseX, mouseY, partialTicks);
            var fontRenderer = Minecraft.getInstance().font;
            var position = getPosition();
            var size = getSize();
            for (int i = 0; i < cacheLines.size(); i++) {
                var cacheLine = cacheLines.get(i);
                if (isCenter) {
                    var lineWidth = fontRenderer.width(cacheLine);
                    fontRenderer.draw(poseStack, cacheLine, position.x + (size.width - lineWidth) / 2f, position.y + i * (fontRenderer.lineHeight + space), -1);
                } else {
                    fontRenderer.draw(poseStack, cacheLine, position.x, position.y + i * (fontRenderer.lineHeight + space), -1);
                }
            }
        }
    }

}
