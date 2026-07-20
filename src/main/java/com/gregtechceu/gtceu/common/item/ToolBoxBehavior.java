package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ToolBoxBehavior implements IItemUIFactory, IRecipeRemainder, IAddInformation {

    private static final int SLOTS = 9;
    private static final String INVENTORY_TAG = "inventory";
    public static final String TOOL_TYPES = "tool_types";
    public static final String LAST_USED_TOOL_TAG = "last_used_tool";

    public static final ToolBoxBehavior INSTANCE = new ToolBoxBehavior();

    private ToolBoxBehavior() {}

    public static CustomItemStackHandler getInventory(ItemStack stack) {
        CustomItemStackHandler handler = new CustomItemStackHandler(SLOTS);
        handler.setFilter(candidate -> candidate.getItem() instanceof IGTTool tool && tool.getToolType() != null);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(INVENTORY_TAG)) {
            handler.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        }
        handler.setOnContentsChanged(() -> {
            stack.getOrCreateTag().put(INVENTORY_TAG, handler.serializeNBT());
            Set<GTToolType> toolTypes = new ReferenceArraySet<>();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack toolStack = handler.getStackInSlot(slot);
                if (!toolStack.isEmpty()) {
                    toolTypes.addAll(ToolHelper.getToolTypes(toolStack));
                }
            }
            String types = "";
            for (var type : toolTypes) {
                types += type.name;
                types += " ";
            }
            stack.getOrCreateTag().put(TOOL_TYPES, StringTag.valueOf(types));
        });
        return handler;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();
        CustomItemStackHandler toolboxInventory = getInventory(stack);
        ModularUI ui = new ModularUI(176, 121, holder, player)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 5, stack.getDescriptionId()));

        for (int slot = 0; slot < SLOTS; slot++) {
            ui.widget(new SlotWidget(toolboxInventory, slot, 7 + slot * 18, 19)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        ui.widget(createPlayerInventory(player.getInventory(), player.getInventory().selected));
        return ui;
    }

    private static WidgetGroup createPlayerInventory(Inventory inventory, int lockedHotbarSlot) {
        WidgetGroup group = new WidgetGroup(7, 39, 162, 76);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + (row + 1) * 9;
                group.addWidget(new SlotWidget(inventory, slot, column * 18, row * 18)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setLocationInfo(true, false));
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            boolean isHeldToolboxSlot = slot == lockedHotbarSlot;
            group.addWidget(new SlotWidget(inventory, slot, slot * 18, 58,
                    !isHeldToolboxSlot, !isHeldToolboxSlot)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setLocationInfo(true, true));
        }
        return group;
    }

    @Override
    public ItemStack getRecipeRemained(ItemStack stack) {
        ItemStack result = stack.copy();
        String lastUsedTool = result.getOrCreateTag().getString(LAST_USED_TOOL_TAG);
        if (lastUsedTool.isEmpty()) {
            return result;
        }

        GTToolType requestedToolType = GTToolType.getTypes().get(lastUsedTool);
        result.removeTagKey(LAST_USED_TOOL_TAG);
        if (requestedToolType == null) {
            return result;
        }

        CustomItemStackHandler inventory = getInventory(result);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack toolStack = inventory.getStackInSlot(slot);
            if (toolStack.getItem() instanceof IGTTool tool && tool.getToolType() != null &&
                    tool.getToolType() == requestedToolType) {
                ToolHelper.damageItemWhenCrafting(toolStack, ForgeHooks.getCraftingPlayer());
                inventory.setStackInSlot(slot, toolStack);
                break;
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<Component> contents = new ArrayList<>();
        CustomItemStackHandler inventory = getInventory(stack);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack toolStack = inventory.getStackInSlot(slot);
            if (!toolStack.isEmpty()) {
                contents.add(Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(toolStack.getHoverName().copy().withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" %d / %d".formatted(
                                toolStack.getMaxDamage() - toolStack.getDamageValue(), toolStack.getMaxDamage()))
                                .withStyle(ChatFormatting.GREEN)));
            }
        }
        if (contents.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool_box.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.addAll(contents);
        }
    }
}
