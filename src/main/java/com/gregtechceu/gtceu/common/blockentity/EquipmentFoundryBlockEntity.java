package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.managed.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipmentFoundryBlockEntity extends ManagedSyncBlockEntity
                                         implements ICapabilityProvider, IUIHolder<PosGuiData> {

    public static final int MAX_MODIFIER_SLOTS = 10;

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    @SaveField
    private final CustomItemStackHandler equipmentSlot;
    @SaveField
    private final CustomItemStackHandler moduleSlots;

    public EquipmentFoundryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.equipmentSlot = new CustomItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                ItemStack itemStack = super.insertItem(slot, stack, simulate);
                if (!simulate) onEquipmentSlotChanged(null, List.of());
                return itemStack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack itemStack = super.extractItem(slot, amount, simulate);
                if (!simulate) onEquipmentSlotChanged(null, List.of());
                return itemStack;
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
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
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

    public boolean isModuleValid(int slot, @NotNull ItemStack stack) {
        if (this.getLevel() == null) {
            return false;
        }
        NonNullList<ItemStack> stacks = NonNullList.create();
        stacks.add(this.equipmentSlot.getStackInSlot(0));
        stacks.add(stack);
        RecipeWrapper newWrapper = new RecipeWrapper(new CustomItemStackHandler(stacks));

        return getLevel().getRecipeManager()
                .getRecipeFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get(), newWrapper, this.getLevel())
                .map(recipe -> recipe.matches(newWrapper, slot)).orElse(false);
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData posGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        List<ItemSlot> moduleSlots = new ArrayList<>();
        for (int i = 0; i < MAX_MODIFIER_SLOTS; i++) {
            moduleSlots.add(new ItemSlot()
                    .background()
                    .slot(new ModularSlot(this.moduleSlots, i)
                            .singletonSlotGroup()));
        }
        return new ModularPanel<>("equipment_foundry")
                .size(168, 75)
                .bindPlayerInventory(-80)
                .child(GTMuiWidgets.createTitleBar(GTBlocks.EQUIPMENT_FOUNDRY::asStack, 176,
                        GTGuiTextures.BACKGROUND_STEEL))
                .child(new ItemSlot()
                        .left(10)
                        .top(28)
                        .background()
                        .slot(equipmentSlot, 0)
                        .slot(new ModularSlot(equipmentSlot, 0)
                                .singletonSlotGroup()
                                .changeListener((stack, onlyAmount, client, init) -> {
                                    onEquipmentSlotChanged(posGuiData.getPlayer(), moduleSlots);

                                })))
                .child(new Grid()
                        .background()
                        .left(34)
                        .top(-1)
                        .minColWidth(26)
                        .minRowHeight(39)
                        .gridOf(5, moduleSlots));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * ModularUI modularUI = new ModularUI(176, 166, this, entityPlayer);
     * modularUI.background(GuiTextures.BACKGROUND.copy().setColor(0xff69645f));
     * 
     * IGuiTexture slotTexture = GuiTextures.SLOT.copy().setColor(0xff69645f);
     * 
     * TextTexture titleText = new TextTexture(getBlockState().getBlock().getDescriptionId())
     * .setColor(0xffffff)
     * .setDropShadow(false)
     * .setType(TextTexture.TextType.ROLL)
     * .setWidth(105);
     * titleText.setRollSpeed(0.7f);
     * modularUI.widget(new WidgetGroup(9, -16, 160, 16)
     * .addWidget(new ImageWidget(
     * 16, 2, 105, 16, titleText))
     * .setBackground(GuiTextures.TITLE_BAR_BACKGROUND.copy().setColor(0xff69645f)));
     * modularUI.widget(new ImageWidget(4, 4, 168, 75, GuiTextures.EQUIPMENT_FOUNDRY_BACKGROUND));
     * List<SlotWidget> slotWidgets = new ArrayList<>();
     * modularUI.widget(new SlotWidget(equipmentSlot, 0, 14, 32)
     * .setChangeListener(() -> this.onEquipmentSlotChanged(entityPlayer, slotWidgets))
     * .setBackgroundTexture(null));
     * 
     * int x = 42;
     * int y = 13;
     * for (int i = 0; i < MAX_MODIFIER_SLOTS; i++) {
     * final int finalI = i;
     * SlotWidget slotWidget = new SlotWidget(moduleSlots, i, x, y)
     * // .setIsBlocked(() -> isModifierSlotBlocked(finalI))
     * .setBackgroundTexture(null);
     * modularUI.widget(slotWidget);
     * slotWidgets.add(slotWidget);
     * x += 26;
     * if (i == 4) {
     * x = 42;
     * y = 52;
     * }
     * }
     * // modularUI.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), slotTexture, 7, 84, true));
     * return modularUI;
     * }
     */

    public boolean isModifierSlotBlocked(int slot) {
        ItemStack equipment = equipmentSlot.getStackInSlot(0);
        IModularItem modularItem = GTCapabilityHelper.getModularItem(equipment);
        if (modularItem == null) return true;
        AppliedItemModule module = modularItem.getModuleInSlot(slot);
        if (module != null) return !(module.canRemove() && module.getModuleItem() != null);
        return modularItem.getSlots().size() <= slot;
    }

    public void onEquipmentSlotChanged(@Nullable Player player, List<ItemSlot> slotWidgets) {
        ItemStack stack = equipmentSlot.getStackInSlot(0);
        if (stack.isEmpty()) {
            for (int i = 0; i < moduleSlots.getSlots(); i++) {
                ItemStack out = moduleSlots.extractItem(i, Integer.MAX_VALUE, true);
                if (out.isEmpty()) {
                    continue;
                }
                out = moduleSlots.extractItem(i, Integer.MAX_VALUE, false);
                out.shrink(1);
                if (player != null && !player.getInventory().add(out)) {
                    player.drop(out, true);
                } else if (!out.isEmpty() && getLevel() != null) {
                    Block.popResource(getLevel(), getBlockPos(), out);
                }
            }
            slotWidgets.forEach(Widget::background);
        } else {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
            if (modularItem == null) return;
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
                if (module.getSlot() < MAX_MODIFIER_SLOTS && module.getModuleItem() != null) {
                    moduleSlots.setStackInSlot(module.getSlot(), module.getModuleItem());
                }
            }
            List<ItemModuleSlot> slots = modularItem.getSlots();
            for (int i = 0; i < slots.size() && i < slotWidgets.size(); i++) {
                ItemSlot slotWidget = slotWidgets.get(i);
                if (slots.get(i) != null && slotWidget != null) {
                    slotWidget.background(slots.get(i).getSlotTexture());
                }
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
        AppliedItemModule prevModule = modularItem == null ? null : modularItem.getModuleInSlot(slot);
        if (prevModule != null) prevModule.detach();
        ItemStack newModule = moduleSlots.getStackInSlot(slot);
        if (newModule.isEmpty()) return;
        RecipeWrapper recipeWrapper = new RecipeWrapper(new CombinedInvWrapper(
                this.equipmentSlot,
                new CustomItemStackHandler(newModule)));
        Optional<EquipmentFoundryRecipe> recipe = getLevel().getRecipeManager().getRecipeFor(
                GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get(),
                recipeWrapper,
                this.getLevel());
        if (recipe.isPresent()) {
            ItemStack newStack = recipe.get().assemble(recipeWrapper, slot);
            if (newStack.isEmpty()) return;
            equipmentSlot.setStackInSlot(0, newStack);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            if (side.getAxis().isVertical())
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> equipmentSlot));
            if (side.getAxis().isHorizontal())
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> moduleSlots));
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ModularScreen createScreen(PosGuiData posGuiData, ModularPanel<?> modularPanel) {
        return new GTGuiScreen(modularPanel, GTGuiTheme.EQUIPMENT_FOUNDRY);
    }
}
