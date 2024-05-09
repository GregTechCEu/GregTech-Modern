package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.items.component.IAddInformation;
import com.gregtechceu.gtceu.api.items.component.IDataItem;
import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.GTRecipeType;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collection;
import java.util.List;

public class DataItemBehavior implements IAddInformation, IDataItem {

    private final boolean requireDataBank;

    public DataItemBehavior() {
        this.requireDataBank = false;
    }

    public DataItemBehavior(boolean requireDataBank) {
        this.requireDataBank = requireDataBank;
    }

    @Override
    public boolean requireDataBank() {
        return requireDataBank;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
        if (researchData == null) return;
        Collection<GTRecipe> recipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
        if (recipes != null && !recipes.isEmpty()) {
            tooltipComponents.add(Component.translatable("behavior.data_item.assemblyline.title"));
            Collection<ItemStack> added = new ObjectOpenHashSet<>();
            for (GTRecipe recipe : recipes) {
                ItemStack output = ItemRecipeCapability.CAP.of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                if (added.add(output)) {
                    tooltipComponents.add(Component.translatable("behavior.data_item.assemblyline.data", output.getDisplayName()));
                }
            }
        }
    }
}