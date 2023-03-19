package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ComponentItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItemImpl extends ComponentItem implements FabricItem {
    protected ComponentItemImpl(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getRecipeRemainder(itemStack);
    }

    public static ComponentItem create(Properties properties) {
        return new ComponentItemImpl(properties);
    }

}
