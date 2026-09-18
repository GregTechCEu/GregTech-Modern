package brachy.modularui.integration.recipeviewer;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.jei.JeiRecipeViewerSlot;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.rei.ReiRecipeViewerSlot;
import brachy.modularui.widget.Widget;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class RecipeViewerSlotWidget<W extends RecipeViewerSlotWidget<W>> extends Widget<W> implements Interactable {

    public abstract W recipeSlotRole(RecipeSlotRole recipeSlotRole);

    public abstract <T> W value(EntryList<T> entryList);

    public W value(ItemStack stack) {
        return value(ItemStackList.of(stack));
    }

    public W value(FluidStack stack) {
        return value(FluidStackList.of(stack));
    }

    public abstract W chance(float chance);

    public static RecipeViewerSlotWidget<?> create() {
        if (!ModularUI.Mods.isRecipeViewerLoaded()) {
            throw new IllegalStateException("Cannot create recipe viewer slot without a recipe viewer mod loaded.");
        }

        if (ModularUI.Mods.EMI.isLoaded()) {
            return new EmiRecipeViewerSlot();
        } else if (ModularUI.Mods.REI.isLoaded()) {
            return new ReiRecipeViewerSlot();
        } else if (ModularUI.Mods.JEI.isLoaded()) {
            return new JeiRecipeViewerSlot();
        }
        throw new UnsupportedOperationException("Cannot create recipe viewer slot without EMI, REI, or JEI being loaded.");
    }
}
