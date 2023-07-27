package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote HeaderComponent
 */
@NoArgsConstructor
public class HeaderComponent extends AbstractComponent {
    public enum Header {
        h1(16, 3, true, 200),
        h2(13, 2, true, 140),
        h3(10, 1, false, 80);

        public final int fontSize;
        public final int space;
        public final boolean isCenter;
        public final int width;

        Header(int fontSize, int space, boolean isCenter, int width) {
            this.fontSize = fontSize;
            this.space = space;
            this.isCenter = isCenter;
            this.width = width;
        }
    }

    protected Header header = Header.h1;
    protected String text = "";
    protected int space = 1;
    protected int fontSize = 9;
    protected int fontColor = 0xFFFFFFFF;
    protected boolean isCenter = false;
    protected boolean isShadow = true;

    @Override
    public ILayoutComponent fromXml(Element element) {
        header = Header.valueOf(element.getTagName());
        this.isCenter = header.isCenter;
        this.fontSize = header.fontSize;
        this.space = header.space;
        this.bottomMargin = 3;
        text = XmlUtils.getContent(element, true);
        space = XmlUtils.getAsInt(element, "space", space);
        fontSize = XmlUtils.getAsInt(element, "font-size", fontSize);
        fontColor = XmlUtils.getAsColor(element, "font-color", fontColor);
        if (element.hasAttribute("isCenter")) {
            isCenter = XmlUtils.getAsBoolean(element, "isCenter", true);
        }
        if (element.hasAttribute("isShadow")) {
            isShadow = XmlUtils.getAsBoolean(element, "isShadow", true);
        }
        return super.fromXml(element);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        var pageWidth = currentPage.getPageWidth();
        // text
        List<String> textLines = new ArrayList<>();
        Font font = Minecraft.getInstance().font;
        List<String> content = Arrays.stream(I18n.get(text).split("\n")).toList();
        for (String textLine : content) {
            if (textLine.isEmpty()) {
                textLines.add(" ");
            } else {
                textLines.addAll(font.getSplitter()
                        .splitLines(textLine, pageWidth, Style.EMPTY)
                        .stream().map(FormattedText::getString).toList());
            }
        }

        currentPage = currentPage.addStreamWidget(new TextBoxWidget(0, 0, pageWidth, textLines)
                .setShadow(isShadow)
                .setCenter(isCenter)
                .setFontColor(fontColor)
                .setFontSize(fontSize)
                .setSpace(space));

        // tail
        WidgetGroup group = new WidgetGroup(0, 0, pageWidth, 3);
        group.addWidget(new ImageWidget(isCenter ? (pageWidth - header.width) / 2 : 0, 0, header.width, 2, ColorPattern.WHITE.rectTexture()));
        return currentPage.addStreamWidget(group);
    }

}
