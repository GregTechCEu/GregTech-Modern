package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.Lists;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget.drainFrom;

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
        if (GTCEu.Mods.isREILoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
            ingredient = new FluidStack(fluidStack.getFluid(), (int) fluidStack.getAmount(), fluidStack.getTag());
        }

        if (GTCEu.Mods.isEMILoaded() && ingredient instanceof EmiStack fluidEmiStack) {
            Fluid fluid = fluidEmiStack.getKeyOfType(Fluid.class);
            ingredient = fluid == null ? FluidStack.EMPTY :
                    new FluidStack(fluid, (int) fluidEmiStack.getAmount(), fluidEmiStack.getNbt());
        }

        if (GTCEu.Mods.isJEILoaded() && ingredient instanceof net.minecraftforge.fluids.FluidStack fluidStack) {
            ingredient = new FluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
        }
        return ingredient;
    }
}
