package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MachineConfiguration implements IInteractionItem, ISubItemHandler, IAddInformation, IItemLifeCycle, IComponentCapability {
    public final boolean isWrench;

    protected MachineConfiguration(boolean isWrench) {
        this.isWrench = isWrench;
    }

    public static MachineConfiguration create(boolean isWrench) {
        return new MachineConfiguration(isWrench);
    }



    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        boolean isItemMode = (getConfigMode(stack) & 1) == 1;
        boolean isFluidMode = (getConfigMode(stack) & 2) == 0b10;

        if(!level.isClientSide() && !level.isEmptyBlock(pos) && player != null) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            BlockEntity tileEntity = level.getBlockEntity(pos);

            if(tileEntity instanceof IMachineBlockEntity machineBlockEntity) {
                MetaMachine machine = machineBlockEntity.getMetaMachine();

                if(isItemMode && machine instanceof IAutoOutputItem autoOutputItem) {
                    if(isWrench) autoOutputItem.setOutputFacingItems(context.getClickedFace()); // wrench, item mode
                    else autoOutputItem.setAutoOutputItems(!autoOutputItem.isAutoOutputItems()); // screwdriver, item mode
                }

                if(isFluidMode && machine instanceof IAutoOutputFluid autoOutputFluid) {
                    if(isWrench) autoOutputFluid.setOutputFacingFluids(context.getClickedFace()); // wrench, fluid mode
                    else autoOutputFluid.setAutoOutputFluids(!autoOutputFluid.isAutoOutputFluids()); // screwdriver, fluid mode
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {

        var itemStack = player.getItemInHand(usedHand);

        int mode = getConfigMode(itemStack);
        mode = (mode + 1) % 4;
        boolean isItemMode = (mode & 1) == 1;
        boolean isFluidMode = (mode & 2) == 0b10;
        setConfigMode(itemStack, isItemMode, isFluidMode);
        player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode", Boolean.toString(isItemMode), Boolean.toString(isFluidMode)), true);

        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    private static void setConfigMode(ItemStack stack, boolean isItem, boolean isFluid) {
        var tagCompound = stack.getOrCreateTag();
        tagCompound.putBoolean("ItemMode", isItem);
        tagCompound.putBoolean("FluidMode", isFluid);
    }

    private static int getConfigMode(ItemStack stack) {
        int mode = 0;
        var tagCompound = stack.getTag();
        if(tagCompound == null) return 0;
        mode |= tagCompound.getBoolean("ItemMode") ? 1 : 0;
        mode |= tagCompound.getBoolean("FluidMode") ? 2 : 0;
        return mode;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        return null;
    }


}
