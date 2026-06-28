package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class FoamSprayBehavior implements IInteractionItem, IDurabilityBar, IComponentCapability, IAddInformation {

    private static final IntIntPair DURABILITY_BAR_COLORS = GradientUtil
            .getGradient(GTMaterials.ConstructionFoam.getMaterialRGB(), 10);

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();

        ItemStack offhand = context.getPlayer().getOffhandItem();
        DyeColor color = DyeColor.WHITE;
        if (offhand.getItem() instanceof ComponentItem compItem) {
            var behavior = compItem.getComponents().stream()
                    .filter(comp -> comp instanceof ColorSprayBehaviour)
                    .map(comp -> (ColorSprayBehaviour) comp)
                    .findFirst();
            if (behavior.isPresent()) {
                color = behavior.get().getColor();
            }
        }

        var fluidHandler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).resolve();
        if (fluidHandler.isEmpty()) return InteractionResult.FAIL;
        FluidStack fluidStack = fluidHandler.get().getFluidInTank(0);
        if (fluidStack.getAmount() >= ConfigHolder.INSTANCE.tools.foamSprayerFluidUse) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            BlockState offsetState = context.getLevel()
                    .getBlockState(context.getClickedPos().offset(context.getClickedFace().getNormal()));
            if (state.getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                int maxFrames = fluidStack.getAmount() / ConfigHolder.INSTANCE.tools.foamSprayerFluidUse;
                int framesFoamed = foamFrameBlocks(context.getLevel(), context.getClickedPos(), maxFrames, color,
                        context.getPlayer().isCrouching());
                if (!context.getPlayer().isCreative()) {
                    fluidHandler.get().drain(ConfigHolder.INSTANCE.tools.foamSprayerFluidUse * framesFoamed,
                            IFluidHandler.FluidAction.EXECUTE);
                }
                return InteractionResult.SUCCESS;
            } else if (offsetState.canBeReplaced()) {
                int maxBlocks = fluidStack.getAmount() / ConfigHolder.INSTANCE.tools.foamSprayerFluidUse;
                int blocksFoamed = foamReplaceableBlocks(context.getLevel(), context.getClickedPos(), maxBlocks, color);
                if (!context.getPlayer().isCreative()) {
                    fluidHandler.get().drain(ConfigHolder.INSTANCE.tools.foamSprayerFluidUse * blocksFoamed,
                            IFluidHandler.FluidAction.EXECUTE);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return IInteractionItem.super.useOn(context);
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        var handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).resolve();
        if (handler.isEmpty()) return 0;

        FluidStack fluid = handler.get().getFluidInTank(0);
        return (float) fluid.getAmount() / (float) handler.get().getTankCapacity(0);
    }

    @Override
    public @Nullable IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
        return DURABILITY_BAR_COLORS;
    }

    private static int foamFrameBlocks(Level level, BlockPos pos, int maxBlocksToFoam, DyeColor color,
                                       boolean sneaking) {
        MutableObject<Material> frameMaterial = new MutableObject<>(GTMaterials.NULL);
        List<BlockPos> frameBlocks = gatherReplaceableBlocks(level, pos, 10, (blockState -> {
            if (blockState.getBlock() instanceof MaterialBlock materialBlock) {
                return (frameMaterial.getValue().isNull() || frameMaterial.getValue() == materialBlock.material);
            }
            return false;
        }));
        frameBlocks = frameBlocks.subList(0, Math.min(frameBlocks.size(), maxBlocksToFoam));

        for (BlockPos blockPos : frameBlocks) {
            BlockState state = level.getBlockState(blockPos);
            boolean reinforced = state.is(CustomTags.REINFORCED_FOAM_MAKING_BLOCKS);
            BlockState foam = reinforced ? GTBlocks.REINFORCED_FOAMS.get(color).getDefaultState() :
                    GTBlocks.FOAMS.get(color).getDefaultState();
            level.setBlockAndUpdate(blockPos, foam);
        }

        return frameBlocks.size();
    }

    private static int foamReplaceableBlocks(Level level, BlockPos pos, int maxBlocksToFoam, DyeColor color) {
        List<BlockPos> replaceableBlocks = gatherReplaceableBlocks(level, pos, 10,
                BlockBehaviour.BlockStateBase::canBeReplaced);
        replaceableBlocks = replaceableBlocks.subList(0, Math.min(replaceableBlocks.size(), maxBlocksToFoam));

        for (BlockPos blockPos : replaceableBlocks) {
            level.setBlockAndUpdate(blockPos, GTBlocks.FOAMS.get(color).getDefaultState());
        }

        return replaceableBlocks.size();
    }

    private static List<BlockPos> gatherReplaceableBlocks(Level level, BlockPos center, int radiusSq,
                                                          Predicate<BlockState> filter) {
        List<BlockPos> blocksList = new ArrayList<>();
        BlockState centerState = level.getBlockState(center);
        if (filter.test(centerState)) {
            blocksList.add(center);
        }
        BlockPos.MutableBlockPos currentPos = center.mutable();
        List<Direction> moveStack = new ArrayList<>();
        Vec3i centerVec = new Vec3i(center.getX(), center.getY(), center.getZ());
        OUTER:
        while (true) {
            for (Direction facing : GTUtil.DIRECTIONS) {
                currentPos.move(facing);
                BlockState state = level.getBlockState(currentPos);

                if (currentPos.distSqr(centerVec) <= radiusSq && filter.test(state) &&
                        !blocksList.contains(currentPos)) {
                    blocksList.add(currentPos.immutable());
                    moveStack.add(facing.getOpposite());
                    continue OUTER;
                } else {
                    currentPos.move(facing.getOpposite());
                }
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.remove(moveStack.size() - 1));
            } else {
                break;
            }
        }
        blocksList.sort(Comparator.comparingDouble(i -> i.distSqr(centerVec)));
        return blocksList;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                LazyOptional.of(() -> new FilteredFluidHandlerItemStack(itemStack, 10000,
                        stack -> stack.getFluid() == GTMaterials.ConstructionFoam.getFluid())));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        FluidUtil.getFluidContained(stack).ifPresent(fluid -> tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.fluid_stored", fluid.getDisplayName(),
                        fluid.getAmount())));
    }
}
