package com.gregtechceu.gtceu.api.recipe.kind;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.item.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.GTRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolHeadReplaceRecipe extends CustomRecipe {

    private static final Map<TagPrefix, GTToolType[]> TOOL_HEAD_TO_TOOL_MAP = new HashMap<>();

    public static void setToolHeadForTool(TagPrefix toolHead, GTToolType tool) {
        if (!(tool.electricTier > -1)) return;
        TOOL_HEAD_TO_TOOL_MAP.computeIfAbsent(toolHead, p -> new GTToolType[GTValues.MAX])[tool.electricTier] = tool;
    }

    public ToolHeadReplaceRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, @NotNull Level level) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                list.add(stack);
                if (list.size() > 2) {
                    return false;
                }
            }
        }

        if (list.size() == 2) {
            ItemStack stack1 = list.get(0);
            ItemStack stack2 = list.get(1);

            IGTTool tool;
            MaterialEntry toolHead;
            if (stack1.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack1.getItem();
                toolHead = ChemicalHelper.getMaterialEntry(stack2.getItem());
            } else if (stack2.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack2.getItem();
                toolHead = ChemicalHelper.getMaterialEntry(stack1.getItem());
            } else return false;

            if (!tool.isElectric()) return false;
            if (toolHead.isEmpty()) return false;
            GTToolType[] output = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.tagPrefix());
            return output != null && output[tool.getElectricTier()] != null;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput inv, @NotNull HolderLookup.Provider registries) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack itemstack = inv.getItem(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);
            }
        }

        if (list.size() == 2) {
            ItemStack first = list.get(0), second = list.get(1);

            IGTTool tool;
            MaterialEntry toolHead;
            ItemStack realTool;
            if (first.getItem() instanceof IGTTool) {
                tool = (IGTTool) first.getItem();
                toolHead = ChemicalHelper.getMaterialEntry(second.getItem());
                realTool = first;
            } else if (second.getItem() instanceof IGTTool) {
                tool = (IGTTool) second.getItem();
                toolHead = ChemicalHelper.getMaterialEntry(first.getItem());
                realTool = second;
            } else return ItemStack.EMPTY;
            if (!tool.isElectric()) return ItemStack.EMPTY;
            IElectricItem powerUnit = GTCapabilityHelper.getElectricItem(realTool);
            if (toolHead.isEmpty() || powerUnit == null) return ItemStack.EMPTY;
            GTToolType[] toolArray = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.tagPrefix());
            ItemStack newTool = GTMaterialItems.TOOL_ITEMS.get(toolHead.material(), toolArray[tool.getElectricTier()])
                    .get().get(powerUnit.getCharge(), powerUnit.getMaxCharge());
            if (newTool.isEmpty()) return ItemStack.EMPTY;

            return newTool;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        var result = super.getRemainingItems(input);
        for (ItemStack stack : result) {
            if (stack.getItem() instanceof IGTTool) {
                stack.setCount(0);
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializers.CRAFTING_TOOL_HEAD_REPLACE.get();
    }
}
