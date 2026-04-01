package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

import lombok.Setter;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * An output slot for crafting recipes for modular UIs. For input slots use regular {@link ModularSlot ModularSlots}.
 * The implementation is mostly copied from {@link net.minecraft.world.inventory.ResultSlot ResultSlot}.
 * To use this, you must call {@link #inputInventory(IItemHandlerModifiable, int)} or {@link #inputInventory(IItemHandlerModifiable)}.
 * If the grid has any other size than 3x3, you also need to call {@link #gridSize(int, int)}.
 * It is required that all input slots use the same {@link IItemHandlerModifiable}. Additionally, all input slots and this output slot, MUST
 * be in the same slot group.
 */
@SuppressWarnings("unused")
public class ModularCraftingSlot extends ModularSlot {

    private CraftingContainerWrapper craftSlots;
    private int cols = 3, rows = 3;
    private int inputStartIndex;
    private IItemHandlerModifiable inputInventory;

    private int amountCrafted;
    private final Consumer<Slot> slotChangeListener = this::updateCraftResult;

    public ModularCraftingSlot(IItemHandler outputInventory, int index) {
        super(outputInventory, index);
        this.canPut(false);
    }

    /**
     * Optional setter for the crafting grid size. By default, 3x3 is assumed.
     * Note that this is ONLY used to calculate the total amount slots used in the input inventory.
     *
     * @param columns columns / width
     * @param rows    rows / height
     * @return this
     */
    public ModularCraftingSlot gridSize(int columns, int rows) {
        this.cols = columns;
        this.rows = rows;
        this.craftSlots = null;
        return this;
    }

    /**
     * Mandatory setter for the input inventory. The input inventory must contain the slot indices consecutively from left to right,
     * top to bottom. For a 3x3 grid it would look like this:
     * <p>
     * 0 1 2 <br>
     * 3 4 5 <br>
     * 6 7 8
     * </p>
     * It is the users responsibility to assign the correct inventory and index to the input slots in the UI.
     *
     * @param inputInventory the input inventory which is used for all input slots
     * @return this
     */
    public ModularCraftingSlot inputInventory(IItemHandlerModifiable inputInventory) {
        return inputInventory(inputInventory, 0);
    }

    /**
     * Mandatory setter for the input inventory. The input inventory must contain the slot indices consecutively from left to right,
     * top to bottom. For a 3x3 grid with starting index 0 it would look like this:
     * <p>
     * 0 1 2 <br>
     * 3 4 5 <br>
     * 6 7 8
     * </p>
     * It is the users responsibility to assign the correct inventory and index to the input slots in the UI.
     *
     * @param inputInventory the input inventory which is used for all input slots
     * @param startIndex     the starting index where the consecutive input slots can be found
     * @return this
     */
    public ModularCraftingSlot inputInventory(IItemHandlerModifiable inputInventory, int startIndex) {
        this.inputInventory = inputInventory;
        this.inputStartIndex = startIndex;
        this.craftSlots = null;
        return this;
    }

    public CraftingContainerWrapper getCraftSlots() {
        if (this.craftSlots == null) {
            if (this.inputInventory == null) {
                throw new IllegalStateException("The crafting inventory of the crafting slot has not been initialised. " +
                        "Call inputInventory() with appropriate arguments and optionally gridSize().");
            }
            this.craftSlots = new CraftingContainerWrapper(this, this.cols, this.rows, this.inputInventory, this.inputStartIndex);
        }
        return this.craftSlots;
    }

    // Register the slot change listener
    @Override
    public ModularSlot slotGroup(SlotGroup slotGroup) {
        if (this.getSlotGroup() == slotGroup) return this;
        if (this.getSlotGroup() != null) {
            this.getSlotGroup().removeSlotChangeListener(this.slotChangeListener);
        }
        if (slotGroup != null) {
            slotGroup.addSlotChangeListener(this.slotChangeListener);
        }

        return super.slotGroup(slotGroup);
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public @NotNull ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        this.amountCrafted += numItemsCrafted;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void checkTakeAchievements(@NotNull ItemStack stack) {
        if (this.amountCrafted > 0) {
            stack.onCraftedBy(getPlayer().level(), getPlayer(), this.amountCrafted);
            ForgeEventFactory.firePlayerCraftingEvent(getPlayer(), stack, this.getCraftSlots());
        }

        if (this.getItemHandler() instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.getCraftSlots().getItems());
        }

        this.amountCrafted = 0;
    }

    @Override
    public void onCraftShiftClick(Player playerIn, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            playerIn.drop(itemStack, false);
        }
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.checkTakeAchievements(stack);

        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> recipeInputs = player.level().getRecipeManager()
                .getRemainingItemsFor(RecipeType.CRAFTING, this.getCraftSlots(), player.level());
        ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < recipeInputs.size(); ++i) {
            ItemStack slotItem = this.getCraftSlots().getItem(i);
            ItemStack recipeItem = recipeInputs.get(i);

            if (!slotItem.isEmpty()) {
                this.getCraftSlots().removeItem(i, 1);
                slotItem = this.getCraftSlots().getItem(i);
            }

            if (recipeItem.isEmpty()) {
                continue;
            }
            if (slotItem.isEmpty()) {
                this.getCraftSlots().setItem(i, recipeItem);
            } else if (ItemStack.isSameItemSameTags(slotItem, recipeItem)) {
                recipeItem.grow(slotItem.getCount());
                this.getCraftSlots().setItem(i, recipeItem);
            } else if (!player.getInventory().add(recipeItem)) {
                player.drop(recipeItem, false);
            }
        }
        // force update recipe
        updateCraftResult(null);
    }

    protected void updateCraftResult(Slot slot) {
        // don't check possible crafting recipes if this is the slot that changed
        if (slot == this) return;
        // acts as a side check and a cast
        if (!(getSyncHandler().getSyncManager().getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        ItemStack result = ItemStack.EMPTY;

        Optional<CraftingRecipe> possibleRecipe = player.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, getCraftSlots(), level);
        if (possibleRecipe.isPresent()) {
            CraftingRecipe recipe = possibleRecipe.get();
            if (setRecipeUsed(getItemHandler(), player, recipe)) {
                result = recipe.assemble(getCraftSlots(), level.registryAccess());
                if (!result.isItemEnabled(level.enabledFeatures())) {
                    result = ItemStack.EMPTY;
                }
            }
        }

        set(result);
    }

    protected boolean setRecipeUsed(@Nullable Object possibleRecipeHolder, ServerPlayer player, Recipe<?> recipe) {
        if (!recipe.isSpecial() && player.level().getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) &&
                !player.getRecipeBook().contains(recipe)) {
            return false;
        }
        if (possibleRecipeHolder instanceof RecipeHolder recipeHolder) {
            recipeHolder.setRecipeUsed(recipe);
        }
        return true;
    }
}