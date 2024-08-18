package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FluidKey;
import com.gregtechceu.gtceu.utils.GTHashMaps;
import com.gregtechceu.gtceu.utils.OverlayedFluidHandler;
import com.gregtechceu.gtceu.utils.OverlayingFluidStorage;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.utils.TagOrCycleFluidTransfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.*;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
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
public class FluidRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(@NotNull SizedFluidIngredient content) {
        return new SizedFluidIngredient(content.ingredient(), content.amount());
    }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) {
        return new SizedFluidIngredient(content.ingredient(), modifier.apply(content.amount()).intValue());
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof SizedFluidIngredient ingredient) {
            switch (ingredient.ingredient()) {
                case TagFluidIngredient tag -> ingredients.add(new MapFluidTagIngredient(tag.tag()));
                case SingleFluidIngredient single -> ingredients
                        .add(new MapFluidStackIngredient(single.getStacks()[0]));
                case DataComponentFluidIngredient component when component.isStrict() -> ingredients
                        .addAll(MapFluidStackDataComponentIngredient.from(ingredient.ingredient()));
                case DataComponentFluidIngredient component when !component.isStrict() -> ingredients
                        .addAll(MapFluidStackWeakDataComponentIngredient.from(ingredient.ingredient()));
                case IntersectionFluidIngredient intersection -> ingredients
                        .add(new MapFluidIntersectionIngredient(intersection));
                case CompoundFluidIngredient compound -> {
                    for (FluidIngredient inner : compound.children()) {
                        ingredients.addAll(convertToMapIngredient(inner));
                    }
                }
                default -> {
                    for (FluidStack fluid : ingredient.getFluids()) {
                        ingredients.add(new MapFluidStackIngredient(fluid));
                    }
                }
            }
        } else if (obj instanceof FluidStack stack) {
            ingredients.add(new MapFluidStackIngredient(stack));
            stack.getFluidHolder().tags()
                    .forEach(tag -> ingredients.add(new MapFluidTagIngredient(tag)));
        }

        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedFluidIngredient fluid) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SizedFluidIngredient fluidIngredient) {
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
                    if (obj instanceof SizedFluidIngredient fluidIngredient) {
                        if (fluidIngredient.test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack stack) {
                        if (FluidStack.isSameFluidSameComponents(fluidStack, stack)) {
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

        OverlayedFluidHandler overlayedFluidHandler = new OverlayedFluidHandler(new FluidTransferList(
                Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.OUT, FluidRecipeCapability.CAP),
                        Collections::emptyList)
                        .stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .toList()));

        while (minMultiplier != maxMultiplier) {
            overlayedFluidHandler.reset();

            int amountLeft = 0;

            for (FluidStack fluidStack : recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                    .map(FluidRecipeCapability.CAP::of).filter(ingredient -> !ingredient.ingredient().hasNoFluids())
                    .map(ingredient -> ingredient.getFluids()[0]).toList()) {
                if (fluidStack.getAmount() <= 0) continue;
                // Since multiplier starts at Int.MAX, check here for integer overflow
                if (multiplier > Integer.MAX_VALUE / fluidStack.getAmount()) {
                    amountLeft = Integer.MAX_VALUE;
                } else {
                    amountLeft = fluidStack.getAmount() * multiplier;
                }
                int inserted = overlayedFluidHandler.insertFluid(fluidStack, amountLeft);
                if (inserted > 0) {
                    amountLeft -= inserted;
                }
                if (amountLeft > 0) {
                    break;
                }
            }

            int[] bin = ParallelLogic.adjustMultiplier(amountLeft == 0, minMultiplier, multiplier, maxMultiplier);
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
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum,
                        Object2LongLinkedOpenHashMap::new));

        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe input fluids to account for duplicated fluids,
        // so their sum is counted against the total of fluids available in the input
        Map<SizedFluidIngredient, Integer> fluidCountMap = new HashMap<>();
        Map<SizedFluidIngredient, Integer> notConsumableMap = new HashMap<>();
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            SizedFluidIngredient fluidInput = FluidRecipeCapability.CAP.of(content.content);
            final int fluidAmount = fluidInput.amount();
            if (content.chance == 0.0f) {
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
        for (Map.Entry<SizedFluidIngredient, Integer> notConsumableFluid : notConsumableMap.entrySet()) {
            int needed = notConsumableFluid.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : fluidStacks.entrySet()) {
                // Strip the Non-consumable tags here, as FluidKey compares the tags, which causes finding matching
                // fluids
                // in the input tanks to fail, because there is nothing in those hatches with a non-consumable tag
                FluidStack stack = new FluidStack(inputFluid.getKey().fluid, inputFluid.getValue(), inputFluid.getKey().component);
                if (notConsumableFluid.getKey().equals(stack)) {
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
        for (Map.Entry<SizedFluidIngredient, Integer> fs : fluidCountMap.entrySet()) {
            long needed = fs.getValue();
            long available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : fluidStacks.entrySet()) {
                FluidStack stack = new FluidStack(inputFluid.getKey().fluid, inputFluid.getValue(), inputFluid.getKey().component);
                if (fs.getKey().test(stack)) {
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
        return new TagOrCycleFluidTransfer(
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
            if (storage instanceof TagOrCycleFluidTransfer fluidTransfer) {
                tank.setFluidTank(fluidTransfer, index);
            } else if (storage instanceof IFluidHandlerModifiable fluidTransfer) {
                tank.setFluidTank(new OverlayingFluidStorage(fluidTransfer, index));
            }
            tank.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
            tank.setAllowClickFilled(!isXEI);
            tank.setAllowClickDrained(!isXEI);
            if (content != null) {
                tank.setXEIChance((float) content.chance / content.maxChance);
                tank.setOnAddedTooltips((w, tooltips) -> {
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
    public static Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>> mapFluid(SizedFluidIngredient ingredient) {
        final int amount = ingredient.amount();
        if (ingredient.ingredient() instanceof IntersectionFluidIngredient intersection) {
            // Map intersection ingredients to the items inside, as recipe viewers don't support them.
            List<FluidIngredient> children = intersection.children();
            if (children.isEmpty()) {
                return Either.right(null);
            }
            var childEither = mapFluid(new SizedFluidIngredient(children.getFirst(), amount));
            return Either.right(childEither.map(tags -> {
                List<FluidStack> tagItems = tags.stream()
                        .map(pair -> Pair.of(BuiltInRegistries.FLUID.getTag(pair.getFirst()).stream(),
                                pair.getSecond()))
                        .flatMap(pair -> pair.getFirst().flatMap(
                                tag -> tag.stream().map(holder -> new FluidStack(holder.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
                ListIterator<FluidStack> iterator = tagItems.listIterator();
                iteratorLoop:
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            continue iteratorLoop;
                        }
                    }
                    iterator.set(item.copyWithAmount(amount));
                }
                return tagItems;
            }, items -> {
                items = new ArrayList<>(items);
                ListIterator<FluidStack> iterator = items.listIterator();
                iteratorLoop:
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            continue iteratorLoop;
                        }
                    }
                    iterator.set(item.copyWithAmount(amount));
                }
                return items;
            }));
        } else if (ingredient.ingredient() instanceof TagFluidIngredient tag) {
            return Either.left(List.of(Pair.of(tag.tag(), amount)));
        }
        return Either.right(Arrays.stream(ingredient.getFluids()).toList());
    }
}
