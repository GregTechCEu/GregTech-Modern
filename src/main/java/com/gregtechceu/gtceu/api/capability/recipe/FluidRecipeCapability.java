package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidTagIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.OverlayingFluidStorage;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.utils.TagOrCycleFluidTransfer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.neoforged.neoforge.fluids.FluidStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
                    Collection<Holder<Fluid>> fluids = value.getFluids();
                    for (Holder<Fluid> fluid : fluids) {
                        ingredients.add(new MapFluidIngredient(new FluidStack(fluid, ingredient.getAmount(), ingredient.getComponents().asPatch())));
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
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        return contents.stream().map(content -> content.content)
            .map(this::of)
            .map(FluidRecipeCapability::mapFluid)
            .collect(Collectors.toList());
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        //noinspection unchecked
        return new TagOrCycleFluidTransfer((List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>>) contents);
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
                tank.setXEIChance(content.chance);
                tank.setOnAddedTooltips((w, tooltips) -> {
                    GTRecipeWidget.setConsumedChance(content, tooltips);
                    if (index >= recipe.getOutputContents(this).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        }
    }

    // Maps fluids to Either<(tag with count), FluidStack>s
    public static Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>> mapFluid(FluidIngredient ingredient) {
        int amount = ingredient.getAmount();
        PatchedDataComponentMap components = ingredient.getComponents();

        List<Pair<TagKey<Fluid>, Integer>> tags = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                tags.add(Pair.of(tagValue.getTag(), amount));
            } else {
                fluids.addAll(value.getFluids().stream().map(fluid -> new FluidStack(fluid, amount, components.asPatch())).toList());
            }
        }
        if (!tags.isEmpty()) {
            return Either.left(tags);
        }else {
            return Either.right(fluids);
        }
    }
}
