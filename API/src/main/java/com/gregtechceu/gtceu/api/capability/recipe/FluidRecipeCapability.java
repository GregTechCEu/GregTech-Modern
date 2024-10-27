package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidTagIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.api.transfer.fluid.TagOrCycleFluidHandler;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FluidKey;
import com.gregtechceu.gtceu.utils.GTHashMaps;
import com.gregtechceu.gtceu.utils.OverlayedFluidHandler;
import com.gregtechceu.gtceu.utils.OverlayingFluidStorage;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote FluidRecipeCapability
 */
public class FluidRecipeCapability extends RecipeCapability<FluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public FluidIngredient copyInner(FluidIngredient content) {
        return content.copy();
    }

    @Override
    public FluidIngredient copyWithModifier(FluidIngredient content, ContentModifier modifier) {
        if (content.isEmpty()) return content.copy();
        FluidIngredient copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()).intValue());
        return copy;
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof FluidIngredient ingredient) {
            for (FluidIngredient.Value value : ingredient.values) {
                if (value instanceof FluidIngredient.TagValue tagValue) {
                    ingredients.add(new MapFluidTagIngredient(tagValue.getTag()));
                } else {
                    Collection<Fluid> fluids = value.getFluids();
                    for (Fluid fluid : fluids) {
                        ingredients.add(new MapFluidIngredient(
                                new FluidStack(fluid, ingredient.getAmount(), ingredient.getNbt())));
                    }
                }
            }
        } else if (obj instanceof FluidStack stack) {
            ingredients.add(new MapFluidIngredient(stack));
            // noinspection deprecation
            stack.getFluid().builtInRegistryHolder().tags()
                    .forEach(tag -> ingredients.add(new MapFluidTagIngredient(tag)));
        }

        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof FluidIngredient fluid) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof FluidIngredient fluidIngredient) {
                        if (fluid.equals(fluidIngredient)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack fluidStack) {
                        if (fluid.test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluid);
            } else if (item instanceof FluidStack fluidStack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof FluidIngredient fluidIngredient) {
                        if (fluidIngredient.test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack stack) {
                        if (fluidStack.isFluidEqual(stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluidStack);
            }
        }
        return list;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        OverlayedFluidHandler overlayedFluidHandler = new OverlayedFluidHandler(new FluidHandlerList(
                Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.OUT, FluidRecipeCapability.CAP),
                        Collections::emptyList)
                        .stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .toList()));

        List<FluidStack> recipeOutputs = recipe.getOutputContents(FluidRecipeCapability.CAP)
                .stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .filter(ingredient -> !ingredient.isEmpty())
                .map(ingredient -> ingredient.getStacks()[0])
                .toList();

        while (minMultiplier != maxMultiplier) {
            overlayedFluidHandler.reset();

            int returnedAmount = 0;
            int amountToInsert = 0;

            for (FluidStack fluidStack : recipeOutputs) {
                if (fluidStack.getAmount() <= 0) continue;
                if (fluidStack.isEmpty()) continue;
                // Since multiplier starts at Int.MAX, check here for integer overflow
                if (multiplier > Integer.MAX_VALUE / fluidStack.getAmount()) {
                    amountToInsert = Integer.MAX_VALUE;
                } else {
                    amountToInsert = fluidStack.getAmount() * multiplier;
                }
                returnedAmount = amountToInsert - overlayedFluidHandler.insertFluid(fluidStack, amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = ParallelLogic.adjustMultiplier(returnedAmount == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    @Override
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        Map<FluidKey, Integer> fluidStacks = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .map(container -> container.getContents().stream().filter(FluidStack.class::isInstance)
                        .map(FluidStack.class::cast).toList())
                .flatMap(container -> GTHashMaps.fromFluidCollection(container).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum,
                        Object2IntLinkedOpenHashMap::new));

        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe input fluids to account for duplicated fluids,
        // so their sum is counted against the total of fluids available in the input
        Map<FluidIngredient, Integer> fluidCountMap = new HashMap<>();
        Map<FluidIngredient, Integer> notConsumableMap = new HashMap<>();
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            FluidIngredient fluidInput = FluidRecipeCapability.CAP.of(content.content);
            int fluidAmount = fluidInput.getAmount();
            if (content.chance == 0) {
                notConsumableMap.computeIfPresent(fluidInput,
                        (k, v) -> v + fluidAmount);
                notConsumableMap.putIfAbsent(fluidInput, fluidAmount);
            } else {
                fluidCountMap.computeIfPresent(fluidInput,
                        (k, v) -> v + fluidAmount);
                fluidCountMap.putIfAbsent(fluidInput, fluidAmount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable fluids from the fluid inventory map
        for (Map.Entry<FluidIngredient, Integer> notConsumableFluid : notConsumableMap.entrySet()) {
            int needed = notConsumableFluid.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : fluidStacks.entrySet()) {
                // Strip the Non-consumable tags here, as FluidKey compares the tags, which causes finding matching
                // fluids
                // in the input tanks to fail, because there is nothing in those hatches with a non-consumable tag
                if (notConsumableFluid.getKey().test(
                        new FluidStack(inputFluid.getKey().fluid, inputFluid.getValue(), inputFluid.getKey().tag))) {
                    available = inputFluid.getValue();
                    if (available > needed) {
                        inputFluid.setValue(available - needed);
                        needed -= available;
                        break;
                    } else {
                        inputFluid.setValue(0);
                        notConsumableFluid.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            // We need to check >= available here because of Non-Consumable inputs with stack size. If there is a NC
            // input
            // with size 1000, and only 500 in the input, needed will be equal to available, but this situation should
            // still fail
            // as not all inputs are present
            if (needed >= available) {
                return 0;
            }
        }

        // Return the maximum parallel limit here if there are only non-consumed inputs, which are all found in the
        // input bus
        // At this point, we would have already returned 0 if we were missing any non-consumable inputs, so we can omit
        // that check
        if (fluidCountMap.isEmpty() && !notConsumableMap.isEmpty()) {
            return parallelAmount;
        }

        // Iterate through the fluid inputs in the recipe
        for (Map.Entry<FluidIngredient, Integer> fs : fluidCountMap.entrySet()) {
            int needed = fs.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : fluidStacks.entrySet()) {
                if (fs.getKey().test(
                        new FluidStack(inputFluid.getKey().fluid, inputFluid.getValue(), inputFluid.getKey().tag))) {
                    available += inputFluid.getValue();
                }
            }
            if (available >= needed) {
                int ratio = (int) Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        return contents.stream().map(content -> content.content)
                .map(this::of)
                .map(FluidRecipeCapability::mapFluid)
                .collect(Collectors.toList());
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        // noinspection unchecked
        return new TagOrCycleFluidHandler(
                (List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>>) contents);
    }

    @NotNull
    @Override
    public Widget createWidget() {
        TankWidget tank = new TankWidget();
        tank.initTemplate();
        tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
        return tank;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return TankWidget.class;
    }

    @Override
    public void applyWidgetInfo(@NotNull Widget widget,
                                int index,
                                boolean isXEI,
                                IO io,
                                GTRecipeTypeUI.@UnknownNullability("null when storage == null") RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @UnknownNullability("null when content == null") GTRecipe recipe,
                                @Nullable Content content,
                                @Nullable Object storage) {
        if (widget instanceof TankWidget tank) {
            if (storage instanceof TagOrCycleFluidHandler fluidHandler) {
                tank.setFluidTank(fluidHandler, index);
            } else if (storage instanceof IFluidHandlerModifiable fluidHandler) {
                tank.setFluidTank(new OverlayingFluidStorage(fluidHandler, index));
            }
            tank.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
            tank.setAllowClickFilled(!isXEI);
            tank.setAllowClickDrained(!isXEI && io.support(IO.IN));
            if (content != null) {
                tank.setXEIChance((float) content.chance / content.maxChance);
                tank.setOnAddedTooltips((w, tooltips) -> {
                    FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content);
                    if (!isXEI && ingredient.getStacks().length > 0) {
                        FluidStack stack = ingredient.getStacks()[0];
                        TooltipsHandler.appendFluidTooltips(stack.getFluid(),
                                stack.getAmount(),
                                tooltips::add,
                                TooltipFlag.NORMAL);
                    }

                    GTRecipeWidget.setConsumedChance(content,
                            recipe.getChanceLogicForCapability(this, io, isTickSlot(index, io, recipe)), tooltips);
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        }
    }

    // Maps fluids to Either<(tag with count), FluidStack>s
    public static Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>> mapFluid(FluidIngredient ingredient) {
        int amount = ingredient.getAmount();
        CompoundTag tag = ingredient.getNbt();

        List<Pair<TagKey<Fluid>, Integer>> tags = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                tags.add(Pair.of(tagValue.getTag(), amount));
            } else {
                fluids.addAll(value.getFluids().stream().map(fluid -> new FluidStack(fluid, amount, tag)).toList());
            }
        }
        if (!tags.isEmpty()) {
            return Either.left(tags);
        } else {
            return Either.right(fluids);
        }
    }

    @Override
    public Object2IntMap<FluidIngredient> makeChanceCache() {
        return super.makeChanceCache();
    }
}
