package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Lists;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface IGhostItemTarget extends IGhostIngredientTarget {

    @OnlyIn(Dist.CLIENT)
    Rect2i getRectangleBox();

    @OnlyIn(Dist.CLIENT)
    void acceptItem(ItemStack itemStack);

    @OnlyIn(Dist.CLIENT)
    @Override
    default List<Target> getPhantomTargets(Object ingredient) {
        if (!(convertIngredient(ingredient) instanceof ItemStack)) {
            return Collections.emptyList();
        } else {
            final Rect2i rectangle = getRectangleBox();
            return Lists.newArrayList(new Target[] { new Target() {

                @NotNull
                public Rect2i getArea() {
                    return rectangle;
                }

                public void accept(@NotNull Object ingredient) {
                    ingredient = convertIngredient(ingredient);

                    if (ingredient instanceof ItemStack stack) {
                        acceptItem(stack);
                    }
                }
            } });
        }
    }

    default Object convertIngredient(Object ingredient) {
        if (GTCEu.Mods.isEMILoaded() && ingredient instanceof EmiStack itemEmiStack) {
            Item item = itemEmiStack.getKeyOfType(Item.class);
            ItemStack itemStack = item == null ? ItemStack.EMPTY : new ItemStack(item, (int) itemEmiStack.getAmount());
            if (!itemStack.isEmpty()) {
                itemStack.setTag(itemEmiStack.getNbt());
            }
            ingredient = itemStack;
        }

        if (GTCEu.Mods.isJEILoaded() && ingredient instanceof ITypedIngredient<?> itemJeiStack) {
            return itemJeiStack.getItemStack().orElse(ItemStack.EMPTY);
        }
        return ingredient;
    }
}
