package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidTransferHandler;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import net.minecraft.world.item.crafting.Ingredient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/10/23
 * @implNote SlotComponent
 */
public class IngredientComponent extends AbstractComponent {
    List<Object> ingredients = new ArrayList<>();

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element e) {
                if (e.getNodeName().equals("item")) {
                    var ingredient = XmlUtils.getIngredient(e);
                    ingredients.add(ingredient);
                }
                if (e.getNodeName().equals("fluid")) {
                    var fluidStack = XmlUtils.getFluidStack(e);
                    ingredients.add(fluidStack);
                }
            }
        }
        return this;
    }

    @Override
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        if (ingredients.isEmpty()) return currentPage;
        WidgetGroup group = new WidgetGroup(0, 0, ingredients.size() * 20, 20);
        int x = 1;
        for (Object ingredient : ingredients) {
            if (ingredient instanceof Ingredient item) {
                CycleItemStackHandler itemStackHandler = new CycleItemStackHandler(List.of(Arrays.stream(item.getItems()).toList()));
                group.addWidget(new SlotWidget(itemStackHandler, 0, x, 1, false, false)
                        .setBackground(GuiTextures.SLOT));
            } else if (ingredient instanceof FluidStack fluidStack) {
                group.addWidget(new TankWidget(new FluidStorage(fluidStack), x, 1, false, false)
                        .setBackground(GuiTextures.FLUID_SLOT));
            }
            x += 20;
        }
        return currentPage.addStreamWidget(group);
    }
}
