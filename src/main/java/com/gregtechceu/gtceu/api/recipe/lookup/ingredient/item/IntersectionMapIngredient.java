package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.core.mixins.forge.IntersectionIngredientAccessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntersectionMapIngredient extends AbstractMapIngredient {

    @Getter
    protected List<AbstractMapIngredient> children;

    public IntersectionMapIngredient(List<AbstractMapIngredient> children) {
        this.children = children;
        this.children.sort(Comparator.comparingInt(AbstractMapIngredient::hashCode));
    }

    @NotNull
    public static List<AbstractMapIngredient> from(IntersectionIngredient ingredient) {
        List<Ingredient> originalChildren = ((IntersectionIngredientAccessor) ingredient).getChildren();
        List<AbstractMapIngredient> mapChildren = new ObjectArrayList<>();
        for (var ing : originalChildren) {
            mapChildren.addAll(MapIngredientTypeManager.getFrom(ing, ItemRecipeCapability.CAP));
        }

        return Collections.singletonList(new IntersectionMapIngredient(mapChildren));
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        MaterialEntry entry = ChemicalHelper.getMaterialEntry(stack.getItem());

        if (!entry.isEmpty() && TagPrefix.ORES.containsKey(entry.tagPrefix())) {
            List<AbstractMapIngredient> children = new ArrayList<>();
            children.add(new ItemTagMapIngredient(entry.tagPrefix().getItemTags(entry.material())[0]));
            children.add(new ItemTagMapIngredient(entry.tagPrefix().getItemParentTags()[0]));

            return Collections.singletonList(new IntersectionMapIngredient(children));
        }
        return Collections.emptyList();
    }

    @Override
    protected int hash() {
        int hash = 31;
        for (var child : children) {
            hash *= 31 * child.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            IntersectionMapIngredient other = (IntersectionMapIngredient) o;
            if (this.children != null) {
                if (other.children != null) {
                    if (this.children.size() != other.children.size()) return false;
                    for (int i = 0; i < this.children.size(); ++i) {
                        var ingredient1 = this.children.get(i);
                        var ingredient2 = other.children.get(i);
                        if (!ingredient1.equals(ingredient2)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (o instanceof ItemStackMapIngredient stackIngredient) {
            for (var child : this.children) {
                if (!child.equals(stackIngredient)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isSpecialIngredient() {
        for (var child : this.children) {
            if (child.isSpecialIngredient()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "IntersectionMapIngredient{" + "children=" + children + "}";
    }
}
