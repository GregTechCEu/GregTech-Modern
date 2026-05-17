package com.gregtechceu.gtceu.integration.ae2.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.ITheme;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import java.util.List;

/**
 * 18x18 display widget for a single AE2 GenericStack. Reads from a shared mutable list
 * by index so that amount updates don't require widget rebuilds.
 */
public class AEStackDisplayWidget extends Widget<AEStackDisplayWidget> {

    private final List<GenericStack> source;
    private final int index;

    public AEStackDisplayWidget(List<GenericStack> source, int index) {
        this.source = source;
        this.index = index;
        size(18);
    }

    private GenericStack getStack() {
        return index < source.size() ? source.get(index) : null;
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        var graphics = context.getGraphics();
        GuiTextures.SLOT_DARK.draw(graphics, 0, 0, 0, 0, 18, 18);

        GenericStack stack = getStack();
        if (stack == null) return;

        if (stack.what() instanceof AEItemKey itemKey) {
            graphics.renderItem(itemKey.toStack(1), 1, 1);
        } else if (stack.what() instanceof AEFluidKey) {
            AEGuiHelper.drawFluid(graphics, stack, 1, 1);
        }
        AEGuiHelper.drawAmountOverlay(graphics, stack.amount(), 1, 1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (isHovering()) {
            GenericStack stack = getStack();
            if (stack == null) return;

            AEGuiHelper.drawSelectionOverlay(context.getGraphics(), 1, 1, 16, 16);
            context.getGraphics().renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(stack.what().getDisplayName(),
                            Component.literal("x" + AEGuiHelper.formatAmountFull(stack.amount()))
                                    .withStyle(ChatFormatting.GRAY)),
                    (int) context.getAbsMouseX(), (int) context.getAbsMouseY());
        }
    }
}
