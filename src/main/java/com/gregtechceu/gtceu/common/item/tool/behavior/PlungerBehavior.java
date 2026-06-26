package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.component.IComponentCapability;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlungerBehavior implements IToolBehavior<PlungerBehavior>, IComponentCapability, IInteractionItem {

    public static final PlungerBehavior INSTANCE = PlungerBehavior.create();
    public static final Codec<PlungerBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, PlungerBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

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
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        IFluidHandler fluidHandler;
        if (level.getBlockEntity(context.getClickedPos()) instanceof MetaMachine machine) {
            fluidHandler = machine.getFluidHandlerCap(context.getClickedFace(), false);
        } else {
            fluidHandler = FluidUtil.getFluidHandler(level, context.getClickedPos(), context.getClickedFace())
                    .orElse(null);
        }
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        FluidStack drained = fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
            ToolHelper.onActionDone(player, stack, level, context.getClickLocation());
            return InteractionResult.sidedSuccess(level.isClientSide);
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
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, unused) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, Integer.MAX_VALUE) {

                    @Override
                    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                        int result = resource.getAmount();
                        if (result > 0) {
                            ToolHelper.damageItem(this.getContainer(), null);
                        }
                        return result;
                    }
                }, item);
    }
}
