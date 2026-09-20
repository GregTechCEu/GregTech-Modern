package brachy.modularui.widgets.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

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
            stack.onCraftedBy(getPlayer(), this.amountCrafted);
            EventHooks.firePlayerCraftingEvent(getPlayer(), stack, this.getCraftSlots());
        }

        this.amountCrafted = 0;

        if (this.getItemHandler() instanceof RecipeCraftingHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.craftSlots.getItems());
        }
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

        CraftingInput.Positioned positioned = this.getCraftSlots().asPositionedCraftInput();
        CraftingInput input = positioned.input();
        NonNullList<ItemStack> recipeInputs;
        CommonHooks.setCraftingPlayer(player);
        try {
            if (player.level() instanceof ServerLevel level) {
                recipeInputs = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level)
                        .map(recipe -> recipe.value().getRemainingItems(input))
                        .orElseGet(() -> {
                            NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
                            for (int i = 0; i < input.size(); i++) remaining.set(i, input.getItem(i).copy());
                            return remaining;
                        });
            } else {
                recipeInputs = CraftingRecipe.defaultCraftingReminder(input);
            }
        } finally {
            CommonHooks.setCraftingPlayer(null);
        }

        for (int inputIndex = 0; inputIndex < recipeInputs.size(); ++inputIndex) {
            int i = inputIndex % input.width() + positioned.left() +
                    (inputIndex / input.width() + positioned.top()) * this.getCraftSlots().getWidth();
            ItemStack slotItem = this.getCraftSlots().getItem(i);
            ItemStack recipeItem = recipeInputs.get(inputIndex);

            if (!slotItem.isEmpty()) {
                this.getCraftSlots().removeItem(i, 1);
                slotItem = this.getCraftSlots().getItem(i);
            }

            if (recipeItem.isEmpty()) {
                continue;
            }
            if (slotItem.isEmpty()) {
                this.getCraftSlots().setItem(i, recipeItem);
            } else if (ItemStack.isSameItemSameComponents(slotItem, recipeItem)) {
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

        ServerLevel level = player.level();
        ItemStack result = ItemStack.EMPTY;

        Optional<RecipeHolder<CraftingRecipe>> possibleRecipe = level.recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, getCraftSlots().asCraftInput(), level);
        if (possibleRecipe.isPresent()) {
            RecipeHolder<CraftingRecipe> recipe = possibleRecipe.get();
            if (setRecipeUsed(getItemHandler(), player, recipe)) {
                result = recipe.value().assemble(getCraftSlots().asCraftInput());
                if (!result.isItemEnabled(level.enabledFeatures())) {
                    result = ItemStack.EMPTY;
                }
            }
        }

        set(result);
    }

    protected boolean setRecipeUsed(@Nullable Object possibleRecipeHolder, ServerPlayer player, RecipeHolder<CraftingRecipe> recipe) {
        if (!recipe.value().isSpecial() && player.level().getGameRules().get(GameRules.LIMITED_CRAFTING) &&
                !player.getRecipeBook().contains(recipe.id())) {
            return false;
        }

        if (possibleRecipeHolder instanceof RecipeCraftingHolder recipeHolder) {
            recipeHolder.setRecipeUsed(recipe);
        }

        return true;
    }
}
