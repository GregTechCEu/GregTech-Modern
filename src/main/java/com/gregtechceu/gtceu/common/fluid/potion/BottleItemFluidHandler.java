package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStackSimple;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class BottleItemFluidHandler extends FilteredFluidHandlerItemStackSimple {

    public BottleItemFluidHandler(@NotNull ItemStack container) {
        super(GTDataComponents.FLUID_CONTENT, container,
                PotionFluidHelper.BOTTLE_AMOUNT, s -> s.is(CustomTags.POTION_FLUIDS));
    }

    public BottleItemFluidHandler(ItemStack container, Void ignored) {
        this(container);
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        if (!fluid.isEmpty()) {
            container = new ItemStack(Items.POTION);
            container.set(DataComponents.POTION_CONTENTS,
                    fluid.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
        }
    }
}
