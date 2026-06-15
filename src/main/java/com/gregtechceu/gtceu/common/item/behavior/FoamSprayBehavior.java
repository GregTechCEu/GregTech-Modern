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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FoamSprayBehavior implements IInteractionItem, IDurabilityBar, IComponentCapability, IAddInformation {

    private static final IntIntPair DURABILITY_BAR_COLORS = GradientUtil
            .getGradient(GTMaterials.ConstructionFoam.getMaterialRGB(), 10);

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.FAIL;
        var stack = context.getItemInHand();

        var offhand = context.getPlayer().getOffhandItem();
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
        var fluidStack = fluidHandler.get().getFluidInTank(0);
        if (fluidStack.getAmount() >= ConfigHolder.INSTANCE.tools.foamSprayerBlockAmount) {
            var state = context.getLevel().getBlockState(context.getClickedPos());
            var offsetState = context.getLevel()
                    .getBlockState(context.getClickedPos().offset(context.getClickedFace().getNormal()));
            if (state.getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                int maxFrames = fluidStack.getAmount() / ConfigHolder.INSTANCE.tools.foamSprayerBlockAmount;
                int framesFoamed = foamFrameBlocks(context.getLevel(), context.getClickedPos(), maxFrames, color,
                        context.getPlayer().isCrouching());
                if (!context.getPlayer().isCreative()) {
                    fluidHandler.get().drain(ConfigHolder.INSTANCE.tools.foamSprayerBlockAmount * framesFoamed,
                            IFluidHandler.FluidAction.EXECUTE);
                }
                return InteractionResult.SUCCESS;
            } else if (offsetState.canBeReplaced()) {
                int maxBlocks = fluidStack.getAmount() / ConfigHolder.INSTANCE.tools.foamSprayerBlockAmount;
                int blocksFoamed = foamReplaceableBlocks(context.getLevel(), context.getClickedPos(), maxBlocks, color);
                if (!context.getPlayer().isCreative()) {
                    fluidHandler.get().drain(ConfigHolder.INSTANCE.tools.foamSprayerBlockAmount * blocksFoamed,
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
        var frameBlocks = gatherSameFrames(level, pos, 10);
        frameBlocks = frameBlocks.subList(0, Math.min(frameBlocks.size(), maxBlocksToFoam));

        for (BlockPos blockPos : frameBlocks) {
            BlockState state = level.getBlockState(blockPos);
            boolean reinforced = state.is(CustomTags.REINFORCED_FRAMES);
            BlockState foam = reinforced ? GTBlocks.REINFORCED_FOAMS.get(color).getDefaultState() :
                    GTBlocks.FOAMS.get(color).getDefaultState();
            level.setBlockAndUpdate(blockPos, foam);
        }

        return frameBlocks.size();
    }

    private static int foamReplaceableBlocks(Level level, BlockPos pos, int maxBlocksToFoam, DyeColor color) {
        var replaceableBlocks = gatherReplaceableBlocks(level, pos, 10);
        replaceableBlocks = replaceableBlocks.subList(0, Math.min(replaceableBlocks.size(), maxBlocksToFoam));

        for (var blockPos : replaceableBlocks) {
            level.setBlockAndUpdate(blockPos, GTBlocks.FOAMS.get(color).getDefaultState());
        }

        return replaceableBlocks.size();
    }

    private static List<BlockPos> gatherReplaceableBlocks(Level level, BlockPos center, int radiusSq) {
        Set<BlockPos> blocks = new ObjectOpenHashSet<>();
        List<BlockPos> blocksList = new ArrayList<>();
        blocks.add(center);
        blocksList.add(center);
        BlockPos.MutableBlockPos currentPos = center.mutable();
        List<Direction> moveStack = new ArrayList<>();
        Vec3i centerVec = new Vec3i(center.getX(), center.getY(), center.getZ());
        outer:
        while (true) {
            for (var facing : GTUtil.DIRECTIONS) {
                currentPos.move(facing);
                var state = level.getBlockState(currentPos);

                if (state.canBeReplaced() && currentPos.distSqr(centerVec) <= radiusSq &&
                        !blocks.contains(currentPos)) {
                    blocks.add(currentPos.immutable());
                    blocksList.add(currentPos.immutable());
                    moveStack.add(facing.getOpposite());
                    continue outer;
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

    private static List<BlockPos> gatherSameFrames(Level level, BlockPos center, int radiusSq) {
        Set<BlockPos> frames = new ObjectOpenHashSet<>();
        List<BlockPos> framesList = new ArrayList<>();
        frames.add(center);
        framesList.add(center);
        BlockPos.MutableBlockPos currentPos = center.mutable();
        Material frameMaterial = GTMaterials.NULL;
        List<Direction> moveStack = new ArrayList<>();
        Vec3i centerVec = new Vec3i(center.getX(), center.getY(), center.getZ());
        outer:
        while (true) {
            for (var facing : GTUtil.DIRECTIONS) {
                currentPos.move(facing);
                var state = level.getBlockState(currentPos);

                if (state.getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                    if (currentPos.distSqr(centerVec) <= radiusSq &&
                            (frameMaterial.isNull() || frameMaterial == matBlock.material) &&
                            !frames.contains(currentPos)) {
                        frames.add(currentPos.immutable());
                        framesList.add(currentPos.immutable());
                        moveStack.add(facing.getOpposite());
                        frameMaterial = matBlock.material;
                        continue outer;
                    } else {
                        currentPos.move(facing.getOpposite());
                    }
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
        framesList.sort(Comparator.comparingDouble(i -> i.distSqr(centerVec)));
        return framesList;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                    LazyOptional.of(() -> new FilteredFluidHandlerItemStack(itemStack, 10000,
                            stack -> stack.getFluid() == GTMaterials.ConstructionFoam.getFluid())));
        }
        return LazyOptional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        FluidUtil.getFluidContained(stack).ifPresent(fluid -> tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.fluid_stored", fluid.getDisplayName(),
                        fluid.getAmount())));
    }
}
