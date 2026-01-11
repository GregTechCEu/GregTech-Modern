package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.mui.base.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToolBoxBehavior implements IInteractionItem, IItemUIHolder, IRecipeRemainder, IAddInformation {

    private final int SLOTS = 9;

    public static final ToolBoxBehavior INSTANCE = new ToolBoxBehavior();
    private static final String INV_TAG = "Inventory";
    private static final String SYNC_KEY = "toolbox_slot";

    private ToolBoxBehavior() {}

    public static final ResourceLocation MODEL_OVERRIDE_KEY = new ResourceLocation(GTCEu.MOD_ID, "tool_box_opened");

    public static float getOpenedPredicate(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity,
                                           int seed) {
        return isOpened(stack) ? 1.0f : 0.0f;
    }

    public static boolean isOpened(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("IsOpened");
    }

    public CustomItemStackHandler getInventory(ItemStack stack) {
        CustomItemStackHandler handler = new CustomItemStackHandler(SLOTS);

        handler.setFilter(s -> {
            if (s.getItem() instanceof IGTTool tool) {
                var toolType = tool.getToolType();
                return toolType != null;
            }
            return false;
        });

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(INV_TAG)) {
            handler.deserializeNBT(tag.getCompound(INV_TAG));
        }

        handler.setOnContentsChanged(() -> {
            stack.getOrCreateTag().put(INV_TAG, handler.serializeNBT());
        });

        return handler;
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack stack = data.getUsedItemStack();
        CustomItemStackHandler inventory = getInventory(stack);

        stack.getOrCreateTag().putBoolean("IsOpened", true);

        syncManager.registerSlotGroup("toolbox_slots", SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            syncManager.itemSlot(SYNC_KEY, i, new ModularSlot(inventory, i).slotGroup("toolbox_slots"));
        }

        ModularPanel panel = new ModularPanel("tool_box")
                .background(GTGuiTextures.BACKGROUND)
                .size(176, 166);

        ParentWidget<?> grid = new ParentWidget<>()
                .size(18 * SLOTS, 18);

        for (int i = 0; i < SLOTS; i++) {
            grid.child(new ItemSlot().syncHandler(SYNC_KEY, i).pos(i * 18, 0));
        }

        panel.child(grid);
        syncManager.bindPlayerInventory(data.getPlayer());
        panel.bindPlayerInventory();

        panel.onCloseAction(() -> {
            ItemStack finalStack = data.getUsedItemStack();
            if (!finalStack.isEmpty()) {
                finalStack.getOrCreateTag().putBoolean("IsOpened", false);
                finalStack.getOrCreateTag().put(INV_TAG, inventory.serializeNBT());
                data.getPlayer().setItemInHand(data.getPlayer().getUsedItemHand(), finalStack);
            }
        });
      //  panel.onClose();
        return panel;
    }

    @Override
    public ItemStack getRecipeRemained(ItemStack stack) {
        ItemStack result = stack.copy();
        ItemStackHandler handler = getInventory(result);
        boolean changed = false;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inner = handler.getStackInSlot(i);
            if (!inner.isEmpty() && inner.getItem() instanceof IGTTool tool) {
                int damage = tool.getToolStats().getToolDamagePerCraft(inner);
                if (inner.getDamageValue() + damage >= inner.getMaxDamage()) {
                    handler.setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    inner.setDamageValue(inner.getDamageValue() + damage);
                }
                changed = true;
                break;
            }
        }

        if (changed) {
            result.getOrCreateTag().put(INV_TAG, handler.serializeNBT());
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CustomItemStackHandler handler = getInventory(stack);
        boolean hasItems = false;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inner = handler.getStackInSlot(i);
            if (!inner.isEmpty()) {
                if (!hasItems) {
                    tooltip.add(Component.translatable("item.gtceu.tool_box.contents").withStyle(ChatFormatting.GRAY));
                    hasItems = true;
                }
                tooltip.add(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(inner.getHoverName().copy().withStyle(ChatFormatting.AQUA)));
            }
        }
    }
}
