package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Shadow(remap = false)
    private ItemStack tooltipStack;

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void gtceu$renderResearchItemContent(@Nullable LivingEntity entity, @Nullable Level level,
                                                 ItemStack stack, int x, int y, int seed, int z,
                                                 Operation<Void> original) {
        if (!RenderUtil.renderResearchItemContent((GuiGraphics) (Object) this, original,
                entity, level, stack, x, y, z, seed)) {
            original.call(entity, level, stack, x, y, seed, z);
        }
    }

    @WrapMethod(method = "renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V")
    private void gtceu$replaceWithRichTooltip(Font font, List<ClientTooltipComponent> components,
                                              int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner,
                                              Operation<Void> original) {
        if (!ConfigHolder.INSTANCE.client.ui.replaceVanillaTooltips || components.isEmpty()) {
            original.call(font, components, mouseX, mouseY, tooltipPositioner);
            return;
        }

        RichTooltip tooltip = new RichTooltip();
        tooltip.parent(area -> RichTooltip.findIngredientArea(area, mouseX, mouseY));
        // Other positions don't really work due to the lack of GuiContext in non-modular uis
        tooltip.add(components.get(0)).newLine();
        if (!this.tooltipStack.isEmpty()) {
            tooltip.spaceLine();
        }
        for (int i = 1, n = components.size(); i < n; i++) {
            tooltip.add(components.get(i)).newLine();
        }

        GuiContext context = GuiContext.getDefault();
        GuiGraphics lastGraphics = context.getGraphics();

        context.setGraphics((GuiGraphics) (Object) this);
        tooltip.draw(context, this.tooltipStack);

        context.setGraphics(lastGraphics);
    }
}
