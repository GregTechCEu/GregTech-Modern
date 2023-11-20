package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        String researchId = AssemblyLineManager.readResearchId(stack);
        if (researchId == null) return;
        Collection<GTRecipe> recipes = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.getDataStickEntry(researchId);
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