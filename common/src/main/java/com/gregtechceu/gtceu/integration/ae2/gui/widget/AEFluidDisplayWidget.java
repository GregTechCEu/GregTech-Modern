package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gregtechceu.gtceu.integration.ae2.util.AEConfigSlot.drawSelectionOverlay;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawText;

/**
 * @author GlodBlock
 * @ Display a certain {@link com.lowdragmc.lowdraglib.side.fluid.FluidStack} element.
 * @date 2023/4/19-0:30
 */
public class AEFluidDisplayWidget extends Widget {

    private final AEListGridWidget gridWidget;
    private final int index;

    public AEFluidDisplayWidget(int x, int y, AEListGridWidget gridWidget, int index) {
        super(new Position(x, y), new Size(18, 18));
        this.gridWidget = gridWidget;
        this.index = index;
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GenericStack fluid = this.gridWidget.getAt(this.index);
        GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        GuiTextures.NUMBER_BACKGROUND.draw(graphics, mouseX, mouseY, position.x + 18, position.y, 140, 18);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (fluid != null) {
            FluidStack fluidStack = fluid.what() instanceof AEFluidKey key ? FluidStack.create(key.getFluid(), fluid.amount(), key.getTag()) : FluidStack.empty();
            DrawerHelper.drawFluidForGui(graphics, fluidStack, fluid.amount(), stackX, stackY, 17, 17);
            String amountStr = String.format("x%,d", fluid.amount());
            drawText(graphics, amountStr, stackX + 20, stackY + 5, 1, 0xFFFFFFFF);
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        }
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            GenericStack fluid = this.gridWidget.getAt(this.index);
            FluidStack fluidStack = fluid.what() instanceof AEFluidKey key ? FluidStack.create(key.getFluid(), fluid.amount(), key.getTag()) : FluidStack.empty();
            if (fluid != null) {
                List<Component> hoverStringList = new ArrayList<>();
                hoverStringList.add(fluidStack.getDisplayName());
                hoverStringList.add(Component.literal(String.format("%,d L", fluid.amount())));
                TooltipsHandler.appendFluidTooltips(fluidStack.getFluid(), hoverStringList, TooltipFlag.NORMAL);
                graphics.renderTooltip(Minecraft.getInstance().font, hoverStringList, Optional.empty(), mouseX, mouseY);
            }
        }
    }

}