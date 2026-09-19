package com.gregtechceu.gtceu.common.machine.misc;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFoundryMachine extends MetaMachine implements IMuiMachine {

    public static final int MAX_MODIFIER_SLOTS = 10;

    @SaveField
    private final CustomItemStackHandler equipmentSlot;
    @SaveField
    private final CustomItemStackHandler moduleSlots;

    public EquipmentFoundryMachine(BlockEntityCreationInfo info) {
        super(info);
        this.equipmentSlot = new CustomItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onEquipmentSlotChanged();
            }
        };

        this.equipmentSlot.setFilter(
                stack -> GTCapabilityHelper.getModularItem(stack) != null);

        this.moduleSlots = new CustomItemStackHandler(MAX_MODIFIER_SLOTS) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return super.isItemValid(slot, stack) && (!isModifierSlotBlocked(slot) || stack.isEmpty()) &&
                        isModuleValid(slot, stack);
            }

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onModifierSlotChanged(slot);
            }
        };
    }

    public boolean isModuleValid(int slot, ItemStack stack) {
        if (this.getLevel() == null) {
            return false;
        }

        return getLevel().getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get())
                .stream().anyMatch(recipe -> recipe.value().matches(equipmentSlot.getStackInSlot(0), stack));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        ParentWidget<?> main = new ParentWidget<>().background(GTGuiTextures.BACKGROUND_EQUIPMENT_FOUNDRY).size(168,
                75);

        List<ItemSlot> moduleSlots = new ArrayList<>();
        for (int i = 0; i < MAX_MODIFIER_SLOTS; i++) {
            moduleSlots.add(new ItemSlot()
                    .background()
                    .slot(new ModularSlot(this.moduleSlots, i)
                            .singletonSlotGroup()));
        }

        main.child(new ItemSlot()
                .left(10)
                .top(28)
                .background()
                .slot(equipmentSlot, 0)
                .slot(new ModularSlot(equipmentSlot, 0)
                        .singletonSlotGroup()
                        .changeListener((oldStack, newStack, client, init) -> {
                            if (ItemStack.isSameItem(oldStack, newStack)) return;
                            var modular = GTCapabilityHelper.getModularItem(newStack);
                            if (modular == null) {
                                moduleSlots.forEach(Widget::background);
                                return;
                            };
                            List<ItemModuleSlot> slots = modular.getSlots();
                            for (int i = 0; i < slots.size() && i < moduleSlots.size(); i++) {
                                ItemSlot slotWidget = moduleSlots.get(i);
                                slotWidget.background(slots.get(i).getSlotTexture());
                            }
                        })))
                .child(new Grid()
                        .background()
                        .left(34)
                        .top(-1)
                        .minColWidth(26)
                        .minRowHeight(39)
                        .gridOf(5, moduleSlots));

        mainWidget.child(main);
    }

    public boolean isModifierSlotBlocked(int slot) {
        ItemStack equipment = equipmentSlot.getStackInSlot(0);
        IModularItem modularItem = GTCapabilityHelper.getModularItem(equipment);
        if (modularItem == null) return true;
        ModuleContext module = modularItem.getModuleContextForSlot(slot);
        if (module != null) return !module.getModule().canRemove(module);
        return modularItem.getSlots().size() <= slot;
    }

    public void onEquipmentSlotChanged() {
        ItemStack stack = equipmentSlot.getStackInSlot(0);
        if (stack.isEmpty()) {
            moduleSlots.clear();
        } else {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
            if (modularItem == null) return;
            for (int i = 0; i < MAX_MODIFIER_SLOTS; i++) {
                var data = modularItem.getModuleContextForSlot(i);
                moduleSlots.setStackInSlot(i, data == null ? ItemStack.EMPTY : data.getData().getModuleItem());
            }
        }
    }

    public void onModifierSlotChanged(int slot) {
        if (getLevel() == null || getLevel().isClientSide) {
            return;
        }

        ItemStack stack = equipmentSlot.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        ModuleContext prevModule = modularItem == null ? null : modularItem.getModuleContextForSlot(slot);
        if (prevModule != null) modularItem.detach(slot);

        ItemStack newModule = moduleSlots.getStackInSlot(slot);
        if (newModule.isEmpty()) return;

        var equipmentItem = equipmentSlot.getStackInSlot(0);
        for (var recipe : getLevel().getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get())) {
            if (recipe.value().matches(equipmentItem, newModule)) {
                recipe.value().applyToItem(equipmentItem, newModule, slot);
            }
        }

        moduleSlots.getStackInSlot(slot);
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == null) return super.getItemHandlerCap(null, useCoverCapability);
        if (side.getAxis().isVertical()) return equipmentSlot;
        if (side.getAxis().isHorizontal()) return moduleSlots;
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ModularScreen createScreen(PosGuiData posGuiData, ModularPanel<?> modularPanel) {
        return new GTGuiScreen(modularPanel, GTGuiTheme.EQUIPMENT_FOUNDRY);
    }
}
