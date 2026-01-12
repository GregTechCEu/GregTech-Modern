package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.mui.base.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gregtechceu.gtceu.api.item.component.ElectricStats.addCurrentChargeTooltip;
import static dev.emi.emi.search.EmiSearch.tooltips;

public class ToolBoxBehavior implements IInteractionItem, IItemUIHolder, IRecipeRemainder, IAddInformation {

    private static final int SLOTS = 9;

    public static final ToolBoxBehavior INSTANCE = new ToolBoxBehavior();
    private static final String INV_TAG = "inventory";
    private static final String SYNC_KEY = "toolbox_slot";

    private ToolBoxBehavior() {}

    public static final ResourceLocation MODEL_OVERRIDE_KEY = new ResourceLocation(GTCEu.MOD_ID, "tool_box_opened");

    public static boolean isOpened(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("is_opened");
    }

    public static CustomItemStackHandler getInventory(ItemStack stack) {
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
        // TODO: Open/closed state

        // stack.getOrCreateTag().putBoolean("is_opened", true);
        syncManager.registerSlotGroup("toolbox_slots", SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            syncManager.itemSlot(SYNC_KEY, i, new ModularSlot(inventory, i).slotGroup("toolbox_slots"));
        }

        ParentWidget<?> grid = new ParentWidget<>()
                .size(18 * SLOTS, 18);
        for (int i = 0; i < SLOTS; i++) {
            grid.child(new ItemSlot().syncHandler(SYNC_KEY, i).pos(i * 18, 0));
        }

        ModularPanel panel = new ModularPanel("tool_box")
                .height(113)
                .child(GTMuiWidgets.createTitleBar(stack, 172).right(48))
                .child(grid.top(7).left(7));

        panel.bindPlayerInventory();

        syncManager.bindPlayerInventory(data.getPlayer(), (inv, index) -> {
            ModularSlot slot = new ModularSlot(inv, index);
            if (inv.getStackInSlot(index) == stack) {
                slot.accessibility(false, false);
            }
            return slot;
        });

        // panel.onCloseAction(() -> {
        // ItemStack finalStack = data.getUsedItemStack();
        // if (!finalStack.isEmpty()) {
        // finalStack.getOrCreateTag().putBoolean("is_opened", false);
        // finalStack.getOrCreateTag().put(INV_TAG, inventory.serializeNBT());
        // data.getPlayer().setItemInHand(data.getPlayer().getUsedItemHand(), finalStack);
        // }
        // });
        return panel;
    }

    @Override
    public ItemStack getRecipeRemained(ItemStack stack) {
        ItemStack result = stack.copy();
        ItemStackHandler handler = getInventory(result);
        boolean changed = false;
        String typeName = stack.getOrCreateTagElement("last_used_tool").getString("type");
        GTToolType lastType = GTToolType.getTypes().get(typeName);
        if (lastType == null) return result;

        var player = net.minecraftforge.common.ForgeHooks.getCraftingPlayer();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inner = handler.getStackInSlot(i);
            if (inner.getItem() instanceof IGTTool tool) {
                if (tool.getToolType().craftingTags.get(0).equals(lastType.craftingTags.get(0))) {
                    ToolHelper.damageItemWhenCrafting(inner, player);
                    handler.setStackInSlot(i, inner);
                    changed = true;
                    break;
                }
            }
        }

        if (changed) {
            result.getOrCreateTag().put(INV_TAG, handler.serializeNBT());
        }
        return result;
    }

    public static Set<TagKey<Item>> getAvailableTools(ItemStack stack) {
        CustomItemStackHandler inventory = getInventory(stack);
        Set<TagKey<Item>> result = new HashSet<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).getItem() instanceof IGTTool tool) {
                result.add(tool.getToolType().craftingTags.get(0));
            }
        }
        return result;
    }

    private static List<Component> getTooltip(ItemStack inner, @Nullable Level level) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(" • ").append(inner.getHoverName().copy()
                .append(Component.literal(
                        " §a%d / %d".formatted(inner.getMaxDamage() - inner.getDamageValue(), inner.getMaxDamage())))));

        if (inner.getItem() instanceof IGTTool tool) {
            if (tool.isElectric()) {
                IElectricItem item = GTCapabilityHelper.getElectricItem(inner);
                if (item != null) {
                    if (GTUtil.isShiftDown()) {
                        addCurrentChargeTooltip(tooltip, item.getCharge(), item.getMaxCharge(), item.getTier(), false);
                        tooltip.set(tooltip.size() - 1,
                                Component.literal("    ").append(tooltip.get(tooltip.size() - 1)));
                    }
                }
            }
        }

        return tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CustomItemStackHandler handler = getInventory(stack);
        List<Component> tooltips = new ArrayList<Component>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inner = handler.getStackInSlot(i);
            if (!inner.isEmpty()) {
                tooltips.addAll(getTooltip(inner, level));
            }
        }
        if (tooltips.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool_box.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.addAll(tooltips);
        }
    }
}
