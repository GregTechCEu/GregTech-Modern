package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior {

    public static final PlungerBehavior INSTANCE = new PlungerBehavior();

    protected PlungerBehavior() {/**/}

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        IFluidTransfer fluidHandler = FluidTransferHelper.getFluidTransfer(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        IFluidTransfer handlerToRemoveFrom = fluidHandler;
//                player.isCrouching() ?
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).input : null) :
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).output : fluidHandler);

        if (handlerToRemoveFrom != null && handlerToRemoveFrom.drain(1000, true) != null) {
            ToolHelper.onActionDone(context.getPlayer(), context.getLevel(), context.getHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /*
    @Override
    public ICapabilityProvider createProvider(ItemStack stack, @Nullable CompoundTag tag) {
        return new VoidFluidHandlerItemStack(stack) {

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                int result = super.fill(resource, doFill);
                if (result > 0) {
                    ToolHelper.damageItem(getContainer(), null);
                }
                return result;
            }
        };
    }
    */

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.plunger"));
    }
}