package com.gregtechceu.gtceu.api.machine.trait.customlogic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
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
import net.neoforged.neoforge.common.util.ItemStackMap;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class SteamBoilerLogic implements GTRecipeType.ICustomRecipeLogic {

    private static final Set<SteamBoilerLogic> ALL_BOILER_LOGICS = new HashSet<>();
    private static final ResourceLocation EMPTY_MARKER_ID = GTCEu.id("invalid_recipe");
    private static final ItemStack EMPTY_MARKER_ITEM = Util.make(new ItemStack(Items.BARRIER), stack -> {
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Invalid Recipe! Contact developers for help!"));
    });

    private final Map<ItemStack, GTRecipe> itemRecipeCache = ItemStackMap.createTypeAndTagMap();
    private final Map<FluidStack, GTRecipe> fluidRecipeCache = new Object2ObjectOpenCustomHashMap<>(
            FluidStackLinkedSet.TYPE_AND_COMPONENTS);

    private final Supplier<GTRecipe> emptyMarker = GTMemoizer
            .memoize(() -> new GTRecipeBuilder(EMPTY_MARKER_ID, getRecipeType())
                    .inputItems(EMPTY_MARKER_ITEM.copy())
                    .build());

    public SteamBoilerLogic() {
        ALL_BOILER_LOGICS.add(this);
    }

    public static void clearBoilerRecipeCaches() {
        for (SteamBoilerLogic logic : ALL_BOILER_LOGICS) {
            logic.itemRecipeCache.clear();
        }
    }

    protected abstract GTRecipeType getRecipeType();

    protected abstract int modifyBurnTime(int originalBurnTime);

    protected @Nullable GTRecipe makeAnyRecipe(ItemStack input, int burnTime) {
        GTRecipe recipe;

        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(input);
        if (containedFluid.isEmpty()) {
            recipe = makeItemRecipe(input, burnTime);
        } else {
            recipe = makeFluidRecipe(containedFluid.get(), burnTime);
        }
        if (recipe == null) {
            return null;
        }

        recipe.setId(recipe.getId().withPrefix("/"));
        return recipe;
    }

    protected @Nullable GTRecipe makeItemRecipe(ItemStack input, int burnTime) {
        if (FluidUtil.getFluidContained(input).isPresent()) {
            return null;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(input.getItem());

        return getRecipeType().recipeBuilder(GTCEu.id(id.toDebugFileName()))
                .inputItems(input.copyWithCount(1))
                .duration(modifyBurnTime(burnTime))
                .build();
    }

    protected @Nullable GTRecipe makeFluidRecipe(FluidStack input, int itemBurnTime) {
        FluidStack fluid = input.copyWithAmount(250);
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());

        // the lava recipe's duration is 4/9 of the bucket's burn time
        // the creosote recipe's duration is 7/32 of the bucket's burn time
        // as such, the mean ratio is 191/576, or approximately 1/3.
        return getRecipeType().recipeBuilder(id.toDebugFileName())
                .inputFluids(fluid)
                .duration(modifyBurnTime(itemBurnTime / 3))
                .build();
    }

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        // items
        List<IRecipeHandler<?>> handlers = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        if (!handlers.isEmpty()) {
            // the machine has item inputs, try to get a valid recipe from them
            GTRecipe recipe = findItemRecipe(handlers);
            if (recipe != null) {
                return recipe;
            }
        }

        // fluids
        handlers = holder.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        if (!handlers.isEmpty()) {
            // the machine has fluid inputs, try to get a valid recipe from them
            GTRecipe recipe = findFluidRecipe(handlers);
            if (recipe != null) {
                return recipe;
            }
        }

        return null;
    }

    private @Nullable GTRecipe findItemRecipe(List<IRecipeHandler<?>> handlers) {
        IItemHandlerModifiable[] itemInputs = handlers.stream()
                .filter(IItemHandlerModifiable.class::isInstance)
                .map(IItemHandlerModifiable.class::cast)
                .toArray(IItemHandlerModifiable[]::new);
        IItemHandlerModifiable inputs = new CombinedInvWrapper(itemInputs);

        for (int i = 0; i < inputs.getSlots(); ++i) {
            ItemStack input = inputs.getStackInSlot(i);
            if (input.isEmpty()) {
                continue;
            }

            GTRecipe cached = itemRecipeCache.get(input);
            if (cached == emptyMarker.get()) {
                continue;
            } else if (cached != null) {
                return cached;
            }

            int burnTime = input.getBurnTime(null);
            if (burnTime <= 0) {
                itemRecipeCache.put(input, emptyMarker.get());
                continue;
            }
            GTRecipe recipe = makeItemRecipe(input, burnTime);
            if (recipe == null) {
                itemRecipeCache.put(input, emptyMarker.get());
                continue;
            }

            itemRecipeCache.put(input, recipe);
            return recipe;
        }

        return null;
    }

    private @Nullable GTRecipe findFluidRecipe(List<IRecipeHandler<?>> handlers) {
        IFluidHandler[] fluidInputs = handlers.stream()
                .filter(IFluidHandler.class::isInstance).map(IFluidHandler.class::cast)
                .toArray(IFluidHandler[]::new);
        FluidHandlerList inputs = new FluidHandlerList(fluidInputs);

        for (int i = 0; i < inputs.getTanks(); ++i) {
            FluidStack input = inputs.getFluidInTank(i);
            if (input.isEmpty()) {
                continue;
            }

            GTRecipe cached = fluidRecipeCache.get(input);
            if (cached == emptyMarker.get()) {
                continue;
            } else if (cached != null) {
                return cached;
            }

            ItemStack bucket = input.getFluid().getBucket().getDefaultInstance();
            bucket.applyComponents(input.getComponentsPatch());
            int burnTime = bucket.getBurnTime(null);
            if (burnTime <= 0) {
                fluidRecipeCache.put(input, emptyMarker.get());
                continue;
            }
            GTRecipe recipe = makeFluidRecipe(input, burnTime);
            if (recipe == null) {
                fluidRecipeCache.put(input, emptyMarker.get());
                continue;
            }

            fluidRecipeCache.put(input, recipe);
            return recipe;
        }

        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack input = item.getDefaultInstance();

            int burnTime = input.getBurnTime(null);
            if (burnTime <= 0) {
                continue;
            }
            GTRecipe recipe = makeAnyRecipe(input, burnTime);
            if (recipe != null) {
                getRecipeType().addToMainCategory(recipe);
            }
        }
    }
}
