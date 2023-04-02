package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ComponentItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItemImpl extends ComponentItem {
    protected int burnTime = -1;

    protected ComponentItemImpl(Properties properties) {
        super(properties);
    }

    public static ComponentItem create(Item.Properties properties) {
        return new ComponentItemImpl(properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getCraftingRemainingItem(itemStack);
    }

    public <T> LazyOptional<T> getCapability(@Nonnull final ItemStack itemStack, @Nonnull final Capability<T> cap) {
        for (IItemComponent component : components) {
            if (component instanceof IComponentCapability componentCapability) {
                var value = componentCapability.getCapability(itemStack, cap);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }

    public void burnTime(int burnTime) {
        this.burnTime = burnTime;
    }
}
