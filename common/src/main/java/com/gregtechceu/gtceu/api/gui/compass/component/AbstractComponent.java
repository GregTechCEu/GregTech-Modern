package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.utils.XmlUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.w3c.dom.Element;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote AbstractComponent
 */
public abstract class AbstractComponent implements ILayoutComponent {
    protected int topMargin = 0;
    protected int bottomMargin = 0;
    protected String hoverInfo;

    @Override
    public ILayoutComponent fromXml(Element element) {
        this.topMargin = XmlUtils.getAsInt(element, "top-margin", topMargin);
        this.bottomMargin = XmlUtils.getAsInt(element, "bottom-margin", bottomMargin);
        this.hoverInfo = XmlUtils.getAsString(element, "hover-info", null);
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public final LayoutPageWidget createWidgets(LayoutPageWidget currentPage) {
        if (topMargin > 0) {
            currentPage = currentPage.addOffsetSpace(topMargin);
        }
        currentPage = addWidgets(currentPage);
        if (bottomMargin > 0) {
            currentPage = currentPage.addOffsetSpace(bottomMargin);
        }
        return currentPage;
    }

    @Environment(EnvType.CLIENT)
    protected abstract LayoutPageWidget addWidgets(LayoutPageWidget currentPage);
}
