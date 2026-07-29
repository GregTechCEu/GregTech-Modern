package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.ToolBoxBehavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public final class CustomToolIngredientHelper {

    private CustomToolIngredientHelper() {}

    public static boolean isCustomTool(ItemStack stack) {
        return stack.is(GTItems.TOOL_BOX.asItem()) || stack.getItem() instanceof ICustomToolIngredient;
    }

    public static boolean containsTool(ItemStack stack, GTToolType toolType) {
        if (stack.getItem() instanceof ICustomToolIngredient toolbox) {
            return toolbox.containsTool(stack, toolType);
        }
        if (stack.is(GTItems.TOOL_BOX.asItem())) {
            var inventory = ToolBoxBehavior.getInventory(stack);
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (ToolHelper.is(inventory.getStackInSlot(slot), toolType)) return true;
            }
        }
        return false;
    }

    public static boolean damageTool(ItemStack stack, GTToolType toolType, @Nullable LivingEntity user, int damage) {
        if (stack.getItem() instanceof ICustomToolIngredient toolbox) {
            return toolbox.damageTool(stack, toolType, user, damage);
        }
        if (stack.is(GTItems.TOOL_BOX.asItem())) {
            var inventory = ToolBoxBehavior.getInventory(stack);
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack tool = inventory.getStackInSlot(slot);
                if (!tool.isEmpty() && ToolHelper.is(tool, toolType)) {
                    ToolHelper.damageItem(tool, user, damage);
                    inventory.setStackInSlot(slot, tool);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean tryUseTool(Player player, GTToolType toolType) {
        ItemStack carried = player.containerMenu.getCarried();
        if (ToolHelper.is(carried, toolType)) {
            ToolHelper.damageItem(carried, player, 1);
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (ToolHelper.is(stack, toolType)) {
                ToolHelper.damageItem(stack, player, 1);
                return true;
            }
            if (isCustomTool(stack) && damageTool(stack, toolType, player, 1)) return true;
        }
        var curiosOptional = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);
        if (!curiosOptional.isPresent()) return false;
        var curios = curiosOptional.resolve().orElse(null);
        if (curios == null) return false;
        var handler = curios.getEquippedCurios();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (isCustomTool(stack) && damageTool(stack, toolType, player, 1)) return true;
        }
        return false;
    }

    public static void markLastUsedTool(ItemStack stack, GTToolType toolType) {
        if (stack.getItem() instanceof ICustomToolIngredient customToolIngredient) {
            customToolIngredient.markLastUsedTool(stack, toolType);
        } else if (stack.is(GTItems.TOOL_BOX.asItem())) {
            stack.getOrCreateTag().putString("last_used_tool", toolType.name);
        }
    };
}
