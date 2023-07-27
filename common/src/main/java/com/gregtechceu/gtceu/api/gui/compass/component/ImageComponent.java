package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.w3c.dom.Element;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote TextureComponent
 */
@NoArgsConstructor
public class ImageComponent extends TextBoxComponent {

    protected int width = 50;
    protected int height = 50;
    protected float u0 = 0, v0 = 0;
    protected float u1 = 1, v1 = 1;
    protected IGuiTexture guiTexture = new ColorBorderTexture(-1, -1);

    @Override
    public ILayoutComponent fromXml(Element element) {
        this.width = XmlUtils.getAsInt(element, "width", width);
        this.height = XmlUtils.getAsInt(element, "height", height);
        this.u0 = XmlUtils.getAsFloat(element, "u0", u0);
        this.v0 = XmlUtils.getAsFloat(element, "v0", v0);
        this.u1 = XmlUtils.getAsFloat(element, "u1", u1);
        this.v1 = XmlUtils.getAsFloat(element, "v1", v1);
        String type = XmlUtils.getAsString(element, "type", "resource");
        String url = XmlUtils.getAsString(element, "url", "");
        if (type.equals("resource")) {
            if (ResourceLocation.isValidResourceLocation(url)) {
                guiTexture = new ResourceTexture(url).getSubTexture(u0, v0, u1, v1);
            }
        }
        isCenter = true;
        return super.fromXml(element);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        var imageWidget = new ImageWidget(0, 0, width, height, guiTexture);
        if (this.hoverInfo != null) {
            imageWidget.setHoverTooltips(hoverInfo);
        }
        currentPage.addStreamWidget(imageWidget);
        currentPage.addOffsetSpace(3);
        return super.addWidgets(currentPage);
    }
}
