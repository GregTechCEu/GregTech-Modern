package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Basically a copy of {@link net.minecraft.world.inventory.ResultSlot} for {@link ModularSlot}.
 */
public class ModularCraftingSlot extends ModularSlot {

    private CraftingContainerWrapper craftMatrix;
    @Setter
    private CraftingContainer craftSlots;
    private int amountCrafted;

    public ModularCraftingSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    public boolean isItemValid(@NotNull ItemStack stack) {
        return false;
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
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(getPlayer(), stack, this.craftSlots);
        }

        this.amountCrafted = 0;

        if (this.container instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.craftSlots.getItems());
        }
        if (this.getItemHandler() instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.craftSlots.getItems());
        }
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.checkTakeAchievements(stack);
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> nonnulllist = player.level().getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING,
                this.craftSlots, player.level());
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = this.craftSlots.getItem(i);
            ItemStack itemstack1 = nonnulllist.get(i);

            if (!itemstack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                itemstack = this.craftSlots.getItem(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    this.craftSlots.setItem(i, itemstack1);
                } else if (ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    this.craftSlots.setItem(i, itemstack1);
                } else if (!getPlayer().getInventory().add(itemstack1)) {
                    getPlayer().drop(itemstack1, false);
                }
            }
        }
    }

    public void updateResult(ItemStack stack) {
        set(stack);
        getSyncHandler().forceSyncItem();
    }
}
