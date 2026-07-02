package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.RangedItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.SimpleTagIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;
import com.gregtechceu.gtceu.utils.*;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class ItemRecipeCapability extends RecipeCapability<ItemIngredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, ItemIngredient.CODEC);
    }

    @Override
    public ItemIngredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return ItemIngredient.fromNetwork(friendlyByteBuf);
    }

    @Override
    public void toNetwork(ItemIngredient ingredient, FriendlyByteBuf friendlyByteBuf) {
        ingredient.toNetwork(friendlyByteBuf);
    }

    @Override
    public ItemIngredient copyInner(ItemIngredient content, int multiplier) {
        return content.copyWithMultiplier(multiplier);
    }

    @Override
    public boolean isChanced(ItemIngredient content) {
        return content.isChanced();
    }

    @Override
    public IGuiTexture createXEIOverlay(ItemIngredient content, boolean perTick) {
        return new IGuiTexture() {

            @Override
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(graphics, x, y, width, height, content.getChance());
                if (content instanceof RangedItemIngredient ranged) {
                    drawString(graphics, x, y, width, height, "%s-%s".formatted(ranged.getMinCount(), ranged.getCount()),
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
    public List<AbstractMapIngredient> getMapIngredients(ItemIngredient content) {
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

        int maxCount = 0;
        List<ItemIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            maxCount = Math.max(maxCount, content.getCount());
            ingredients.add(content);
        }

        if (maxCount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxCount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxCount;
        }

        while (minMultiplier != maxMultiplier) {
            List<ItemIngredient> copied = new ArrayList<>();
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

        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        Object2LongMap<ItemStack> inventory = getInputContents(holder);
        if (inventory.isEmpty()) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        var nonConsumables = new Object2LongOpenHashMap<ItemIngredient>();
        var consumables = new Object2LongOpenHashMap<ItemIngredient>();
        for (var content : inputs) {

            int count = content.getCount();

            if (content.getChance() == 0) {
                nonConsumables.addTo(content, count);
            } else {
                consumables.addTo(content, count);
            }
        }

        // is this even possible
        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;

        // Check for enough NC in inventory
        for (var ncEntry : Object2LongMaps.fastIterable(nonConsumables)) {
            ItemIngredient ingredient = ncEntry.getKey();
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
            ItemIngredient ingredient = cEntry.getKey();
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

    private static Object2LongMap<ItemStack> getInputContents(RecipeHandlerGroup holder) {
        var handlers = holder.getInputHandlerMap().get(ItemRecipeCapability.CAP);
        final var strat = ItemStackHashStrategy.comparingAllButCount();
        Object2LongOpenCustomHashMap<ItemStack> inventory = new Object2LongOpenCustomHashMap<>(strat);
        if (handlers == null || handlers.isEmpty()) return inventory;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    inventory.addTo(stack, stack.getCount());
                }
            }
        }
        return inventory;
    }

    @Override
    public @NotNull List<ItemEntryList> createXEIContainerContents(List<ItemIngredient> contents, GTRecipeDefinition recipe, IO io) {
        List<ItemEntryList> entryLists = contents.stream()
                .map(ItemRecipeCapability::mapItem)
                .collect(Collectors.toList());

        if (io == IO.OUT && recipe.recipeType.isScanner()) {
            List<ItemEntryList> scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            ResearchManager.ResearchItem researchData = null;
            for (var ingredient : recipe.getOutputContents(this)) {
                ItemStack[] stacks = ingredient.getItems();
                if (stacks.length == 0 || stacks[0].isEmpty()) continue;

                researchData = ResearchManager.readResearchId(stacks[0]);
                if (researchData != null) break;
            }
            if (researchData != null) {
                Collection<GTRecipeDefinition> possibleRecipes = researchData.recipeType()
                        .getDataStickEntry(researchData.researchId());
                Set<ItemStack> cache = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingItem());
                if (possibleRecipes != null) {
                    for (GTRecipeDefinition             r : possibleRecipes) {
                        var outputs = r.getOutputContents(this);
                        if (outputs.isEmpty()) continue;

                        var outputContent = outputs.get(0);
                        ItemStack[] stacks = outputContent.getItems();
                        if (stacks.length == 0) continue;

                        ItemStack researchStack = stacks[0];
                        if (!researchStack.isEmpty() && !cache.contains(researchStack)) {
                            cache.add(researchStack);
                            scannerPossibilities.add(ItemStackList.of(researchStack.copyWithCount(1)));
                        }
                    }
                }
                scannerPossibilities.add(entryLists.get(0));
                entryLists = scannerPossibilities;
            }
        }

        return entryLists;
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        // noinspection unchecked
        return new CycleItemEntryHandler((List<ItemEntryList>) contents);
    }

    @NotNull
    @Override
    public Widget createWidget() {
        SlotWidget slot = new SlotWidget();
        slot.initTemplate();
        return slot;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return SlotWidget.class;
    }

    @Override
    public void applyWidgetInfo(@NotNull Widget widget,
                                int index,
                                boolean isXEI,
                                IO io,
                                GTRecipeTypeUI.@UnknownNullability("null when storage == null") RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @UnknownNullability("null when content == null") GTRecipeDefinition recipe,
                                @Nullable ItemIngredient content,
                                @Nullable Object storage, int recipeTier, int chanceTier) {
        if (widget instanceof SlotWidget slot) {
            if (storage instanceof IItemHandlerModifiable items) {
                if (index >= 0 && index < items.getSlots()) {
                    slot.setHandlerSlot(items, index);
                    slot.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
                    slot.setCanTakeItems(!isXEI);
                    slot.setCanPutItems(!isXEI && io.support(IO.IN));
                    if (items instanceof IContentChangeAware item1) {
                        slot.setChangeListener(item1.getOnContentsChanged());
                    }
                }
                // 1 over container size.
                // If in a recipe viewer and a research slot can be added, add it.
                if (isXEI && recipeType.isHasResearchSlot() && index == items.getSlots()) {
                    if (ConfigHolder.INSTANCE.machines.enableResearch) {
                        ResearchCondition condition = recipeHolder.conditions().stream()
                                .filter(ResearchCondition.class::isInstance).findAny()
                                .map(ResearchCondition.class::cast).orElse(null);
                        if (condition != null) {
                            List<ItemStack> dataItems = new ArrayList<>();
                            for (ResearchData.ResearchEntry entry : condition.data) {
                                ItemStack dataStick = entry.getDataItem().copy();
                                ResearchManager.writeResearchToNBT(dataStick.getOrCreateTag(), entry.getResearchId(),
                                        recipeType);
                                dataItems.add(dataStick);
                            }
                            CycleItemEntryHandler handler = CycleItemEntryHandler.fromStacks(List.of(dataItems));
                            slot.setHandlerSlot(handler, 0);
                            slot.setIngredientIO(IngredientIO.CATALYST);
                            slot.setCanTakeItems(false);
                            slot.setCanPutItems(false);
                        }
                    }
                }
            }
            if (content != null) {
                if (content.isChanced()) {
                    slot.setXEIChance((float) content.getChance() / IChancedIngredient.MAX_CHANCE);
                }
                slot.setOnAddedTooltips((w, tooltips) -> {
                    if (content instanceof RangedItemIngredient ranged) {
                        tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                                ranged.getMinCount(), ranged.getCount())
                                .withStyle(ChatFormatting.GOLD));
                    }
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
                if (io == IO.IN && content.getChance() == 0) {
                    slot.setIngredientIO(IngredientIO.CATALYST);
                }
            }
        }
    }

    // Maps ingredients to an ItemEntryList for XEI: either an ItemTagList or an ItemStackList
    private static ItemEntryList mapItem(final ItemIngredient ingredient) {
        var tagList = tryMapTag(ingredient.getInner(), ingredient.getCount());
        if (tagList != null) return tagList;

        ItemStackList stackList = new ItemStackList();
        for (ItemStack i : ingredient.getItems()) {
            stackList.add(i);
        }
        return stackList;
    }

    private static @Nullable ItemTagList tryMapTag(final ItemIngredient ingredient, int amount) {
        if (ingredient instanceof SimpleTagIngredient tagIngredient) {
            return ItemTagList.of(tagIngredient.getTag(), amount, null);
        }
        return null;
    }

    // Items should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }

    @Override
    public List<?> getXEIIngredients(List<ItemIngredient> contents, GTRecipeDefinition recipe, IO io) {
        var list = createXEIContainerContents(contents, recipe, io);
        return list.stream()
                .map(ItemEntryList::getStacks)
                .map(stacks -> stacks.stream().map(EmiStack::of).toList())
                .map(EmiIngredient::of)
                .toList();
    }
}
