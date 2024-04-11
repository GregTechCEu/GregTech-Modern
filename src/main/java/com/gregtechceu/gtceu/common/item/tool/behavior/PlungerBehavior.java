package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior, IComponentCapability, IInteractionItem {

    public static final PlungerBehavior INSTANCE = PlungerBehavior.create();

    protected PlungerBehavior() {/**/}

    protected static PlungerBehavior create() {
        return new PlungerBehavior();
    }

    @Override
    public boolean shouldOpenUIAfterUse(UseOnContext context) {
        return !(context.getPlayer() != null && context.getPlayer().isShiftKeyDown());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        IFluidHandler fluidHandler = FluidTransferHelper.getFluidTransfer(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        FluidStack drained = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            ToolHelper.onActionDone(context.getPlayer(), context.getLevel(), context.getHand());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.plunger"));
    }

    @Override
    public void attachCaps(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) -> new FluidHandlerItemStack(stack, Integer.MAX_VALUE) {
            @Override
            public int fill(FluidStack resource, FluidAction action) {
                int result = resource.getAmount();
                if (result > 0) {
                    ToolHelper.damageItem(this.getContainer(), null);
                }
                return result;
            }
        }, item);
    }
}
