package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.RangedFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GradientUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class FluidRecipeCapability extends RecipeCapability<FluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, FluidIngredient.CODEC);
    }

    @Override
    public FluidIngredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return FluidIngredient.fromNetwork(friendlyByteBuf);
    }

    @Override
    public void toNetwork(FluidIngredient ingredient, FriendlyByteBuf friendlyByteBuf) {
        ingredient.toNetwork(friendlyByteBuf);
    }

    @Override
    public FluidIngredient copyInner(FluidIngredient content, int multiplier) {
        return content.copyWithMultiplier(multiplier);
    }

    @Override
    public boolean isChanced(FluidIngredient content) {
        return content.isChanced();
    }

    @Override
    public IGuiTexture createXEIOverlay(FluidIngredient content, boolean perTick) {
        return new IGuiTexture() {

            @Override
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(graphics, x, y, width, height, content.getChance());
                if (content instanceof RangedFluidIngredient ranged) {
                    drawString(graphics, x, y, width, height,
                            "%s-%s".formatted(ranged.getMinAmount(), ranged.getAmount()), 0xFFFFFF, false);
                } else if (content.getAmount() > 0) {
                    drawString(graphics, x, y, width, height, FormattingUtil.formatBuckets(content.getAmount()),
                            0xFFFFFF, false);
                }
                if (perTick) {
                    drawString(graphics, x, y, width, height,
                            LocalizationUtils.format("gtceu.gui.content.tips.per_tick_short"), 0xFFFF00, true);
                }
            }
        };
    }

    private static void drawChance(GuiGraphics graphics, float x, float y, int width, int height, int chance) {
        if (chance == IChancedIngredient.MAX_CHANCE) return;
        float chanceFloat = 1f * chance / IChancedIngredient.MAX_CHANCE;
        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_nc_short") :
                FormattingUtil.formatNumber2Places(100 * chanceFloat) + "%";
        int color = chance == 0 ? 0xFF0000 : GradientUtil.toRGB(Mth.lerp(chanceFloat, 29f, 167f), 100f, 50f);
        drawString(graphics, x, y, width, height, s, color, true);
    }

    private static void drawString(GuiGraphics graphics, float x, float y, int width, int height, String s, int color,
                                   boolean top) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - (top ? height : 0)), color, true);
        graphics.pose().popPose();
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public List<AbstractMapIngredient> getMapIngredients(FluidIngredient content) {
        return content.getMapIngredients();
    }

    @Override
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int multiplier, boolean tick) {
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        var handlers = holder.getOutputHandlerMap().get(this);
        if (handlers == null || handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        int maxAmount = 0;
        List<FluidIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            maxAmount = Math.max(maxAmount, content.getAmount());
            ingredients.add(content);
        }
        if (maxAmount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxAmount;
        }

        while (minMultiplier != maxMultiplier) {
            List<FluidIngredient> copied = new ArrayList<>();
            for (final var ing : ingredients) {
                copied.add(copyWithMultiplier(ing, multiplier));
            }

            for (var handler : handlers) {
                if (handler.handleRecipe(IO.OUT, recipe, copied, true)) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(copied.isEmpty(), minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }

        return multiplier;
    }

    @Override
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return limit;

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        Object2LongMap<FluidStack> inventory = getInputContents(holder);
        if (inventory.isEmpty()) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        var nonConsumables = new Object2LongOpenHashMap<FluidIngredient>();
        var consumables = new Object2LongOpenHashMap<FluidIngredient>();
        for (FluidIngredient ing : inputs) {
            int amount = ing.getAmount();

            if (ing.getChance() == 0) {
                nonConsumables.addTo(ing, amount);
            } else {
                consumables.addTo(ing, amount);
            }
        }

        // is this even possible
        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;

        // Check for enough NC in inventory
        for (var ncEntry : Object2LongMaps.fastIterable(nonConsumables)) {
            FluidIngredient ingredient = ncEntry.getKey();
            long needed = ncEntry.getLongValue();
            for (var stackEntry : Object2LongMaps.fastIterable(inventory)) {
                if (ingredient.test(stackEntry.getKey())) {
                    long count = stackEntry.getLongValue();
                    long lesser = Math.min(needed, count);
                    count -= lesser;
                    needed -= lesser;
                    stackEntry.setValue(count);
                    if (needed == 0) break;
                }
            }
            if (needed > 0) return 0;
        }
        // Satisfied NC + no consumables -> early return
        if (consumables.isEmpty()) return limit;

        int maxMultiplier = Integer.MAX_VALUE;
        // Loop over all consumables
        for (var cEntry : Object2LongMaps.fastIterable(consumables)) {
            FluidIngredient ingredient = cEntry.getKey();
            final long needed = cEntry.getLongValue();
            final long maxNeeded = needed * limit;
            long available = 0;
            // Search stacks in our inventory, summing them up
            for (var stackEntry : Object2LongMaps.fastIterable(inventory)) {
                if (ingredient.test(stackEntry.getKey())) {
                    available += stackEntry.getLongValue();
                    // We can stop if we already have enough for max parallel
                    if (available >= maxNeeded) break;
                }
            }
            // ratio will equal 0 if available < needed
            int ratio = GTMath.saturatedCast(Math.min(limit, available / needed));
            maxMultiplier = Math.min(maxMultiplier, ratio);
            if (ratio == 0) break;
        }

        return maxMultiplier;
    }

    private static Object2LongMap<FluidStack> getInputContents(RecipeHandlerGroup holder) {
        var handlers = holder.getInputHandlerMap().get(FluidRecipeCapability.CAP);

        Object2LongOpenHashMap<FluidStack> inventory = new Object2LongOpenHashMap<>();
        if (handlers == null || handlers.isEmpty()) return inventory;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    inventory.addTo(stack, stack.getAmount());
                }
            }
        }
        return inventory;
    }

    @Override
    public @NotNull List<FluidEntryList> createXEIContainerContents(List<FluidIngredient> contents, GTRecipeDefinition recipe, IO io) {
        return contents.stream()
                .map(FluidRecipeCapability::mapFluid)
                .collect(Collectors.toList());
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        // noinspection unchecked
        return new CycleFluidEntryHandler((List<FluidEntryList>) contents);
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
                                 @UnknownNullability("null when content == null") GTRecipeDefinition recipe,
                                 @Nullable FluidIngredient content,
                                 @Nullable Object storage, int recipeTier, int chanceTier) {
        if (widget instanceof TankWidget tank) {
            if (storage instanceof IFluidHandler fluidHandler) {
                tank.setFluidTank(fluidHandler, index);
            }
            tank.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
            tank.setAllowClickFilled(!isXEI);
            tank.setAllowClickDrained(!isXEI && io.support(IO.IN));
            if (isXEI) tank.setShowAmount(false);
            if (content != null) {
                if (content.isChanced()) {
                    tank.setXEIChance((float) content.getChance() / IChancedIngredient.MAX_CHANCE);
                }
                tank.setOnAddedTooltips((w, tooltips) -> {
                    if (!isXEI && content.getFluids().length > 0) {
                        FluidStack stack = content.getFluids()[0];
                        TooltipsHandler.appendFluidTooltips(stack, tooltips::add, TooltipFlag.NORMAL);
                    }
                    if (content instanceof RangedFluidIngredient ranged) {
                        tooltips.add(Component.translatable("gtceu.gui.content.fluid_range",
                                ranged.getMinAmount(), ranged.getAmount())
                                .withStyle(ChatFormatting.GOLD));
                    }
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
                if (io == IO.IN && content.getChance() == 0) {
                    tank.setIngredientIO(IngredientIO.CATALYST);
                }
            }
        }
    }

    // Maps fluids to a FluidEntryList for XEI: either a FluidTagList or a FluidStackList
    public static FluidEntryList mapFluid(FluidIngredient ingredient) {
        int amount = ingredient.getAmount();
        FluidIngredient base = ingredient.getInner();
        FluidIngredient.Value value = base.getValue();
        FluidTagList tags = new FluidTagList();
        FluidStackList fluids = new FluidStackList();
        if (value instanceof FluidIngredient.TagValue tagValue) {
            tags.add(tagValue.getTag(), amount, value.nbt());
        } else {
            fluids.addAll(value.getStacks().stream().map(stack -> {
                FluidStack copy = stack.copy();
                copy.setAmount(amount);
                return copy;
            }).toList());
        }
        if (!tags.isEmpty()) {
            return tags;
        } else {
            return fluids;
        }
    }

    // Fluids should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }

    @Override
    public List<?> getXEIIngredients(List<FluidIngredient> contents, GTRecipeDefinition recipe, IO io) {
        var list = createXEIContainerContents(contents, recipe, io);
        return list.stream()
                .map(FluidEntryList::getStacks)
                .map(fluidStacks -> fluidStacks.stream()
                        .map(fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getTag(), fluidStack.getAmount()))
                        .toList())
                .map(EmiIngredient::of)
                .toList();
    }
}
