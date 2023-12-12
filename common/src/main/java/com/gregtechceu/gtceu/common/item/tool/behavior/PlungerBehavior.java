package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior {

    public static final PlungerBehavior INSTANCE = new PlungerBehavior();

    protected PlungerBehavior() {/**/}

    @Override
    public InteractionResult onItemUseFirst(@NotNull Player player, @NotNull Level world, @NotNull BlockPos pos,
                                            @NotNull Direction facing, float hitX, float hitY, float hitZ,
                                            @NotNull InteractionHand hand) {
        IFluidTransfer fluidHandler = FluidTransferHelper.getFluidTransfer(world, pos, facing);
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        IFluidTransfer handlerToRemoveFrom = fluidHandler;
//                player.isCrouching() ?
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).input : null) :
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).output : fluidHandler);

        if (handlerToRemoveFrom != null && handlerToRemoveFrom.drain(1000, true) != null) {
            ToolHelper.onActionDone(player, world, hand);
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
        tooltip.add(I18n.format("item.gt.tool.behavior.plunger"));
    }
}