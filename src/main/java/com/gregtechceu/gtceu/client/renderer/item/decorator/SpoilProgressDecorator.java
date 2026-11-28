package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.spoilage.ISpoilableItem;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public class SpoilProgressDecorator implements IItemDecorator {

    public static final SpoilProgressDecorator INSTANCE = new SpoilProgressDecorator();

    private SpoilProgressDecorator() {}

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable == null) return false;

        int extraOffset = 0;
        if (stack.isDamaged()) {
            extraOffset += 2;
        }
        if ((stack.getItem() instanceof IGTTool tool && tool.isElectric()) ||
                stack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM) != null) {
            extraOffset += 2;
        }

        return renderSpoilProgressBar(guiGraphics, stack, spoilable, xOffset, yOffset, extraOffset);
    }

    public static boolean renderSpoilProgressBar(GuiGraphics graphics, ItemStack stack, ISpoilableItem spoilable,
                                                 int xOffset, int yOffset, int extraOffset) {
        if (spoilable.getCreationTick() == Long.MAX_VALUE) return false;

        int level = Math.clamp(spoilable.getTicksUntilSpoiled() * 13 / spoilable.getSpoilTicks(), 0, 13);
        int color = 0xFFFFFFFF;
        ToolChargeBarRenderer.render(graphics, level, xOffset, yOffset, extraOffset, true, color, color, false);
        return true;
    }
}
