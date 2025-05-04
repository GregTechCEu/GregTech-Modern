package com.gregtechceu.gtceu.api.machine.trait.customlogic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.ItemStackMap;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class SteamBoilerLogic implements GTRecipeType.ICustomRecipeLogic {

    private static final Set<SteamBoilerLogic> ALL_BOILER_LOGICS = new HashSet<>();
    private static final ResourceLocation EMPTY_MARKER_ID = GTCEu.id("invalid_recipe");
    private static final ItemStack EMPTY_MARKER_ITEM = Util.make(new ItemStack(Items.BARRIER), stack -> {
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Invalid Recipe! Contact developers for help!"));
    });

    private final Map<ItemStack, GTRecipe> recipeCache = ItemStackMap.createTypeAndTagMap();
    private final Supplier<GTRecipe> emptyMarker = GTMemoizer
            .memoize(() -> new GTRecipeBuilder(EMPTY_MARKER_ID, getRecipeType())
                    .inputItems(EMPTY_MARKER_ITEM.copy())
                    .build());

    public SteamBoilerLogic() {
        ALL_BOILER_LOGICS.add(this);
    }

    public static void clearBoilerRecipeCaches() {
        for (SteamBoilerLogic logic : ALL_BOILER_LOGICS) {
            logic.recipeCache.clear();
        }
    }

    protected abstract GTRecipeType getRecipeType();

    protected abstract int modifyBurnTime(int originalBurnTime);

    private GTRecipe makeRecipe(ItemStack input, int burnTime) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(input.getItem());
        return getRecipeType().recipeBuilder(GTCEu.id(itemId.toDebugFileName()))
                .inputItems(input.copyWithCount(1))
                .duration(modifyBurnTime(burnTime))
                .build();
    }

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var itemInputs = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
                .filter(IItemHandlerModifiable.class::isInstance).map(IItemHandlerModifiable.class::cast)
                .toArray(IItemHandlerModifiable[]::new);
        var inputs = new CombinedInvWrapper(itemInputs);
        for (int i = 0; i < inputs.getSlots(); ++i) {
            ItemStack input = inputs.getStackInSlot(i);
            GTRecipe cached = recipeCache.get(input);
            if (cached == emptyMarker.get()) {
                continue;
            } else if (cached != null) {
                return cached;
            }

            if (input.isEmpty() || FluidUtil.getFluidContained(input).isPresent()) {
                recipeCache.put(input, emptyMarker.get());
                continue;
            }
            int burnTime = input.getBurnTime(RecipeType.SMELTING);
            if (burnTime <= 0) {
                recipeCache.put(input, emptyMarker.get());
                continue;
            }
            GTRecipe recipe = makeRecipe(input, burnTime);
            recipeCache.put(input, recipe);
            return recipe;
        }
        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack input = item.getDefaultInstance();
            if (input.isEmpty() || FluidUtil.getFluidContained(input).isPresent()) {
                continue;
            }
            int burnTime = input.getBurnTime(RecipeType.SMELTING);
            if (burnTime <= 0) {
                continue;
            }
            GTRecipe recipe = makeRecipe(input, burnTime);
            getRecipeType().addToMainCategory(recipe);
        }
    }
}
