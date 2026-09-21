package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStats implements IInteractionItem, ISubItemHandler, IAddInformation, IItemLifeCycle,
                           IComponentCapability, IItemDecoratorComponent {

    public static final ElectricStats EMPTY = ElectricStats.create(0, 0, false, false);

    public final long maxCharge;
    public final int tier;

    public final boolean chargeable;
    public final boolean dischargeable;

    protected ElectricStats(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        this.maxCharge = maxCharge;
        this.tier = tier;
        this.chargeable = chargeable;
        this.dischargeable = dischargeable;
    }

    public static ElectricStats create(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        return new ElectricStats(maxCharge, tier, chargeable, dischargeable);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> capability) {
        return GTCapability.CAPABILITY_ELECTRIC_ITEM.orEmpty(capability,
                LazyOptional.of(() -> new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable)));
    }

    public static float getStoredPredicate(ItemStack itemStack) {
        var electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem != null) {
            var per = (electricItem.getCharge() * 7 / electricItem.getMaxCharge());
            return per / 100f;
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        var itemStack = player.getItemInHand(usedHand);
        var electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem != null) electricItem.use(itemStack, level, player, usedHand);
        return InteractionResultHolder.pass(item);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) electricItem.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) electricItem.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
        var stack = new ItemStack(item);
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            items.add(stack);
        }
    }

    public static ElectricStats createElectricItem(long maxCharge, int tier) {
        return ElectricStats.create(maxCharge, tier, true, false);
    }

    public static ElectricStats createRechargeableBattery(long maxCharge, int tier) {
        return ElectricStats.create(maxCharge, tier, true, true);
    }

    public static ElectricStats createBattery(long maxCharge, int tier, boolean rechargeable) {
        return ElectricStats.create(maxCharge, tier, rechargeable, true);
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            return ToolChargeBarRenderer.renderElectricBar(guiGraphics, electricItem.getCharge(),
                    electricItem.getMaxCharge(), xOffset, yOffset, stack.isBarVisible());
        }
        return false;
    }
}
