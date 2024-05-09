package com.gregtechceu.gtceu.common.items.tool.behavior;

import com.gregtechceu.gtceu.api.items.component.IInteractionItem;
import com.gregtechceu.gtceu.api.items.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.items.tool.ToolHelper;
import com.gregtechceu.gtceu.api.items.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.items.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.data.GTDataComponents;
import com.gregtechceu.gtceu.data.GTToolBehaviors;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlungerBehavior implements IToolBehavior<PlungerBehavior>, IComponentCapability, IInteractionItem {

    public static final PlungerBehavior INSTANCE = PlungerBehavior.create();
    public static final MapCodec<PlungerBehavior> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PlungerBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

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
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        IFluidHandler fluidHandler;

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof IMachineBlockEntity metaMachineBlockEntity) {
            fluidHandler = metaMachineBlockEntity.getMetaMachine().getFluidTransferCap(context.getClickedFace(), false);
        } else {
            fluidHandler = FluidTransferHelper.getFluidTransfer(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        }

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
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.plunger"));
    }

    @Override
    public ToolBehaviorType<PlungerBehavior> getType() {
        return GTToolBehaviors.PLUNGER;
    }

    @Override
    public void attachCaps(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, Integer.MAX_VALUE) {
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