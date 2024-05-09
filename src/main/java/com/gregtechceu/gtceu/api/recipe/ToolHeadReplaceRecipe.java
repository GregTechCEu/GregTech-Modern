package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.item.GTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolHeadReplaceRecipe extends CustomRecipe {
    public static SimpleCraftingRecipeSerializer<ToolHeadReplaceRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(ToolHeadReplaceRecipe::new);

    private static final Map<TagPrefix, GTToolType[]> TOOL_HEAD_TO_TOOL_MAP = new HashMap<>();

    public static void setToolHeadForTool(TagPrefix toolHead, GTToolType tool) {
        if (!(tool.electricTier > -1)) return;
        TOOL_HEAD_TO_TOOL_MAP.computeIfAbsent(toolHead, p -> new GTToolType[GTValues.MAX])[tool.electricTier] = tool;
    }

    public ToolHeadReplaceRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level level) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
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
            UnificationEntry toolHead;
            if (stack1.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack1.getItem();
                toolHead = ChemicalHelper.getUnificationEntry(stack2.getItem());
            } else if (stack2.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack2.getItem();
                toolHead = ChemicalHelper.getUnificationEntry(stack1.getItem());
            } else return false;

            if (!tool.isElectric()) return false;
            if (toolHead == null || toolHead == UnificationEntry.EmptyMapMarkerEntry) return false;
            GTToolType[] output = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.tagPrefix);
            return output != null && output[tool.getElectricTier()] != null;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv, @NotNull HolderLookup.Provider provider) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack itemstack = inv.getItem(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);
            }
        }

        if (list.size() == 2) {
            ItemStack first = list.get(0), second = list.get(1);

            IGTTool tool;
            UnificationEntry toolHead;
            ItemStack realTool;
            if (first.getItem() instanceof IGTTool) {
                tool = (IGTTool) first.getItem();
                toolHead = ChemicalHelper.getUnificationEntry(second.getItem());
                realTool = first;
            } else if (second.getItem() instanceof IGTTool) {
                tool = (IGTTool) second.getItem();
                toolHead = ChemicalHelper.getUnificationEntry(first.getItem());
                realTool = second;
            } else return ItemStack.EMPTY;
            if (!tool.isElectric()) return ItemStack.EMPTY;
            SimpleEnergyContent powerUnit = realTool.get(GTDataComponents.ENERGY_CONTENT);
            if (toolHead == null || toolHead == UnificationEntry.EmptyMapMarkerEntry) return ItemStack.EMPTY;
            GTToolType[] toolArray = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.tagPrefix);
            ItemStack newTool = GTItems.TOOL_ITEMS.get(toolHead.material, toolArray[tool.getElectricTier()])
                .get().get(powerUnit.charge(), powerUnit.maxCharge());
            if (newTool == null) return ItemStack.EMPTY;

            return newTool;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container) {
        var result = super.getRemainingItems(container);
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
        return SERIALIZER;
    }
}
