package com.gregtechceu.gtceu.api.gui.misc;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Lists;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget.drainFrom;

public interface IGhostFluidTarget extends IGhostIngredientTarget {

    @OnlyIn(Dist.CLIENT)
    Rect2i getRectangleBox();

    @OnlyIn(Dist.CLIENT)
    void acceptFluid(FluidStack fluidStack);

    @OnlyIn(Dist.CLIENT)
    @Override
    default List<Target> getPhantomTargets(Object ingredient) {
        ingredient = convertIngredient(ingredient);
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
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

                    FluidStack ingredientStack;
                    if (ingredient instanceof FluidStack fluidStack) {
                        ingredientStack = fluidStack;
                    } else {
                        ingredientStack = drainFrom(ingredient);
                    }

                    if (ingredientStack != null) {
                        acceptFluid(ingredientStack);
                    }
                }
            } });
        }
    }

    default Object convertIngredient(Object ingredient) {
        if (LDLib.isReiLoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
            ingredient = FluidStack.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
        }

        if (LDLib.isEmiLoaded() && ingredient instanceof EmiStack fluidEmiStack) {
            Fluid fluid = fluidEmiStack.getKeyOfType(Fluid.class);
            ingredient = fluid == null ? FluidStack.empty() :
                    FluidStack.create(fluid, fluidEmiStack.getAmount(), fluidEmiStack.getNbt());
        }
        return ingredient;
    }
}
